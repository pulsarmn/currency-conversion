package org.pulsar.currency;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DataSourceFactory {

    private static HikariDataSource dataSource;

    private static final String DEFAULT_PROPERTIES_FILE = "datasource.properties";

    private DataSourceFactory() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DataSourceFactory.class) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }

    private static HikariDataSource createDataSource() {
        HikariConfig config = createConfig();
        return new HikariDataSource(config);
    }

    private static HikariConfig createConfig() {
        HikariConfig config = new HikariConfig();
        Properties properties = getProperties();

        config.setJdbcUrl(properties.getProperty("url"));
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));
        config.setDriverClassName(properties.getProperty("driver"));

        return config;
    }

    private static Properties getProperties() {
        Properties properties = new Properties();

        try(InputStream fileStream = DataSourceFactory.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            properties.load(fileStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
