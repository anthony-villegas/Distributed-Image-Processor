package cloud.integration;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceIntegrationTests {
    private final String functionName = "UserService";
    @Test
    public void testLambdaFunction() {
        LambdaClient awsLambda = LambdaClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .build();

        InvokeResponse res = null ;
        try {
            //Need a SdkBytes instance for the payload
            String json = String.format("{\n" +
                    "  \"body\": \"{\\n    \\\"username\\\" : \\\"%s\\\",\\n    \\\"password\\\" : \\\"%s\\\"\\n}\",\n" +
                    "  \"resource\": \"/users\",\n" +
                    "  \"path\": \"/users\",\n" +
                    "  \"httpMethod\": \"POST\",\n" +
                    "  \"isBase64Encoded\": true,\n" +
                    "  \"queryStringParameters\": {},\n" +
                    "  \"requestContext\": {}\n" +
                    "}", "user", "pass");

            SdkBytes payload = SdkBytes.fromUtf8String(json) ;

            //Setup an InvokeRequest
            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(payload)
                    .build();

            res = awsLambda.invoke(request);
            String value = res.payload().asUtf8String() ;
            System.out.println(value);

        } catch(LambdaException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
