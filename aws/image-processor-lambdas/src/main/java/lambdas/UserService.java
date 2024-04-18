package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import helpers.DatabaseCredentialsManager;
import helpers.ErrorCode;
import helpers.ResponseMessage;
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
            return createErrorResponse(400, ErrorCode.INVALID_USER_FORMAT);
        }

        switch (request.getHttpMethod()) {
            case "POST":
                return createUser(user, context);
            case "DELETE":
                return deleteUser(user, context);
            default:
                return createErrorResponse(400, ErrorCode.METHOD_NOT_ALLOWED);
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
            return createSuccessResponse(201, ResponseMessage.USER_CREATED_SUCCESSFULLY);
        } catch(Exception e) {
            context.getLogger().log("Error processing POST request" + e.getMessage());
            return createErrorResponse(500, ErrorCode.ERROR_PROCESSING_POST_REQUEST);
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
                return createErrorResponse(403, ErrorCode.USER_NOT_FOUND_OR_INCORRECT_PASSWORD);
            }

            // Delete the user
            jdbi.withHandle(handle ->
                handle.createUpdate("DELETE FROM user WHERE username = :username")
                        .bind("username", user.getUsername())
                        .execute()
            );

            return createSuccessResponse(200, ResponseMessage.USER_DELETED_SUCCESSFULLY);
        } catch(Exception e) {
            context.getLogger().log("Error processing DELETE request" + e.getMessage());
            return createErrorResponse(500, ErrorCode.ERROR_PROCESSING_DELETE_REQUEST);
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

    private void generateSchema() {
        // Create tables if not yet present
        jdbi.useHandle(handle -> {
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS user (" +
                            "UserID SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                            "Username VARCHAR(255) UNIQUE NOT NULL," +
                            "Password VARCHAR(255) NOT NULL" +
                            ");"
            );
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS image (" +
                            "UserID SMALLINT UNSIGNED NOT NULL," +
                            "ImageURL VARCHAR(1024) NOT NULL," +
                            "UploadDate TIMESTAMP NOT NULL," +
                            "CONSTRAINT pk_image PRIMARY KEY (ImageURL)," +
                            "CONSTRAINT fk_user_ID FOREIGN KEY (UserID) REFERENCES user(UserID)" +
                            ");"
            );
        });
    }
}