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
    public static void cleanDB() {}

    @Test
    public void testCreateUser() {

    }

    @Test
    public void testRemoveUser() {
    }

    private static UserServiceResponse invokeUserService(String method, String username, String password) {
        return null;
    }
}
