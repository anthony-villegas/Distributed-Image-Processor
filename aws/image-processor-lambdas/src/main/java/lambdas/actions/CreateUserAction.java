package lambdas.actions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lambdas.helpers.ErrorCode;
import lambdas.helpers.ResponseMessage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import schemas.User;

import static lambdas.helpers.ApiGatewayResponseHelper.createApiGatewayResponse;

public class CreateUserAction implements UserAction {
    private final Jdbi jdbi;

    public CreateUserAction(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    @Override
    public APIGatewayProxyResponseEvent doAction(User user, Context context) {
        try {
            createUserInDatabase(user, context);
            return createApiGatewayResponse(201, ResponseMessage.USER_CREATED_SUCCESSFULLY);
        } catch(Exception e) {
            context.getLogger().log("Error processing POST request" + e.getMessage());
            return createApiGatewayResponse(500, ErrorCode.ERROR_PROCESSING_POST_REQUEST);
        }
    }

    private void createUserInDatabase(User user, Context context) {
        context.getLogger().log("Creating user: " + user.getUsername());
        generateSchema();
        jdbi.useHandle(handle -> {
            Update update = handle.createUpdate("INSERT INTO user (Username, Password) VALUES (:username, :password)");
            update.bind("username", user.getUsername());
            update.bind("password", user.getPassword());
            update.execute();
        });
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
                            "UserID SMALLINT UNSIGNED PRIMARY KEY NOT NULL," +
                            "ImageURL VARCHAR(1024) NOT NULL," +
                            "UploadDate TIMESTAMP NOT NULL," +
                            "FOREIGN KEY (UserID) REFERENCES user(UserID)" +
                            ");"
            );
        });
    }
}
