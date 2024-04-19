package helpers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public interface DatabaseHelper {
    public static HikariDataSource initializeHikari(DatabaseCredentialsManager.Credentials credentials, String dbEndpoint, String dbName) {
        // Initialize HikariDataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbEndpoint + ":3306/" + dbName);
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
        return new HikariDataSource(config);
    }

}
