package local.unit;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lambdas.helpers.SignedUrlGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import schemas.SignedUrl;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SignedUrlGeneratorUnitTests {
    private static SignedUrlGenerator generator;
    private static LambdaLogger logger;
    private static final String bucketName = "test-bucket";

    @BeforeAll
    public static void setUp() {
        logger = Mockito.mock(LambdaLogger.class);
        generator = new SignedUrlGenerator(bucketName, S3Presigner.create());
    }
    @Test
    public void testCreateSignedUrl() {
        SignedUrl signedUrl = generator.createSignedUrl(logger);

        assertNotNull(signedUrl);
        assertNotNull(signedUrl.getSignedUrl());
        assertNotNull(signedUrl.getImageId());
        // Ensure the signed URL starts with the expected bucket URL
        assertTrue(signedUrl.getSignedUrl().startsWith("https://" + bucketName + ".s3."));
    }

    @Test
    public void testCreatesUniqueIds() {
        // Ensure image ids are unique
        Set<String> ids = new HashSet<>();
        for(int i = 0; i < 500; i++) {
            String id = generator.createSignedUrl(logger).getImageId();
            if(ids.contains(id)){
                fail();
            }
            ids.add(id);
        }
    }
}
