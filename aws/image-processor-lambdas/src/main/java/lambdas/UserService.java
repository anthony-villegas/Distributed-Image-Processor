package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaxxer.hikari.HikariDataSource;
import lambdas.actions.UserAction;
import lambdas.helpers.DatabaseCredentialsManager;
import org.jdbi.v3.core.Jdbi;
import com.fasterxml.jackson.databind.ObjectMapper;
import schemas.CognitoEvent;
import schemas.User;
import static lambdas.helpers.DatabaseHelper.initializeHikari;

public class UserService implements RequestHandler<CognitoEvent, CognitoEvent>{
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

    public CognitoEvent handleRequest(CognitoEvent cognitoEvent, Context context) {
        try {
            context.getLogger().log("Handling CognitoEvent:\n" + cognitoEvent.toString());
            validateRequest(cognitoEvent);
            User user = deserializeUser(cognitoEvent);
            UserAction userAction = UserAction.createAction(cognitoEvent, jdbi);
            userAction.doAction(user, context);
            return cognitoEvent;
        }
        catch (Exception e) {
            context.getLogger().log("Error occurred: " + e.getMessage());
            throw e;
        }
    }

    private void validateRequest(CognitoEvent event) {
        if(event == null) {
            throw new IllegalArgumentException("Cognito Event is null");
        }
        if(event.getTriggerSource() == null || event.getTriggerSource().isEmpty()) {
            throw new IllegalArgumentException("Trigger Source is empty");
        }
        if(event.getUserName() == null || event.getUserName().isEmpty()) {
            throw new IllegalArgumentException("Username is empty");
        }
        if(event.getRequest().getUserAttributes() == null || event.getRequest().getUserAttributes().getOrDefault("email", "").isEmpty()) {
            throw new IllegalArgumentException("Email is empty");
        }
    }

    private User deserializeUser(CognitoEvent cognitoEvent) {
        User user = new User();
        user.setEmail(cognitoEvent.getRequest().getUserAttributes().get("email"));
        user.setUserID(cognitoEvent.getUserName());
        return user;
    }
}