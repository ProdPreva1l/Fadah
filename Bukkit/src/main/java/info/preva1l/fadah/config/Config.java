package info.preva1l.fadah.config;

import de.exlll.configlib.*;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public class Config {
    private static Config instance;

    private static final String CONFIG_HEADER = """
            #########################################
            #                  Fadah                #
            #    Finally a Decent Auction House     #
            #########################################
            """;

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .header(CONFIG_HEADER).build();

    @Comment("Toggle with /ah toggle")
    private boolean enabled = true;
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    private int defaultMaxListings = 3;
    private String decimalFormat = "#,###.00";
    private boolean logToFile = true;

    @Comment("Enable this if you are having de-sync issues with multi-server.")
    private boolean strictChecks = false;

    private ListingPrice listingPrice = new ListingPrice();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ListingPrice {
        private double min = 100;
        private double max = 1000000000;
    }

    private ListingAdverts listingAdverts = new ListingAdverts();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ListingAdverts {
        @Comment("Whether or not a listing advert should be made by default.")
        private boolean enabledByDefault = false;
        @Comment({"How much it costs to advertise the listing by default.", "Overridden by the `fadah.advert-price.<amount>` permission."})
        private double defaultPrice = 500;
    }

    private List<String> blacklists = List.of("%material% == \"BEDROCK\"", "%material% == \"NETHER_STAR\" && %name%.includes(\"Menu\")");

    private Hooks hooks = new Hooks();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Hooks {
        private boolean ecoItems = false;

        private Discord discord = new Discord();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Discord {
            private boolean enabled = false;
            private String webhookUrl = "INSERT WEBHOOK URL HERE";

            @Comment("If this is true the webhook will only send a message when the listing has been advertised.")
            private boolean onlySendOnAdvert = false;

            @Comment("Allowed: EMBED, PLAIN_TEXT")
            private DiscordHook.Mode messageMode = DiscordHook.Mode.EMBED;

            private Embed embed = new Embed();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Embed {
                @Comment("Allowed: SIDE, BOTTOM")
                private DiscordHook.ImageLocation imageLocation = DiscordHook.ImageLocation.SIDE;
                private String title = "New Listing by %player%!";
                private String content = "%player% just listed %item% for $%price% on the auction house!";
                private String footer = "Powered by Finally a Decent Auction House";
            }

            private String plainText = "%player% just listed %item% for $%price% on the auction house!";
        }

        private InfluxDB influxdb = new InfluxDB();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class InfluxDB {
            private boolean enabled = false;
            private String uri = "http://localhost:8086";
            private String token = "MyToken";
            private String org = "MyOrg";
            private String bucket = "Fadah";
        }
    }

    private Migrators migrators = new Migrators();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Migrators {
        private ZAuctionHouse zAuctionHouse = new ZAuctionHouse();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ZAuctionHouse {
            private List<String> categoriesToMigrate = List.of("Blocks", "Tools", "Weapons", "Potions", "Misc");
        }
    }

    private Database database = new Database();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Database {
        @Comment("Allowed: SQLITE, MYSQL, MARIADB, MONGO")
        private DatabaseType type = DatabaseType.SQLITE;
        private String uri = "jdbc:mysql://username:password@127.0.0.1:3306/Fadah";
        private String database = "Fadah";
        private boolean useSsl = false;
        private Advanced advanced = new Advanced();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Advanced {
            private int poolSize = 10;
            private int minIdle = 10;
            private int maxLifetime = 1800000;
            private int keepaliveTime = 0;
            private int connectionTimeout = 5000;
        }
    }

    @Comment({"A message broker is only required for x-server environments.",
            "This is not compatible with SQLITE database"})
    private Broker broker = new Broker();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Broker {
        private boolean enabled = false;
        @Comment("Allowed: REDIS")
        private info.preva1l.fadah.multiserver.Broker.Type type = info.preva1l.fadah.multiserver.Broker.Type.REDIS;
        private String host = "localhost";
        private int port = 6379;
        private String password = "myAwesomePassword";
        private String channel = "fadah.cache";
    }

    public void save() {
        YamlConfigurations.save(new File(Fadah.getINSTANCE().getDataFolder(), "config.yml").toPath(), Config.class, this);
    }

    public static void reload() {
        instance = YamlConfigurations.load(new File(Fadah.getINSTANCE().getDataFolder(), "config.yml").toPath(), Config.class, PROPERTIES);
    }

    public static Config i() {
        if (instance != null) {
            return instance;
        }

        return instance = YamlConfigurations.update(new File(Fadah.getINSTANCE().getDataFolder(), "config.yml").toPath(), Config.class, PROPERTIES);
    }
}