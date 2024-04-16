package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import helpers.DatabaseCredentialsManager;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.core.Jdbi;
import com.fasterxml.jackson.databind.ObjectMapper;

import schemas.User;

import java.util.Collections;

public class UserService implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static Jdbi jdbi;

    public UserService(Jdbi jdbi) {
        UserService.jdbi = jdbi;
    }

    public UserService() {
        String secretName = System.getenv("DB_SECRET_NAME");
        HikariDataSource dataSource = initializeHikari(
                new DatabaseCredentialsManager(secretName).getSecretCredentials(),
                System.getenv("DB_ENDPOINT_ADDRESS"),
                System.getenv("DB_NAME")
        );
        UserService.jdbi = Jdbi.create(dataSource);
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context){
        User user;

        try {
             user = objectMapper.readValue(request.getBody(), User.class);
        } catch (Exception e) {
            context.getLogger().log("Error deserializing user: " + e.getMessage());
            return createErrorResponse(400, "Invalid user format");
        }

        switch (request.getHttpMethod()) {
            case "POST":
                return createUser(user, context);
            case "DELETE":
                return deleteUser(user, context);
            default:
                return createErrorResponse(400, "Method not allowed");
        }
    }

    private APIGatewayProxyResponseEvent createUser(User user, Context context) {
        try {
            context.getLogger().log("Creating user: " + user.getUsername());
            generateSchema();
            jdbi.useHandle(handle -> {
                Update update = handle.createUpdate("INSERT INTO user (Username, Password) VALUES (:username, :password)");
                update.bind("username", user.getUsername());
                update.bind("password", user.getPassword());
                update.execute();
            });
            return createSuccessResponse(201, "User created successfully");
        } catch(Exception e) {
            context.getLogger().log("Error processing POST request" + e.getMessage());
            return createErrorResponse(500, "Error processing POST request");
        }
    }

    private APIGatewayProxyResponseEvent deleteUser(User user, Context context) {
        try {
            // Check if the user exists and verify the password
            boolean userExistsAndPasswordCorrect = jdbi.withHandle(handle ->
                    handle.createQuery("SELECT COUNT(*) FROM user WHERE username = :username AND password = :password")
                            .bind("username", user.getUsername())
                            .bind("password", user.getPassword())
                            .mapTo(Integer.class)
                            .findFirst()
                            .orElse(0) > 0
            );

            if (!userExistsAndPasswordCorrect) {
                return createErrorResponse(403, "User not found or incorrect password");
            }

            // Delete the user
            jdbi.withHandle(handle ->
                handle.createUpdate("DELETE FROM user WHERE username = :username")
                        .bind("username", user.getUsername())
                        .execute()
            );

            return createSuccessResponse(200, "User deleted successfully");
        } catch(Exception e) {
            context.getLogger().log("Error processing DELETE request" + e.getMessage());
            return createErrorResponse(500, "Error processing DELETE request");
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, String message) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Collections.singletonMap("Content-Type", "text/plain"))
                .withBody(message);
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String errorMessage) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Collections.singletonMap("Content-Type", "text/plain"))
                .withBody(errorMessage);
    }

    private static HikariDataSource initializeHikari(DatabaseCredentialsManager.Credentials credentials, String dbEndpoint, String dbName) {
        // Initialize HikariDataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbEndpoint + ":3306/" + dbName);
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
        return new HikariDataSource(config);
    }

    private boolean isValidUser(User user) {
        // Perform validation logic here
        if (user.getUsername() == null || user.getPassword() == null) {
            return false;
        }

        return true;
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
}