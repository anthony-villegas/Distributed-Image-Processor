package lambdas.helpers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Collections;

public interface ApiGatewayResponseHelper {
    static APIGatewayProxyResponseEvent createApiGatewayResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Collections.singletonMap("Content-Type", "text/plain"))
                .withBody(body);
    }
}
