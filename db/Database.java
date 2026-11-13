package ains.db;

import ains.config.ConfigData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jdbchelper.JdbcHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {

    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private final JdbcHelper jdbc;
    private final HikariDataSource dataSource;

    public Database() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(ConfigData.DB_URL);
        config.setUsername(ConfigData.DB_USERNAME);
        config.setPassword(ConfigData.DB_PASSWORD);

        config.setMaximumPoolSize(ConfigData.MAXIMUM_POOL_SIZE);
        config.setConnectionTimeout(ConfigData.CONNECTION_TIMEOUT);
        config.setLeakDetectionThreshold(ConfigData.LEAK_DETECTION_THRESHOLD);
        config.setMinimumIdle(ConfigData.MINIMUM_IDLE);
        config.setIdleTimeout(ConfigData.IDLE_TIMEOUT);
        config.setMaxLifetime(ConfigData.MAX_LIFE_TIME);
        config.setAutoCommit(true);

        config.addDataSourceProperty("cachePrepStmts", ConfigData.CACHE_PRERP_STMTS);
        config.addDataSourceProperty("prepStmtCacheSize", ConfigData.PREP_STMT_CACHE_SIZE);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", ConfigData.PREP_STMT_CACHE_SQL_LIMIT);
        config.addDataSourceProperty("useSSL", ConfigData.USE_SSL);

        dataSource = new HikariDataSource(config);
        jdbc = new JdbcHelper(dataSource);

        log.info("Database connections initialized.");
    }

    public JdbcHelper getJdbc() {
        return jdbc;
    }

    /** Raw pool access for work JdbcHelper cannot do, such as JDBC batch inserts. */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void destroy() {
        try {
            log.info("Database connections destroyed.");
        } catch (Exception ex) {
            log.error("Error disposing connection pool: {}", ex.getMessage());
        }
    }

}
