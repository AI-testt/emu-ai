package ains.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class ConfigData {

    private static final Logger log = LoggerFactory.getLogger(ConfigData.class);

    public static final String DB_URL;
    public static final String DB_USERNAME;
    public static final String DB_PASSWORD;

    public static final int MAX_CONNECTION;
    public static final int REQUEST_THREADS;
    // seconds without any packet from a client before its connection is closed (client pings every 20s); 0 disables
    public static final int SERVER_IDLE_TIMEOUT;
    // largest single packet in bytes; the client's +666 char shift makes every character 2 UTF-8 bytes
    public static final int MAX_FRAME_LENGTH;

    public static final int MAXIMUM_POOL_SIZE;
    public static final int CONNECTION_TIMEOUT;
    public static final int LEAK_DETECTION_THRESHOLD;
    public static final int MINIMUM_IDLE;
    public static final int IDLE_TIMEOUT;
    public static final int MAX_LIFE_TIME;

    public static final boolean CACHE_PRERP_STMTS;
    public static final int PREP_STMT_CACHE_SIZE;
    public static final int PREP_STMT_CACHE_SQL_LIMIT;
    public static final boolean USE_SSL;

    public static long ANTI_MESSAGEFLOOD_MIN_MSG_TIME;
    public static int ANTI_MESSAGEFLOOD_TOLERANCE;
    public static int ANTI_MESSAGEFLOOD_MAX_REPEATED;
    public static int ANTI_MESSAGEFLOOD_WARNINGS;
    public static long ANTI_REQUESTFLOOD_MIN_MSG_TIME;
    public static int ANTI_REQUESTFLOOD_TOLERANCE;
    public static int ANTI_REQUESTFLOOD_MAX_REPEATED;
    public static int ANTI_REQUESTFLOOD_WARNINGS;
    public static boolean ANTI_REQUESTFLOOD_REPEAT_ENABLED;
    public static int ANTI_REQUESTFLOOD_COUNT;
    public static long ANTI_REQUESTFLOOD_REQUEST_COUNTER_MAX;

    public static String SERVER_IP;
    public static int SERVER_PORT;
    // WebSocket port for browser (Ruffle) clients; 0 or less disables it
    public static int SERVER_WS_PORT;
    // Bind address of the WebSocket port (default server.ip); use 127.0.0.1 when only a local reverse proxy connects
    public static String SERVER_WS_IP;
    // Reverse proxies whose X-Real-IP / X-Forwarded-For is trusted ("loopback" = 127.0.0.1/::1)
    public static String[] WS_TRUSTED_PROXIES;
    // Flash socket policy server on port 843 (one per host) and the ports its policy allows
    public static boolean POLICY_ENABLED;
    public static String POLICY_PORTS;
    public static final String SERVER_NAME;
    public static final boolean STAFF_ONLY;

    public static String DISCORD_BOT_TOKEN;

    private ConfigData() {
        throw new AssertionError("Cannot instantiate ConfigData");
    }

    static {
        Properties config = new Properties();

        try {
            try (FileInputStream input = new FileInputStream(getConfigPath().toFile())) {
                config.load(input);
            }
        } catch (IOException e) {
            log.error("Error in loading configuration: {}", e.getMessage());
        }

        DB_URL = config.getProperty("database.url");
        DB_USERNAME = config.getProperty("database.user");
        DB_PASSWORD = config.getProperty("database.pass");

        MAX_CONNECTION = parseInteger(config.getProperty("connection.max_connection"), 10);

        MAXIMUM_POOL_SIZE = parseInteger(config.getProperty("connection.config.maximum_pool_size"), 10);
        CONNECTION_TIMEOUT = parseInteger(config.getProperty("connection.config.connections_timeout"), 10000);
        LEAK_DETECTION_THRESHOLD = parseInteger(config.getProperty("connection.config.leak_detection_threshold"), 30000);
        MINIMUM_IDLE = parseInteger(config.getProperty("connection.config.minimum_idle"), 5);
        IDLE_TIMEOUT = parseInteger(config.getProperty("connection.config.idle_timeout"), 120000);
        MAX_LIFE_TIME = parseInteger(config.getProperty("connection.config.max_lifetime"), 600000);

        CACHE_PRERP_STMTS = Boolean.parseBoolean(config.getProperty("data.source.cache_prep_stmts"));
        PREP_STMT_CACHE_SIZE = parseInteger(config.getProperty("data.source.prep_stmt_cache_size"), 250);
        PREP_STMT_CACHE_SQL_LIMIT = parseInteger(config.getProperty("data.source.prep_stmt_cache_sql_limit"), 2048);
        USE_SSL = Boolean.parseBoolean(config.getProperty("data.source.use_ssl"));

        ANTI_MESSAGEFLOOD_MIN_MSG_TIME = Long.parseLong(config.getProperty("antiflood.message.minimumtime"));
        ANTI_MESSAGEFLOOD_TOLERANCE = Integer.parseInt(config.getProperty("antiflood.message.tolerance"));
        ANTI_MESSAGEFLOOD_MAX_REPEATED = Integer.parseInt(config.getProperty("antiflood.message.maxrepeated"));
        ANTI_MESSAGEFLOOD_WARNINGS = Integer.parseInt(config.getProperty("antiflood.message.warnings"));
        ANTI_REQUESTFLOOD_MIN_MSG_TIME = Long.parseLong(config.getProperty("antiflood.request.minimumtime"));
        ANTI_REQUESTFLOOD_TOLERANCE = Integer.parseInt(config.getProperty("antiflood.request.tolerance"));
        ANTI_REQUESTFLOOD_MAX_REPEATED = Integer.parseInt(config.getProperty("antiflood.request.maxrepeated"));
        ANTI_REQUESTFLOOD_WARNINGS = Integer.parseInt(config.getProperty("antiflood.request.warnings"));
        ANTI_REQUESTFLOOD_REPEAT_ENABLED = Boolean.parseBoolean(config.getProperty("antiflood.request.enablerepeatfilter"));
        ANTI_REQUESTFLOOD_COUNT = Integer.parseInt(config.getProperty("antiflood.request.count"));
        ANTI_REQUESTFLOOD_REQUEST_COUNTER_MAX = Long.parseLong(config.getProperty("antiflood.request.request_counter.max"));

        SERVER_IP = config.getProperty("server.ip");
        // request handlers block on JDBC, so the pool should be larger than the core count
        REQUEST_THREADS = parseInteger(config.getProperty("server.request_threads"), Math.max(16, Runtime.getRuntime().availableProcessors() * 4));
        SERVER_PORT = Integer.parseInt(config.getProperty("server.port"));
        SERVER_IDLE_TIMEOUT = parseInteger(config.getProperty("server.idle_timeout"), 90);
        MAX_FRAME_LENGTH = parseInteger(config.getProperty("server.max_frame_length"), 32768);
        SERVER_WS_PORT = parseInteger(config.getProperty("server.ws_port"), 0);
        SERVER_WS_IP = config.getProperty("server.ws_ip", SERVER_IP).trim();
        WS_TRUSTED_PROXIES = java.util.Arrays.stream(config.getProperty("server.ws_trusted_proxies", "loopback").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        POLICY_ENABLED = Boolean.parseBoolean(config.getProperty("server.policy_enabled", "true"));
        POLICY_PORTS = config.getProperty("server.policy_ports", "*").trim();
        SERVER_NAME = config.getProperty("server.name");
        STAFF_ONLY = Boolean.parseBoolean(config.getProperty("server.staff_only"));

        DISCORD_BOT_TOKEN = config.getProperty("discord.bot.token");
    }

    private static Path getConfigPath() throws IOException {
//        String configName = System.getProperty("luminos.conf");
//        Path currentDir = Paths.get("").toAbsolutePath();
//        Path configDir = currentDir.resolve("conf");
//        Files.createDirectories(configDir);
//
//        return configDir.resolve(configName);

        File conf = new File("conf/luminos.conf");

        return conf.toPath();
    }

    private static int parseInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value '{}', using default: {}", value, defaultValue);

            return defaultValue;
        }
    }

}