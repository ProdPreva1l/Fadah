package info.preva1l.fadah.config;

import de.exlll.configlib.*;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseType;
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
            #########################################
            """;

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .header(CONFIG_HEADER).build();

    private Commands commands = new Commands();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Commands {
        private Main main = new Main();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Main {
            private List<String> aliases = List.of("ah", "auctions", "auction");
        }

        private Sell Sell = new Sell();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Sell {
            private String description = "Create a new listing on the auction house!";
            private List<String> aliases = List.of("new-listing", "create-listing");
        }
    }

    private Database database = new Database();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Database {
        @Comment("Allowed: MYSQL, MARIADB")
        private DatabaseType type = DatabaseType.MARIADB;

        private String host = "localhost";
        private int port = 3306;
        private String database = "PrisonCore";
        private String username = "root";
        private String password = "";
        private boolean useSsl = false;
    }

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