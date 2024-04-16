package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private static Jdbi jdbi;
    private final String USER1 = "USER1";
    private final String USER2 = "USER2";
    private final String PASSWORD1 = "PASSWORD1";
    private final String PASSWORD2 = "PASSWORD2";
    private final String POST = "POST";
    private final String DELETE = "DELETE";
    private final String PUT = "PUT";

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
        APIGatewayProxyRequestEvent request = createUserRequest(POST, USER1, PASSWORD1);

        // Invoke user creation
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the user was created successfully
        assertEquals(201, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
    }

    @Test
    public void testCreateUser_DuplicateUsers() {
        // Inject the in-memory database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        APIGatewayProxyRequestEvent request = createUserRequest(POST, USER1, PASSWORD1);

        // Invoke user creation
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the user was created successfully
        assertEquals(201, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());

        // Try to create the same user again
        response = userService.handleRequest(request, context);
        assertEquals(500, response.getStatusCode());
        assertEquals("Error processing POST request", response.getBody());
    }

    @Test
    public void testCreateUser_MultipleUsers() {
        // Inject the in-memory database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        APIGatewayProxyRequestEvent request = createUserRequest(POST, USER1, PASSWORD1);

        // Invoke user creation
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the user was created successfully
        assertEquals(201, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());

        // Create a different user
        APIGatewayProxyRequestEvent request2 = createUserRequest(POST, USER2, PASSWORD1);
        APIGatewayProxyResponseEvent response2 = userService.handleRequest(request2, context);

        // Verify that the user was created successfully
        assertEquals(201, response2.getStatusCode());
        assertEquals("User created successfully", response2.getBody());
    }

    @Test
    public void testDeleteUser_Success() {
        // Inject the in-memory database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        APIGatewayProxyRequestEvent request = createUserRequest(POST, USER1, PASSWORD1);

        // Invoke user creation
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the user was created successfully
        assertEquals(201, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());

        // Define request to delete created user
        request.setHttpMethod(DELETE);

        // Invoke deletion
        response = userService.handleRequest(request, context);
        assertEquals(200, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    public void testDeleteUser_UserNotFound() {
        // Inject the in-memory database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user deletion
        APIGatewayProxyRequestEvent request = createUserRequest(DELETE, USER1, PASSWORD1);

        // Invoke user deletion
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // User deletion fails due to user not existing
        assertEquals(403, response.getStatusCode());
        assertEquals("User not found or incorrect password", response.getBody());
    }

    @Test
    public void testDeleteUser_WrongPassword() {
        // Inject database connection into UserService
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for user creation
        APIGatewayProxyRequestEvent request = createUserRequest(POST, USER1, PASSWORD1);

        // Invoke user creation
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the user was created successfully
        assertEquals(201, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());

        // Define request to delete created user but with wrong password
        request = createUserRequest(DELETE, USER1,PASSWORD2);

        // Invoke deletion
        response = userService.handleRequest(request, context);
        assertEquals(403, response.getStatusCode());
        assertEquals("User not found or incorrect password", response.getBody());
    }

    @Test
    public void testDeserializeUser_ValidRequest() {
        // Create a UserService instance
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Create a JSON request with a valid user object
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USER1, PASSWORD1));
        request.setHttpMethod(POST);

        // Invoke the handleRequest method to deserialize the request
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the deserialization was successful
        assertEquals(201, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
    }

    @Test
    public void testDeserializeUser_InvalidRequest() {
        // Create a UserService instance
        UserService userService = new UserService(jdbi);

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Create a JSON request with invalid data
        String requestBody = "{\"invalid\":\"data\"}";
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(requestBody);
        request.setHttpMethod(POST);

        // Invoke the handleRequest method to deserialize the request
        APIGatewayProxyResponseEvent response = userService.handleRequest(request, context);

        // Verify that the deserialization failed gracefully
        assertEquals(400, response.getStatusCode());
        assertEquals("Invalid user format", response.getBody());
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
        assertEquals("Method not allowed", response.getBody());
    }

    private APIGatewayProxyRequestEvent createUserRequest(String httpMethod, String username, String password) {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod(httpMethod);
        request.setBody(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password));
        return request;
    }
    public static class MockLambdaLogger implements LambdaLogger {
        @Override
        public void log(String message) {}
        @Override
        public void log(byte[] bytes) {}
    }
}
