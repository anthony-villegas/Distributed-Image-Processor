package local.integration;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.zaxxer.hikari.HikariDataSource;
import lambdas.helpers.ErrorCode;
import lambdas.helpers.ResponseMessage;
import lambdas.UserService;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import resources.MockLambdaLogger;
import schemas.CognitoEvent;
import schemas.Request;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceLocalIntegrationTests {
    private final String EMAIL1 = "email1@gmail.com";
    private final String EMAIL2 = "email2@gmail.com";
    private final String USER1 = "f784faf8-70a1-704d-bc25-34fc6853ae23";
    private final String USER2 = "f784faf8-70a1-704d-bc25-34fc6853ae24";
    private final String POST_CONFIRMATION = "PostConfirmation_ConfirmSignUp";
    private static Jdbi jdbi;

    @BeforeAll
    public static void setUp() {
        // Initialize an in-memory H2 database
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");

        // Initialize Jdbi with the H2 database
        jdbi = Jdbi.create(dataSource);
    }

    @AfterEach
    public void cleanDB() {
        jdbi.useHandle(handle -> {
            handle.execute("DELETE FROM image");
            handle.execute("DELETE FROM user");
        });
    }

    @Test
    public void testCreateUser_Success() {
        // Inject the in-memory database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        CognitoEvent request = createCognitoEvent(POST_CONFIRMATION, EMAIL1, USER1);

        // Invoke user creation
        userService.handleRequest(request, context);

        // Verify user is present in database
        boolean userExistsAndEmailCorrect = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM user WHERE UserID = :user_id AND Email = :email")
                        .bind("user_id", USER1)
                        .bind("email", EMAIL1)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
        assertTrue(userExistsAndEmailCorrect);
    }

    @Test
    public void testCreateUser_Failure_DuplicateUsers() {
        // Inject the in-memory database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        CognitoEvent request = createCognitoEvent(POST_CONFIRMATION, EMAIL1, USER1);

        // Invoke user creation
        userService.handleRequest(request, context);

        try {
            // Creating same user again should throw exception
            userService.handleRequest(request, context);
            fail();
        } catch(Exception e) {
            // Verify only single user with given credentials is present
            int numUsers = jdbi.withHandle(handle -> {
                        return handle.createQuery("SELECT COUNT(*) FROM user WHERE UserID = :user_id AND Email = :email")
                                .bind("user_id", USER1)
                                .bind("email", EMAIL1)
                                .mapTo(Integer.class)
                                .findFirst()
                                .orElse(0);
                    }
            );
            assertEquals(1, numUsers);
        }
    }

    @Test
    public void testCreateUser_Success_MultipleUsers() {
        // Inject the in-memory database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        CognitoEvent request = createCognitoEvent(POST_CONFIRMATION, EMAIL1, USER1);

        // Invoke user creation
        userService.handleRequest(request, context);

        // Create a different user
        CognitoEvent request2 = createCognitoEvent(POST_CONFIRMATION, EMAIL2, USER2);
        userService.handleRequest(request2, context);

        // Verify two users are present
        int numUsers = jdbi.withHandle(handle -> {
                    return handle.createQuery("SELECT COUNT(*) FROM user")
                            .mapTo(Integer.class)
                            .findFirst()
                            .orElse(0);
                }
        );
        assertEquals(2, numUsers);
    }

    private CognitoEvent createCognitoEvent(String triggerSource, String email, String username) {
        CognitoEvent cognitoEvent = new CognitoEvent();
        cognitoEvent.setTriggerSource(triggerSource);
        cognitoEvent.setUserName(username);
        Request request = new Request();
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("email", email);
        request.setUserAttributes(attributeMap);
        cognitoEvent.setRequest(request);
        return cognitoEvent;
    }
}
