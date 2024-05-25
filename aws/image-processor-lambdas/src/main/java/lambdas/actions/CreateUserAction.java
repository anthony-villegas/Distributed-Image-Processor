package lambdas.actions;

import com.amazonaws.services.lambda.runtime.Context;
import lambdas.helpers.ErrorCode;
import lambdas.helpers.ResponseMessage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import schemas.User;

public class CreateUserAction implements UserAction {
    private final Jdbi jdbi;

    public CreateUserAction(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    @Override
    public void doAction(User user, Context context) {
        createUserInDatabase(user, context);
        context.getLogger().log(ResponseMessage.USER_CREATED_SUCCESSFULLY);
    }

    private void createUserInDatabase(User user, Context context) {
        context.getLogger().log("Creating user: " + user.getUserID());
        generateSchema();
        jdbi.useHandle(handle -> {
            Update update = handle.createUpdate("INSERT INTO user (UserID, Email, CreationDate) VALUES (:user_id, :email, CURDATE())");
            update.bind("user_id", user.getUserID());
            update.bind("email", user.getEmail());
            update.execute();
        });
    }

    private void generateSchema() {
        // Create tables if not yet present
        jdbi.useHandle(handle -> {
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS user (" +
                            "UserID VARCHAR(2048) PRIMARY KEY NOT NULL," +
                            "Email VARCHAR(255) UNIQUE NOT NULL," +
                            "CreationDate TIMESTAMP NOT NULL" +
                            ");"
            );
            handle.execute(
                    "CREATE TABLE IF NOT EXISTS image (" +
                            "ImageID SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                            "UserID VARCHAR(2048) NOT NULL," +
                            "ImageURL VARCHAR(1024) NOT NULL," +
                            "UploadDate TIMESTAMP NOT NULL," +
                            "FOREIGN KEY (UserID) REFERENCES user(UserID)" +
                            ");"
            );
        });
    }
}
