package cloud.integration;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import schemas.SignedUrl;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SignedUrlServiceIntegrationTests {
    private static final String LAMBDA_FUNCTION_NAME = "SignedUrlService";
    private static final String S3_BUCKET_NAME = "ap-northeast-1-image-processor-images-bucket";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static LambdaClient lambdaClient;
    private static S3Client s3Client;

    @BeforeAll
    public static void setup() {
        // Initialize AWS clients
        lambdaClient = LambdaClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        s3Client = S3Client.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Test
    public void testSignedUrlServiceIntegration() throws Exception {
        // Invoke Lambda function to get signed URL
        SignedUrl signedUrl = invokeLambdaAndGetSignedUrl();

        // Upload an image to S3 using the signed URL
        uploadImageToS3(signedUrl.getSignedUrl());

        // Verify the presence of the uploaded image in S3
        assertTrue(isObjectPresentInS3(S3_BUCKET_NAME, signedUrl.getImageId()));
    }

    private SignedUrl invokeLambdaAndGetSignedUrl() throws JsonProcessingException {
        // Prepare input for Lambda function (if needed)
        InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(LAMBDA_FUNCTION_NAME)
                .build();

        // Invoke Lambda function
        InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
        APIGatewayProxyResponseEvent response = objectMapper.readValue(invokeResponse.payload().asUtf8String(), APIGatewayProxyResponseEvent.class);
        if(response.getStatusCode() != 200) {
            fail();
        }
        JSONObject jsonObject = new JSONObject(response.getBody());
        return new SignedUrl(jsonObject.getString("imageId"), jsonObject.getString("signedUrl"));
    }

    private void uploadImageToS3(String signedUrl) throws Exception {
        // Prepare image file to upload
        Path filePath = Paths.get("src", "test", "java", "resources", "test-image.png");
        File imageFile = filePath.toFile();
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        byte[] imageData = Files.readAllBytes(filePath);

        // Use HttpClient to upload the image to S3 using the pre-signed URL
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(signedUrl))
                .PUT(HttpRequest.BodyPublishers.ofByteArray(imageData))
                .header("Content-Type", "image/png")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to upload image to S3. HTTP status code: " + response.statusCode());
        }
    }

    private boolean isObjectPresentInS3(String bucketName, String objectKey) {
        try {
            HeadObjectResponse headResponse = s3Client
                    .headObject(HeadObjectRequest
                            .builder().bucket(bucketName)
                            .key(objectKey)
                            .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
