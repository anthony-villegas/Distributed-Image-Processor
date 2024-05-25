package local.unit;

import com.amazonaws.services.lambda.runtime.Context;
import lambdas.UserService;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import resources.MockLambdaLogger;
import schemas.CognitoEvent;
import schemas.Request;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceUnitTests {
    private final String EMAIL1 = "email1@gmail.com";
    private final String EMAIL2 = "email2@gmail.com";
    private final String USER1 = "f784faf8-70a1-704d-bc25-34fc6853ae23";
    private final String USER2 = "f784faf8-70a1-704d-bc25-34fc6853ae24";
    private final String POST_CONFIRMATION = "PostConfirmation_ConfirmSignUp";
    private final String PRE_CONFIRMATION = "PreConfirmation_ConfirmSignUp";

    private static Jdbi jdbi;

    @BeforeAll
    public static void setUp() {
        // Initialize Jdbi mock
        jdbi = mock(Jdbi.class);
    }
    @Test
    void testHandleRequest_CreateUser_Success() {
        // Create a mock context
        Context contextMock = mock(Context.class);
        when(contextMock.getLogger()).thenReturn(new MockLambdaLogger());

        // Create a UserService instance with the mocked Jdbi
        UserService userService = new UserService(jdbi);

        // Mock the database interaction
        when(jdbi.withHandle(any())).thenAnswer(invocation -> true);

        // Create a post confirmation CognitoEven with user data
        CognitoEvent request = createCognitoEvent(POST_CONFIRMATION, EMAIL1, USER1);

        // Invoke the handleRequest method
        CognitoEvent response = userService.handleRequest(request, contextMock);
    }

    @Test
    public void testUserService_InvalidMethod() {
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Create an invalid CognitoEvent with user data
        CognitoEvent request = createCognitoEvent(PRE_CONFIRMATION, EMAIL1, USER1);

        try {
            // Invoke user creation, this should fail
            userService.handleRequest(request, context);
            fail();
        } catch (Exception ignored) {}
    }

    @Test
    public void testUserService_NullInput() {
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        try {
            // Invoke user creation with null CognitoEven should fail
            userService.handleRequest(null, context);
            fail();
        } catch (Exception ignored) {}
    }

    @Test
    public void testUserService_EmptyInput() {
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        try {
            // Invoke user creation with empty CognitoEvent should fail
            userService.handleRequest(new CognitoEvent(), context);
            fail();
        } catch (Exception ignored) {}
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
