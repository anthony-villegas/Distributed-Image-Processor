package lambdas.actions;

import com.amazonaws.services.lambda.runtime.Context;
import lambdas.helpers.ErrorCode;
import org.jdbi.v3.core.Jdbi;
import schemas.CognitoEvent;
import lambdas.beans.UserBean;

public interface UserAction {
    void doAction(UserBean user, Context context);

    static UserAction createAction(CognitoEvent cognitoEvent, Jdbi jdbi) {
        switch (cognitoEvent.getTriggerSource()) {
            case "PostConfirmation_ConfirmSignUp":
                return new CreateUserAction(jdbi);
            default:
                throw new IllegalArgumentException(ErrorCode.INVALID_EVENT_SOURCE);
        }
    }
}
