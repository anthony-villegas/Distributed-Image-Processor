package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lambdas.helpers.SignedUrlGenerator;
import schemas.SignedUrl;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static lambdas.helpers.ApiGatewayResponseHelper.*;
import static lambdas.helpers.ErrorCode.ERROR_PROCESSING_SIGNED_URL_REQUEST;

public class SignedUrlService implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final String IMAGE_BUCKET_NAME = System.getenv("IMAGE_BUCKET_NAME");
    private final SignedUrlGenerator signedUrlGenerator;

    public SignedUrlService() {
        this.signedUrlGenerator = new SignedUrlGenerator(IMAGE_BUCKET_NAME, S3Presigner.create());
    }

    public SignedUrlService(SignedUrlGenerator signedUrlGenerator) {
        this.signedUrlGenerator = signedUrlGenerator;
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            context.getLogger().log("Handling presigned url request:\n" + request.toString());
            SignedUrl signedUrl = generateSignedUrl(context.getLogger());
            return createApiGatewayResponse(200, signedUrl.toJson());
        } catch (Exception e) {
            context.getLogger().log(ERROR_PROCESSING_SIGNED_URL_REQUEST + ":\n" + e.toString());
            return createApiGatewayResponse(500, ERROR_PROCESSING_SIGNED_URL_REQUEST);
        }
    }

    private SignedUrl generateSignedUrl(LambdaLogger logger) {
        return signedUrlGenerator.createSignedUrl(logger);
    }
}