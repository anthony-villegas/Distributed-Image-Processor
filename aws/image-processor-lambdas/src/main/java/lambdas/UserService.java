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
        context.getLogger().log("Creating user: " + input.getUsername());
        generateSchema();

        // Insert inputted value
        jdbi.useHandle(handle -> {
            Update update = handle.createUpdate("INSERT INTO user (Username, Password) VALUES (:username, :password)");
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

    private void generateSchema() {
        // Create tables if not yet present
        jdbi.useHandle(handle -> {
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS user (" +
                            "UserID INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                            "Username VARCHAR(255) UNIQUE NOT NULL," +
                            "Password VARCHAR(255) NOT NULL" +
                            ");"
            );
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS image (" +
                            "ImageID INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                            "UserID INT NOT NULL," +
                            "ImageURL VARCHAR(1024) NOT NULL," +
                            "UploadDate TIMESTAMP NOT NULL," +
                            "FOREIGN KEY (UserID) REFERENCES user(UserID)" +
                            ");"
            );
        });
    }
    public void cleanup() {
        dataSource.close(); // Close the HikariDataSource
    }
}