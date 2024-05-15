package lambdas.helpers;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import schemas.SignedUrl;
import schemas.SignedUrlRequest;
import schemas.SignedUrlResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SignedUrlGenerator {
    private final String bucketName;
    private final LambdaLogger logger;
    public SignedUrlGenerator(String bucketName, LambdaLogger logger) {
        this.bucketName = bucketName;
        this.logger = logger;
    }

    public List<SignedUrl> createSignedUrls(SignedUrlRequest signedUrlRequest) {
        List<SignedUrl> preSignedUrls = new ArrayList<SignedUrl>();
        for (String imageName : signedUrlRequest.getImages()) {
            String keyName = createKeyName(signedUrlRequest.getUserID(), createUUID(), imageName);
            String imageUrl = createSignedUrl(keyName, null);
            preSignedUrls.add(new SignedUrl(keyName, imageUrl));
        }
        return preSignedUrls;
    }

    private String createSignedUrl(String keyName, Map<String, String> metadata) {
        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .metadata(metadata)
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
    }
    private String createUUID() {
        return UUID.randomUUID().toString();
    }

    private String createKeyName(String userID, String UUID, String imageName) {
        return "sources/" + userID + "/" + UUID + "/" + imageName;
    }
}
