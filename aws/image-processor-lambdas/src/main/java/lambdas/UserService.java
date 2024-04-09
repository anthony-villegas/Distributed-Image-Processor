package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import helpers.DatabaseCredentialsManager;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.core.Jdbi;

import schemas.User;

import javax.sql.DataSource;
import java.util.List;

public class UserService implements RequestHandler<User, List<User>>{
    private final String dbUrl = System.getenv("DB_ENDPOINT_ADDRESS");
    private final String secretName = System.getenv("DB_SECRET_NAME");

    private final DatabaseCredentialsManager credentialsManager = new DatabaseCredentialsManager(secretName);

    public List<User> handleRequest(User input, Context context) {

        // Retrieve database credentials from AWS Secrets Manager
        DatabaseCredentialsManager.Credentials credentials = credentialsManager.getSecretCredentials();

        Jdbi jdbi = Jdbi.create(dbUrl, credentials.username(), credentials.password());

        context.getLogger().log("Received username: " + input.getUsername());
        context.getLogger().log("Received password: " + input.getPassword());

        // Create table and insert inputted value
        jdbi.useHandle(handle -> {
            handle.execute("CREATE TABLE users (username VARCHAR(50), password VARCHAR(50))");
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
}