package lambdas.helpers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Collections;
import java.util.Map;

public interface ApiGatewayResponseHelper {
    static final String OPTIONS = "OPTIONS";
    static APIGatewayProxyResponseEvent createPreflightRequestApiGatewayResponse() {
        return createApiGatewayResponse(200, null);
    }
    static APIGatewayProxyResponseEvent createApiGatewayResponse(int statusCode, String body) {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS",
                "Access-Control-Allow-Headers", "Content-Type,Access-Control-Allow-Headers,Authorization,X-Requested-With"
        );
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }
}