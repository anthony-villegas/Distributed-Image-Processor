package lambdas.helpers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public interface ApiGatewayResponseHelper {
    static APIGatewayProxyResponseEvent createPreflightRequestApiGatewayResponse() {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", Config.frotendUrl,
                "Access-Control-Allow-Methods", "GET, OPTIONS",
                "Access-Control-Allow-Headers", "*"
        );
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(headers);
    }
    static APIGatewayProxyResponseEvent createApiGatewayResponse(int statusCode, String body) {

        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", Config.frotendUrl,
                "Access-Control-Allow-Methods", "PUT, GET, OPTIONS",
                "Access-Control-Allow-Headers", "*"
        );
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }
}