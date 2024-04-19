package local.integration;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.zaxxer.hikari.HikariDataSource;
import helpers.ErrorCode;
import helpers.ResponseMessage;
import lambdas.UserService;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import resources.MockLambdaLogger;
import resources.UserServiceResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceLocalIntegrationTests {
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
        assertEquals(ResponseMessage.USER_CREATED_SUCCESSFULLY, response.getBody());

        // Verify user is present in database
        boolean userExistsAndPasswordCorrect = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM user WHERE username = :username AND password = :password")
                        .bind("username", USER1)
                        .bind("password", PASSWORD1)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
        assertTrue(userExistsAndPasswordCorrect);
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
        assertEquals(ResponseMessage.USER_CREATED_SUCCESSFULLY, response.getBody());

        // Try to create the same user again
        response = userService.handleRequest(request, context);
        assertEquals(500, response.getStatusCode());
        assertEquals(ErrorCode.ERROR_PROCESSING_POST_REQUEST, response.getBody());

        // Verify only single user with given credentials is present
        int numUsers = jdbi.withHandle(handle -> {
                return handle.createQuery("SELECT COUNT(*) FROM user WHERE username = :username AND password = :password")
                        .bind("username", USER1)
                        .bind("password", PASSWORD1)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0);
                }
        );
        assertEquals(1, numUsers);
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
        assertEquals(ResponseMessage.USER_CREATED_SUCCESSFULLY, response.getBody());

        // Create a different user
        APIGatewayProxyRequestEvent request2 = createUserRequest(POST, USER2, PASSWORD1);
        APIGatewayProxyResponseEvent response2 = userService.handleRequest(request2, context);

        // Verify that the user was created successfully
        assertEquals(201, response2.getStatusCode());
        assertEquals(ResponseMessage.USER_CREATED_SUCCESSFULLY, response2.getBody());

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
        assertEquals(ResponseMessage.USER_CREATED_SUCCESSFULLY, response.getBody());

        // Define request to delete created user
        request.setHttpMethod(DELETE);

        // Invoke deletion
        response = userService.handleRequest(request, context);
        assertEquals(200, response.getStatusCode());
        assertEquals(ResponseMessage.USER_DELETED_SUCCESSFULLY, response.getBody());

        // Verify user has been removed
        boolean userExistsAndPasswordCorrect = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM user WHERE username = :username AND password = :password")
                        .bind("username", USER1)
                        .bind("password", PASSWORD1)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
        assertFalse(userExistsAndPasswordCorrect);
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
        assertEquals(ErrorCode.USER_NOT_FOUND_OR_INCORRECT_PASSWORD, response.getBody());
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
        assertEquals(ResponseMessage.USER_CREATED_SUCCESSFULLY, response.getBody());

        // Define request to delete created user but with wrong password
        request = createUserRequest(DELETE, USER1,PASSWORD2);

        // Invoke deletion
        response = userService.handleRequest(request, context);
        assertEquals(403, response.getStatusCode());
        assertEquals(ErrorCode.USER_NOT_FOUND_OR_INCORRECT_PASSWORD, response.getBody());

        // Verify user has not been deleted
        int numUsers = jdbi.withHandle(handle -> {
                    return handle.createQuery("SELECT COUNT(*) FROM user WHERE username = :username AND password = :password")
                            .bind("username", USER1)
                            .bind("password", PASSWORD1)
                            .mapTo(Integer.class)
                            .findFirst()
                            .orElse(0);
                }
        );
        assertEquals(1, numUsers);
    }

    private APIGatewayProxyRequestEvent createUserRequest(String httpMethod, String username, String password) {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod(httpMethod);
        request.setBody(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password));
        return request;
    }
}
