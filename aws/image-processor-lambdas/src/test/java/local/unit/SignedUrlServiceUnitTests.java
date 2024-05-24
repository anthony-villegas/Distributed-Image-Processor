package local.unit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lambdas.SignedUrlService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import resources.MockLambdaLogger;
import lambdas.helpers.SignedUrlGenerator;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SignedUrlServiceUnitTests {
    private static final Context contextMock = mock(Context.class);
    private static SignedUrlGenerator generator;
    private static final String bucketName = "test-bucket";
    @BeforeAll
    public static void setUp() {
        when(contextMock.getLogger()).thenReturn(new MockLambdaLogger());
        generator = new SignedUrlGenerator(bucketName, S3Presigner.create());
    }

    @Test
    public void testHandleRequest_Success() {
        // Prepare mock request
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        // Invoke handler method
        SignedUrlService signedUrlService = new SignedUrlService(generator);
        APIGatewayProxyResponseEvent response = signedUrlService.handleRequest(request, contextMock);

        // Assert the response
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testHandleRequest_ReturnFailureOnException() {
        // Prepare mock request
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        SignedUrlGenerator generatorMock = mock(SignedUrlGenerator.class);
        when(generatorMock.createSignedUrl(any(LambdaLogger.class))).thenThrow(RuntimeException.class);

        // Invoke handler method
        SignedUrlService signedUrlService = new SignedUrlService(generatorMock);
        APIGatewayProxyResponseEvent response = signedUrlService.handleRequest(request, contextMock);

        // Should return a failure status code on exception in signed url creation
        assertEquals(500, response.getStatusCode());
    }
}
