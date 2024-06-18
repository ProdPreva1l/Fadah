package info.preva1l.fadah;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.api.AuctionHouseAPI;
import info.preva1l.fadah.api.BukkitAuctionHouseAPI;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.commands.AuctionHouseCommand;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.data.*;
import info.preva1l.fadah.hooks.HookManager;
import info.preva1l.fadah.hooks.impl.EcoItemsHook;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.*;
import info.preva1l.fadah.utils.commands.CommandManager;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutManager;
import info.preva1l.fadah.utils.helpers.TransactionLogger;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

// TODO Biddable Auctions
// TODO Custom Inventory Layouts (In progress)
// TODO Folia & ShreddedPaper support (In progress)
// TODO Purchasable listing adverts
// TODO Allow listing adverts to send automatically everytime a new listing is made

public final class Fadah extends JavaPlugin {
    private static final int METRICS_ID = 21651;

    @Getter private static Fadah INSTANCE;
    @Getter @Setter private static NamespacedKey customItemKey;

    @Getter private static Logger console;
    @Getter private final Logger transactionLogger = Logger.getLogger("AuctionHouse-Transactions");

    @Getter private BasicConfig configFile;
    @Getter private BasicConfig categoriesFile;
    @Getter private BasicConfig langFile;
    @Getter private BasicConfig menusFile;

    @Getter private BasicConfig miscMenuFile;
    @Getter private BasicConfig buttonsFile;

    @Getter @Setter private CacheSync cacheSync;
    @Getter private Database database;
    @Getter private CommandManager commandManager;
    @Getter private Economy economy;
    @Getter private HookManager hookManager;
    @Getter private LayoutManager layoutManager;

    private Metrics metrics;

