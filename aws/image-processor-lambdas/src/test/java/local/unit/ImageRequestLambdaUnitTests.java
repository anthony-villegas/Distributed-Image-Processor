package local.unit;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lambdas.ImageRequestLambda;
import lambdas.daos.ProcessingTaskDao;
import lambdas.daos.RequestDao;
import lambdas.daos.SourceImageDao;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ImageRequestLambdaUnitTests {
    private Jdbi mockJdbi;
    private ObjectMapper mockObjectMapper;
    private RequestDao mockRequestDao;
    private SourceImageDao mockSourceImageDao;
    private ProcessingTaskDao mockProcessingTaskDao;
    private Context mockContext;
    private ImageRequestLambda lambda;
    private final String testJwt = "header.eyJjb2duaXRvOnVzZXJuYW1lIjoiVXNlcklkMTIzIn0.signature";
    private final String userId = "UserId123";
    private final String imageId = "imageId123";
    private final String action = "RESIZE";

    @BeforeEach
    public void setUp() {
        mockJdbi = mock(Jdbi.class);
        mockObjectMapper = mock(ObjectMapper.class);
        mockRequestDao = mock(RequestDao.class);
        mockSourceImageDao = mock(SourceImageDao.class);
        mockProcessingTaskDao = mock(ProcessingTaskDao.class);
        mockContext = mock(Context.class);
        lambda = new ImageRequestLambda(mockJdbi, new ObjectMapper());

        when(mockJdbi.onDemand(RequestDao.class)).thenReturn(mockRequestDao);
        when(mockJdbi.onDemand(SourceImageDao.class)).thenReturn(mockSourceImageDao);
        when(mockJdbi.onDemand(ProcessingTaskDao.class)).thenReturn(mockProcessingTaskDao);
    }

    @Test
    public void testHandleRequestSuccess() throws Exception {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + testJwt);
        requestEvent.setHeaders(headers);
        requestEvent.setBody("{\"imageId\": \"" + imageId + "\", \"action\": \"" + action + "\"}");

        when(mockContext.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));
        when(mockObjectMapper.readTree(any(String.class))).thenReturn(mock(JsonNode.class));
        when(mockJdbi.withExtension(eq(RequestDao.class), any())).thenReturn(1);

        APIGatewayProxyResponseEvent response = lambda.handleRequest(requestEvent, mockContext);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testHandleRequestException() throws Exception {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHeaders(new HashMap<>());
        requestEvent.setBody("{}");

        when(mockContext.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));
        doThrow(new RuntimeException("Test Exception")).when(mockJdbi).withExtension(eq(RequestDao.class), any());

        APIGatewayProxyResponseEvent response = lambda.handleRequest(requestEvent, mockContext);

        assertEquals(500, response.getStatusCode());
    }

    @Test
    public void testExtractUserIdFromJwt() throws Exception {
        String payload = new String(Base64.getUrlDecoder().decode("eyJjb2duaXRvOnVzZXJuYW1lIjoiVXNlcklkMTIzIn0"));
        JsonNode mockNode = mock(JsonNode.class);
        when(mockObjectMapper.readTree(payload)).thenReturn(mockNode);
        when(mockNode.path("cognito:username")).thenReturn(mock(JsonNode.class));
        when(mockNode.path("cognito:username").asText()).thenReturn(userId);

        String extractedUserId = lambda.extractUserIdFromJwt(testJwt);

        assertEquals(userId, extractedUserId);
    }
}
