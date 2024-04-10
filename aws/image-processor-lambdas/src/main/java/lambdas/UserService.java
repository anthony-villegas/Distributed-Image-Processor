package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import helpers.DatabaseCredentialsManager;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.core.Jdbi;

import schemas.User;

import java.util.List;

public class UserService implements RequestHandler<User, List<User>>{
    private static final String secretName = System.getenv("DB_SECRET_NAME");
    private static final HikariDataSource dataSource = initializeHikari(
            new DatabaseCredentialsManager(secretName).getSecretCredentials(),
            System.getenv("DB_ENDPOINT_ADDRESS"),
            System.getenv("DB_NAME")
    );
    private static final Jdbi jdbi = Jdbi.create(dataSource);

    public List<User> handleRequest(User input, Context context) {
        context.getLogger().log("Received username: " + input.getUsername());
        context.getLogger().log("\nReceived password: " + input.getPassword());

        // Create table and insert inputted value
        jdbi.useHandle(handle -> {
            handle.execute("CREATE TABLE user (username VARCHAR(50), password VARCHAR(50))");
            Update update = handle.createUpdate("INSERT INTO user (username, password) VALUES (:username, :password)");
            update.bind("username", input.getUsername());
            update.bind("password", input.getPassword());
            update.execute();
        });

        // Query table for inputted value
        List<User> users = jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT * FROM user")
                    .mapToBean(User.class)
                    .list();
        });

        return users;
    }

    private static HikariDataSource initializeHikari(DatabaseCredentialsManager.Credentials credentials, String dbEndpoint, String dbName) {
        // Initialize HikariDataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbEndpoint + ":3306/" + dbName);
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
        return new HikariDataSource(config);
    }
    public void cleanup() {
        dataSource.close(); // Close the HikariDataSource
    }
}