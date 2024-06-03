package local.integration;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import lambdas.ImageRequestLambda;
import lambdas.daos.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import resources.MockLambdaLogger;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageRequestLambdaLocalIntegrationTests {
    private final String testJwt = "header.eyJjb2duaXRvOnVzZXJuYW1lIjoiVXNlcklkMTIzIn0.signature";
    private static final String userId = "UserId123";
    private final String imageId = "imageId123";
    private final String action = "RESIZE";
    private static Jdbi jdbi;

    @BeforeAll
    public static void setUp() {
        // Initialize an in-memory H2 database
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");

        // Initialize Jdbi with the H2 database
        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        // Insert user for test
        jdbi.useExtension(UserDao.class, dao -> {
            dao.createTable();
            dao.insert(userId, "email@gmail.com");
        });
    }

    @AfterEach
    public void cleanDB() {
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS processing_task");
            handle.execute("DROP TABLE IF EXISTS source_image");
            handle.execute("DROP TABLE IF EXISTS request");
        });
    }

    @AfterAll
    public static void resetDB() {
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS user");
        });
    }
    @Test
    public void testHandleRequest_Success() throws Exception {
        // Create an instance of ImageRequestLambda with the in-memory database and ObjectMapper
        ImageRequestLambda lambda = new ImageRequestLambda(jdbi, new ObjectMapper());

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for image processing
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + testJwt);
        requestEvent.setHeaders(headers);
        requestEvent.setBody("{\"imageId\": \"" + imageId + "\", \"action\": \"" + action + "\"}");

        // Invoke the Lambda function
        APIGatewayProxyResponseEvent response = lambda.handleRequest(requestEvent, context);

        // Verify response status code
        assertEquals(200, response.getStatusCode());

        // Verify entries in the database
        boolean requestExists = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM request WHERE userId = :userId")
                        .bind("userId", userId)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
        assertTrue(requestExists);

        boolean sourceImageExists = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM source_image WHERE id = :id")
                        .bind("id", imageId)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
        assertTrue(sourceImageExists);

        boolean processingTaskExists = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM processing_task WHERE imageId = :imageId AND action = :action")
                        .bind("imageId", imageId)
                        .bind("action", action)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
        assertTrue(processingTaskExists);
    }

    @Test
    public void testHandleRequest_Exception() {
        // Create an instance of ImageRequestLambda with the in-memory database and ObjectMapper
        ImageRequestLambda lambda = new ImageRequestLambda(jdbi, new ObjectMapper());

        // Mock Context
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(new MockLambdaLogger());

        // Define request for image processing with invalid JSON body
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + testJwt);
        requestEvent.setHeaders(headers);
        requestEvent.setBody("invalid-json");

        // Invoke the Lambda function
        APIGatewayProxyResponseEvent response = lambda.handleRequest(requestEvent, context);

        // Verify response status code
        assertEquals(500, response.getStatusCode());
    }
}