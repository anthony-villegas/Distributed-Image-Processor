package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lambdas.helpers.SignedUrlGenerator;
import schemas.SignedUrl;
import schemas.SignedUrlRequest;
import schemas.SignedUrlResponse;

import java.util.List;

import static lambdas.helpers.ApiGatewayResponseHelper.createApiGatewayResponse;
import static lambdas.helpers.ErrorCode.ERROR_PROCESSING_SIGNED_URL_REQUEST;
import static lambdas.helpers.ErrorCode.INVALID_SIGNED_URL_INPUT;

public class SignedUrlService implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final String IMAGE_BUCKET_NAME = System.getenv("IMAGE_BUCKET_NAME");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            context.getLogger().log("Handling presigned url request:\n" + request.toString());
            SignedUrlGenerator signedUrlGenerator = new SignedUrlGenerator(IMAGE_BUCKET_NAME, context.getLogger());
            SignedUrlRequest signedUrlRequest = parseSignedUrlRequest(request.getBody());
            List<SignedUrl> signedUrls = signedUrlGenerator.createSignedUrls(signedUrlRequest);
            SignedUrlResponse signedUrlResponse = new SignedUrlResponse(signedUrlRequest.getUserID(), signedUrls);
            return createApiGatewayResponse(200, signedUrlResponse.toJson());
        } catch (JsonProcessingException e) {
            context.getLogger().log(ERROR_PROCESSING_SIGNED_URL_REQUEST + ":\n" + e.toString());
            return createApiGatewayResponse(500, ERROR_PROCESSING_SIGNED_URL_REQUEST);
        } catch (IllegalArgumentException e) {
            return createApiGatewayResponse(400, INVALID_SIGNED_URL_INPUT);
        }
    }

    private SignedUrlRequest parseSignedUrlRequest(String body) throws JsonProcessingException {
        SignedUrlRequest signedUrlRequest = objectMapper.readValue(body, SignedUrlRequest.class);
        if(signedUrlRequest.getImages().isEmpty() ||  signedUrlRequest.getUserID().isEmpty()) {
            throw new IllegalArgumentException("Presigned URL Request contained empty fields");
        }
        return signedUrlRequest;
    }
}
