package info.preva1l.fadah;

import info.preva1l.fadah.api.AuctionHouseAPI;
import info.preva1l.fadah.api.ImplAuctionHouseAPI;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.commands.AuctionHouseCommand;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.data.Database;
import info.preva1l.fadah.data.MongoDatabase;
import info.preva1l.fadah.data.MySQLDatabase;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.BasicConfig;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.commands.CommandManager;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//TODO: Add listing tax
public final class Fadah extends JavaPlugin {
    @Getter private static final DecimalFormat decimalFormat = new DecimalFormat(Config.DECIMAL_FORMAT.toString());

    @Getter private static Fadah INSTANCE;
    @Getter private static AuctionHouseAPI API;
    @Getter private CacheSync cacheSync;

    @Getter private BasicConfig configFile;
    @Getter private BasicConfig categoriesFile;
    @Getter private BasicConfig langFile;
    @Getter private BasicConfig menusFile;

    @Getter private final Logger transactionLogger = Logger.getLogger("AuctionHouse-Transactions");
    @Getter private static Logger console;

    @Getter private Database database;
    @Getter private CommandManager commandManager;
    @Getter private Economy economy;

    @Getter @Setter private static NamespacedKey customItemKey;

    @Override
    public void onEnable() {
        INSTANCE = this;
        console = getLogger();

        if (!hookIntoVault()) {
            getConsole().severe("Disabled due to no Vault dependency found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        initLogger();
        loadFiles();
        loadDataAndPopulateCaches();
        loadCommands();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        TaskManager.Async.runTask(this, listingExpiryTask(), 10L);

        if (Config.REDIS_ENABLED.toBoolean()) {
            cacheSync = new CacheSync();
            cacheSync.start();
        }

        customItemKey = NamespacedKey.minecraft("auctionhouse");

        API = new ImplAuctionHouseAPI();
    }

    @Override
    public void onDisable() {
        if (database != null) database.destroy();
    }

    private Runnable listingExpiryTask() {
        return () -> {
            for (Listing listing : ListingCache.getListings()) {
                if (Instant.now().toEpochMilli() >= listing.deletionDate()) {
                    ListingCache.removeListing(listing);
                    getINSTANCE().getDatabase().removeListing(listing.id());

                    CollectableItem item = new CollectableItem(listing.itemStack(), Instant.now().toEpochMilli());
                    ExpiredListingsCache.addItem(listing.owner(), item);
                    getINSTANCE().getDatabase().addToExpiredItems(listing.owner(), item);
                }
            }
        };
    }

    private void loadCommands() {
        this.commandManager = new CommandManager(this);
        new AuctionHouseCommand(this);
    }

    private void loadFiles() {
        configFile = new BasicConfig(this, "config.yml");
        categoriesFile = new BasicConfig(this, "categories.yml");
        langFile = new BasicConfig(this, "lang.yml");
        menusFile = new BasicConfig(this, "menus.yml");

        Config.loadDefault();
        Lang.loadDefault();
        Menus.loadDefault();

        categoriesFile.save();
        categoriesFile.load();
    }

    private boolean hookIntoVault() {
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
        return economy.isEnabled();
    }

    private void loadDataAndPopulateCaches() {
        database = switch (Config.DATABASE_TYPE.toDBTypeEnum()) {
            case MONGO -> new MongoDatabase();
            case MYSQL, MARIADB -> new MySQLDatabase();
        };
        database.connect();
        database.loadListings();

        CategoryCache.loadCategories();
    }

    private void initLogger() {
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
            fileHandler.setFormatter(new SimpleFormatter());
            transactionLogger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
