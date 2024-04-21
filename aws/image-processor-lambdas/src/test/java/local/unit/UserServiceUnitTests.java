package local.unit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lambdas.helpers.ErrorCode;
import lambdas.helpers.ResponseMessage;
import lambdas.UserService;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import resources.MockLambdaLogger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceUnitTests {
    private final String USER1 = "USER1";
    private final String USER2 = "USER2";
    private final String PASSWORD1 = "PASSWORD1";
    private final String PASSWORD2 = "PASSWORD2";
    private final String POST = "POST";
    private final String DELETE = "DELETE";
    private final String PUT = "PUT";
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

        // Create a request with HTTP method POST and user data
        APIGatewayProxyRequestEvent request = createUserRequest(POST, USER1, PASSWORD1);

        // Invoke the handleRequest method
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, contextMock);

        // Verify that the user was created successfully
        assertEquals(201, response.getStatusCode());
        assertEquals(ResponseMessage.USER_CREATED_SUCCESSFULLY, response.getBody());
    }

    @Test
    public void testCreateUser_InvalidUserFormat() {
        // Inject the mocked Jdbi instance into UserService
        UserService userService = new UserService(jdbi);

        // Mock the Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation with invalid user data
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("POST");
        request.setBody("{\"invalid\":\"data\"}");

        // Invoke user creation
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the response indicates an error due to invalid user format
        assertEquals(400, response.getStatusCode());
        assertEquals(ErrorCode.INVALID_USER_FORMAT, response.getBody());
    }

    @Test
    public void testDeleteUser_Success() {
        // Inject the mocked Jdbi instance into UserService
        UserService userService = new UserService(jdbi);

        // Mock the Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user deletion
        APIGatewayProxyRequestEvent request = createUserRequest("DELETE", "existingUser", "correctPassword");

        // Mock database interaction to simulate user existence and correct password verification
        when(jdbi.withHandle(any())).thenAnswer(invocation -> true);

        // Invoke user deletion
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the response indicates successful user deletion
        assertEquals(200, response.getStatusCode());
        assertEquals(ResponseMessage.USER_DELETED_SUCCESSFULLY, response.getBody());
    }

    @Test
    public void testDeleteUser_UserNotFoundOrIncorrectPassword() {
        // Inject the mocked Jdbi instance into UserService
        UserService userService = new UserService(jdbi);

        // Mock the Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user deletion
        APIGatewayProxyRequestEvent request = createUserRequest("DELETE", "nonexistentUser", "wrongPassword");

        // Mock database interaction to simulate user not found or incorrect password
        when(jdbi.withHandle(any())).thenAnswer(invocation -> false);

        // Invoke user deletion
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the response indicates an error due to user not found or incorrect password
        assertEquals(403, response.getStatusCode());
        assertEquals(ErrorCode.USER_NOT_FOUND_OR_INCORRECT_PASSWORD, response.getBody());
    }

    @Test
    public void testUserService_InvalidMethod() {
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        APIGatewayProxyRequestEvent request = createUserRequest(PUT, USER1, PASSWORD1);

        // Invoke user creation
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that invalid method was gracefully rejected
        assertEquals(400, response.getStatusCode());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED, response.getBody());
    }

    private APIGatewayProxyRequestEvent createUserRequest(String httpMethod, String username, String password) {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod(httpMethod);
        request.setBody(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password));
        return request;
    }
}
