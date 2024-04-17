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
import info.preva1l.fadah.data.*;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.BasicConfig;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.TransactionLogFormatter;
import info.preva1l.fadah.utils.commands.CommandManager;
import info.preva1l.fadah.utils.guis.FastInvManager;
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
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//TODO: Add listing tax
public final class Fadah extends JavaPlugin {
    @Getter private static Fadah INSTANCE;
    @Getter private static AuctionHouseAPI API;
    @Getter @Setter private CacheSync cacheSync;

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
            getConsole().severe("------------------------------------------");
            getConsole().severe("Disabled due to no Vault dependency found!");
            getConsole().severe("------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
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

        initLogger();

        API = new ImplAuctionHouseAPI();
    }

    @Override
    public void onDisable() {
        if (database != null) database.destroy();
        if (cacheSync != null) cacheSync.destroy();
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

                    Fadah.getINSTANCE().getTransactionLogger().info(StringUtils.formatPlaceholders("[LISTING EXPIRED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                            Bukkit.getOfflinePlayer(listing.owner()).getName(), Bukkit.getOfflinePlayer(listing.owner()).getUniqueId().toString(),
                            listing.price(), listing.itemStack().toString()));
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
        return economy != null;
    }

    private void loadDataAndPopulateCaches() {
        database = switch (Config.DATABASE_TYPE.toDBTypeEnum()) {
            case MONGO -> new MongoDatabase();
            case MYSQL, MARIADB -> new MySQLDatabase();
            case SQLITE -> new SQLiteDatabase();
        };
        database.connect();

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
            fileHandler.setFormatter(new TransactionLogFormatter());
            transactionLogger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
