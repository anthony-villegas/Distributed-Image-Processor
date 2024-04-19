package cloud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import resources.UserServiceResponse;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIntegrationTests {
    private static final String functionName = "UserService";

    private static LambdaClient awsLambda = LambdaClient.builder()
            .region(Region.AP_NORTHEAST_1)
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USER1 = "USER1";
    private static final String PASSWORD1 = "PASSWORD1";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    @BeforeAll
    public static void cleanDB() {
        invokeUserService(DELETE, USER1, PASSWORD1);
    }

    @Test
    public void testCreateUser() {
        // Invoke UserService to create user
        UserServiceResponse response = invokeUserService(POST, USER1, PASSWORD1);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void testRemoveUser() {
        // Invoke UserService to create user
        UserServiceResponse postResponse = invokeUserService(POST, USER1, PASSWORD1);
        assertEquals(201, postResponse.getStatusCode());

        // Invoke UserService to delete user
        UserServiceResponse deleteResponse = invokeUserService(DELETE, USER1, PASSWORD1);
        assertEquals(200, deleteResponse.getStatusCode());
    }

    private static UserServiceResponse invokeUserService(String method, String username, String password) {
        try {
            //Need a SdkBytes instance for the payload
            String json = String.format("{\n" +
                    "  \"body\": \"{\\n    \\\"username\\\" : \\\"%s\\\",\\n    \\\"password\\\" : \\\"%s\\\"\\n}\",\n" +
                    "  \"resource\": \"/users\",\n" +
                    "  \"path\": \"/users\",\n" +
                    "  \"httpMethod\": \"%s\",\n" +
                    "  \"isBase64Encoded\": true,\n" +
                    "  \"queryStringParameters\": {},\n" +
                    "  \"requestContext\": {}\n" +
                    "}", username, password, method);

            SdkBytes payload = SdkBytes.fromUtf8String(json);

            //Setup an InvokeRequest
            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(payload)
                    .build();

            InvokeResponse res = awsLambda.invoke(request);

            try {
                return objectMapper.readValue(res.payload().asUtf8String(), UserServiceResponse.class);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } catch(LambdaException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
