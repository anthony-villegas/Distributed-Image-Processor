package lambdas.actions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lambdas.helpers.ErrorCode;
import lambdas.helpers.ResponseMessage;
import lambdas.helpers.UserNotFoundException;
import org.jdbi.v3.core.Jdbi;
import schemas.User;

import static lambdas.helpers.ApiGatewayResponseHelper.createApiGatewayResponse;

public class DeleteUserAction implements UserAction {
    private final Jdbi jdbi;

    public DeleteUserAction(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    @Override
    public APIGatewayProxyResponseEvent doAction(User user, Context context) {
        try {
            deleteUserInDatabase(user, context);
            return createApiGatewayResponse(200, ResponseMessage.USER_DELETED_SUCCESSFULLY);
        } catch(UserNotFoundException e) {
            return createApiGatewayResponse(403, ErrorCode.USER_NOT_FOUND_OR_INCORRECT_PASSWORD);
        } catch(Exception e) {
            context.getLogger().log("Error processing DELETE request" + e.getMessage());
            return createApiGatewayResponse(500, ErrorCode.ERROR_PROCESSING_DELETE_REQUEST);
        }
    }

    private void deleteUserInDatabase(User user, Context context) throws UserNotFoundException {
        context.getLogger().log("Deleting user: " + user.getUsername());
        boolean userExistsAndPasswordCorrect = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM user WHERE username = :username AND password = :password")
                        .bind("username", user.getUsername())
                        .bind("password", user.getPassword())
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
        if (!userExistsAndPasswordCorrect) {
            throw new UserNotFoundException();
        }
        jdbi.withHandle(handle ->
                handle.createUpdate("DELETE FROM user WHERE username = :username AND password = :password")
                        .bind("username", user.getUsername())
                        .bind("password", user.getPassword())
                        .execute()
        );
    }
}