    @Override
    public void onEnable() {
        INSTANCE = this;
        console = getLogger();
        hookManager = new HookManager();

        if (!hookIntoVault()) {
            getConsole().severe("------------------------------------------");
            getConsole().severe("Disabled due to no Vault dependency found!");
            getConsole().severe("------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            getConsole().info("Vault Hooked!");
        }

        loadMenus();
        loadFiles();
        loadDataAndPopulateCaches();
        loadCommands();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        TaskManager.Async.runTask(this, listingExpiryTask(), 10L);
        FastInvManager.register(this);

        if (Config.REDIS_ENABLED.toBoolean()) {
            if (Config.DATABASE_TYPE.toDBTypeEnum() == DatabaseType.SQLITE) {
                getConsole().severe("------------------------------------------");
                getConsole().severe("Redis has not been enabled as the selected");
                getConsole().severe("       database is not compatible!");
                getConsole().severe("------------------------------------------");
                return;
            }
            cacheSync = new CacheSync();
            cacheSync.start();
        }

        customItemKey = NamespacedKey.minecraft("auctionhouse");

        loadHooks();

        initLogger();
        setupMetrics();

        getConsole().info("Enabling the API...");
        AuctionHouseAPI.setInstance(new BukkitAuctionHouseAPI());
        getConsole().info("API Enabled!");

        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&2&l------------------------------"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&a Finally a Decent Auction House"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&a   has successfully started!"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&2&l------------------------------"));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MultiLib.getEntityScheduler(player).execute(this,
                    () -> player.closeInventory(InventoryCloseEvent.Reason.UNLOADED),
                    () -> getConsole().severe("Failed to close %s's inventory (Entity Not Found)"),
                    0L);
        }
        if (database != null) database.destroy();
        if (cacheSync != null) cacheSync.destroy();
        if (metrics != null) metrics.shutdown();
    }

    private Runnable listingExpiryTask() {
        return () -> {
            for (UUID key : ListingCache.getListings().keySet()) {
                Listing listing = ListingCache.getListing(key);
                if (Instant.now().toEpochMilli() >= listing.getDeletionDate()) {
                    ListingCache.removeListing(listing);
                    getINSTANCE().getDatabase().removeListing(listing.getId());

                    CollectableItem item = new CollectableItem(listing.getItemStack(), Instant.now().toEpochMilli());
                    ExpiredListingsCache.addItem(listing.getOwner(), item);
                    getINSTANCE().getDatabase().addToExpiredItems(listing.getOwner(), item);

                    TransactionLogger.listingExpired(listing);
                }
            }
        };
    }

    private void loadCommands() {
        getConsole().info("Loading commands...");
        this.commandManager = new CommandManager(this);
        new AuctionHouseCommand(this);
        getConsole().info("Commands Loaded!");
    }

    private void loadFiles() {
        getConsole().info("Loading Configuration Files...");
        configFile = new BasicConfig(this, "config.yml");
        categoriesFile = new BasicConfig(this, "categories.yml");
        langFile = new BasicConfig(this, "lang.yml");
        menusFile = new BasicConfig(this, "menus.yml");

        Config.loadDefault();
        Lang.loadDefault();
        Menus.loadDefault();

        categoriesFile.save();
        categoriesFile.load();
        getConsole().info("Configuration Files Loaded!");
    }

    private void loadMenus() {
        layoutManager = new LayoutManager();

        buttonsFile = new BasicConfig(this, "menus/buttons.yml");
        miscMenuFile = new BasicConfig(this, "menus/misc.yml");

        layoutManager.loadLayout(new BasicConfig(this, "menus/main.yml"));
        layoutManager.loadLayout(new BasicConfig(this, "menus/new-listing.yml"));
        layoutManager.loadLayout(new BasicConfig(this, "menus/expired-listings.yml"));
        layoutManager.loadLayout(new BasicConfig(this, "menus/active-listings.yml"));
        layoutManager.loadLayout(new BasicConfig(this, "menus/historic-items.yml"));
        layoutManager.loadLayout(new BasicConfig(this, "menus/confirm.yml"));
        layoutManager.loadLayout(new BasicConfig(this, "menus/collection-box.yml"));
        layoutManager.loadLayout(new BasicConfig(this, "menus/profile.yml"));
    }

    private boolean hookIntoVault() {
        getConsole().info("Hooking into Vault...");
        if (INSTANCE.getServer().getPluginManager().getPlugin("Vault") == null) {
            getConsole().info("Vault not installed");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getConsole().info("No Economy Plugin Installed");
            return false;
        }
        economy = rsp.getProvider();

        return true;
    }


    private void loadDataAndPopulateCaches() {
        getConsole().info("Connecting to Database and populating caches...");
        getConsole().info("DB Type: %s".formatted(Config.DATABASE_TYPE.toDBTypeEnum().getFriendlyName()));

        database = switch (Config.DATABASE_TYPE.toDBTypeEnum()) {
            case MONGO -> new MongoDatabase();
            case MYSQL, MARIADB -> new MySQLDatabase();
            case SQLITE -> new SQLiteDatabase();
        };
        database.connect();

        CategoryCache.loadCategories();

        getConsole().info("Connected to Database and populated caches!");
    }

    private void loadHooks() {
        getConsole().info("Configuring Hooks...");

        if (Config.HOOK_ECO_ITEMS.toBoolean()) {
            new EcoItemsHook();
        }

        getConsole().info("Hooked into %s plugins!".formatted(getHookManager().hookCount()));
    }

    private void setupMetrics() {
        getConsole().info("Starting Metrics...");

        metrics = new Metrics(this, METRICS_ID);
        metrics.addCustomChart(new Metrics.SingleLineChart("items_listed", () -> ListingCache.getListings().size()));

        getConsole().info("Metrics Logging Started!");
    }

    private void initLogger() {
        getConsole().info("Initialising transaction logger...");

        if (!Config.LOG_TO_FILE.toBoolean()) {
            return;
        }
        try {
            File logsFolder = new File(this.getDataFolder(), "logs");
            if (!logsFolder.exists()) {
                if (!logsFolder.mkdirs()) {
                    getConsole().warning("Failed to create logs folder!");
                    return;
                }
            }

            File logFile = new File(logsFolder, "transaction-log.log");
            if (logFile.exists()) {
                long epochMillis = System.currentTimeMillis();
                String newFileName = "transaction-log_" + epochMillis + ".log";
                File renamedFile = new File(logsFolder, newFileName);
                if (!logFile.renameTo(renamedFile)) {
                    getConsole().warning("Could not rename logfile!");
                }
            }

            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            fileHandler.setFormatter(new TransactionLogFormatter());
            transactionLogger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getConsole().info("Logger Started!");
    }
}
