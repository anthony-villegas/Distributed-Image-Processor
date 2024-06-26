package lambdas.helpers;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import schemas.SignedUrl;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

public class SignedUrlGenerator {
    private final String bucketName;
    private final S3Presigner presigner;

    public SignedUrlGenerator(String bucketName, S3Presigner presigner) {
        this.bucketName = bucketName;
        this.presigner = presigner;
    }

    public SignedUrl createSignedUrl(LambdaLogger logger) {
        String keyName = createUUID();
        String imageUrl = createSignedUrl(keyName, logger);

        return new SignedUrl(keyName, imageUrl);
    }

    private String createSignedUrl(String keyName, LambdaLogger logger) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        String myURL = presignedRequest.url().toString();
        logger.log("Presigned URL to upload a file to: " +  myURL);
        logger.log("HTTP method: " + presignedRequest.httpRequest().method());

        return presignedRequest.url().toExternalForm();
    }
    private String createUUID() {
        return UUID.randomUUID().toString();
    }
    public void close() {
        presigner.close();
    }
}
