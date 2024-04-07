package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import helpers.DatabaseCredentialsManager;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.json.JSONObject;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class UserService implements RequestHandler<JSONObject, List>{
    private final String dbUrl = System.getenv("DB_ENDPOINT_ADDRESS");
    private final String secretArn = System.getenv("DB_SECRET_ARN");

    private final DatabaseCredentialsManager credentialsManager = new DatabaseCredentialsManager(secretArn);

    public class User {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
    public List handleRequest(JSONObject input, Context context) {

        // Retrieve database credentials from AWS Secrets Manager
        DatabaseCredentialsManager.Credentials credentials = credentialsManager.getSecretCredentials();

        Jdbi jdbi = Jdbi.create(dbUrl, credentials.username(), credentials.password());

        // Extract inputted username and password
        String username = input.getString("username");
        String password = input.getString("password");

        context.getLogger().log("Received username: " + username);
        context.getLogger().log("Received password: " + password);

        // Create table and insert inputted value
        jdbi.useHandle(handle -> {
            handle.execute("CREATE TABLE users (username VARCHAR(50), password VARCHAR(50))");
            Update update = handle.createUpdate("INSERT INTO user (username, password) VALUES (:username, :password)");
            update.bind("username", username);
            update.bind("password", password);
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