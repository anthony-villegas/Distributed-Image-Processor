package lambdas.actions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.jdbi.v3.core.Jdbi;
import schemas.User;

public interface UserAction {
    APIGatewayProxyResponseEvent doAction(User user, Context context);

    static UserAction createAction(String httpMethod, Jdbi jdbi) {
        switch (httpMethod) {
            case "POST":
                return new CreateUserAction(jdbi);
            case "DELETE":
                return new DeleteUserAction(jdbi);
            default:
                throw new IllegalArgumentException("Invalid HTTP method");
        }
    }
}
