package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.zaxxer.hikari.HikariDataSource;
import lambdas.actions.UserAction;
import lambdas.helpers.DatabaseCredentialsManager;
import lambdas.helpers.ErrorCode;
import org.jdbi.v3.core.Jdbi;
import com.fasterxml.jackson.databind.ObjectMapper;
import schemas.User;
import java.io.IOException;

import static lambdas.helpers.ApiGatewayResponseHelper.createApiGatewayResponse;
import static lambdas.helpers.DatabaseHelper.initializeHikari;

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
        try {
            User user = deserializeUser(request.getBody());
            UserAction userAction = UserAction.createAction(request.getHttpMethod(), jdbi);
            return userAction.doAction(user, context);
        } catch (IOException e) {
            context.getLogger().log("Error deserializing user: " + e.getMessage());
            return createApiGatewayResponse(400, ErrorCode.INVALID_USER_FORMAT);
        } catch (IllegalArgumentException e) {
            return createApiGatewayResponse(400, ErrorCode.METHOD_NOT_ALLOWED);
        }
    }

    private User deserializeUser(String requestBody) throws IOException {
        return objectMapper.readValue(requestBody, User.class);
    }

}