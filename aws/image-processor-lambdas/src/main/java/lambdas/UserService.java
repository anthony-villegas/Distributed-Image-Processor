package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lambdas.actions.UserAction;
import lambdas.helpers.DatabaseCredentialsManager;
import org.jdbi.v3.core.Jdbi;
import schemas.CognitoEvent;
import lambdas.beans.UserBean;

public class UserService implements RequestHandler<CognitoEvent, CognitoEvent>{
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
            UserBean user = deserializeUser(cognitoEvent);
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

    private UserBean deserializeUser(CognitoEvent cognitoEvent) {
        UserBean user = new UserBean();
        user.setEmail(cognitoEvent.getRequest().getUserAttributes().get("email"));
        user.setId(cognitoEvent.getUserName());
        return user;
    }

    private static HikariDataSource initializeHikari(DatabaseCredentialsManager.Credentials credentials, String dbEndpoint, String dbName) {
        // Initialize HikariDataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbEndpoint + ":3306/" + dbName);
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
        return new HikariDataSource(config);
    }
}