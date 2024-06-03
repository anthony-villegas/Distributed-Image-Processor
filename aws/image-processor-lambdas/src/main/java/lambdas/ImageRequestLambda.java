package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import lambdas.beans.ProcessingTaskBean;
import lambdas.beans.RequestBean;
import lambdas.daos.ProcessingTaskDao;
import lambdas.daos.RequestDao;
import lambdas.daos.SourceImageDao;
import lambdas.helpers.DatabaseCredentialsManager;
import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;
import java.util.Base64;

import static lambdas.UserService.initializeHikari;
import static lambdas.helpers.ApiGatewayResponseHelper.createApiGatewayResponse;
import static lambdas.helpers.ErrorCode.ERROR_PROCESSING_IMAGE_REQUEST;
import static lambdas.helpers.ResponseMessage.IMAGE_PROCESSING_REQUEST_HAS_BEEN_QUEUED;

public class ImageRequestLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static Jdbi jdbi;
    private final ObjectMapper objectMapper;

    public ImageRequestLambda(Jdbi jdbi, ObjectMapper objectMapper) {
        ImageRequestLambda.jdbi = jdbi;
        this.objectMapper = objectMapper;
    }

    public ImageRequestLambda() {
        String secretName = System.getenv("DB_SECRET_NAME");
        HikariDataSource dataSource = initializeHikari(
                new DatabaseCredentialsManager(secretName).getSecretCredentials(),
                System.getenv("DB_ENDPOINT_ADDRESS"),
                System.getenv("DB_NAME")
        );
        ImageRequestLambda.jdbi = Jdbi.create(dataSource);
        this.objectMapper = new ObjectMapper();
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            context.getLogger().log("Handling image processing request:\n" + request.toString());
            processRequest(request);
            return createApiGatewayResponse(200, IMAGE_PROCESSING_REQUEST_HAS_BEEN_QUEUED);
        } catch (Exception e) {
            context.getLogger().log(ERROR_PROCESSING_IMAGE_REQUEST + ":\n" + e.toString());
            return createApiGatewayResponse(500, ERROR_PROCESSING_IMAGE_REQUEST);
        }
    }

    private void processRequest(APIGatewayProxyRequestEvent request) throws JsonProcessingException {
        String userId = extractUserIdFromJwt(request.getHeaders().get("Authorization"));
        JsonNode imageRequest = deserializeImageRequest(request.getBody());
        String imageId = imageRequest.path("imageId").asText();
        ProcessingTaskBean.Action action = ProcessingTaskBean.Action.valueOf(imageRequest.path("action").asText());

        int requestId = insertRequest(userId);
        insertSourceImage(imageId, requestId);
        insertProcessingTask(imageId, action);
        queueToSqs(requestId);
    }

    public String extractUserIdFromJwt(String jwt) throws JsonProcessingException {
        String payload = decodeJwtPayload(jwt);
        JsonNode rootNode = objectMapper.readTree(payload);
        return rootNode.path("cognito:username").asText();
    }

    private String decodeJwtPayload(String jwt) {
        String[] chunks = jwt.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return new String(decoder.decode(chunks[1]));
    }

    private JsonNode deserializeImageRequest(String body) throws JsonProcessingException {
        return objectMapper.readTree(body);
    }

    private int insertRequest(String userId) {
        return jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            return dao.insert(userId, RequestBean.Status.PENDING);
        });
    }

    private void insertSourceImage(String imageId, int requestId) {
        jdbi.useExtension(SourceImageDao.class, dao -> {
            dao.createTable();
            dao.insert(imageId, requestId);
        });
    }

    private void insertProcessingTask(String imageId, ProcessingTaskBean.Action action) {
        ProcessingTaskBean task = new ProcessingTaskBean();
        task.setImageId(imageId);
        task.setStatus(ProcessingTaskBean.Status.QUEUED);
        task.setRetries(0);
        task.setQueuedAt(new Timestamp(System.currentTimeMillis()));
        task.setAction(action);
        jdbi.useExtension(ProcessingTaskDao.class, dao -> {
            dao.createTable();;
            dao.insert(task);
        });
    }

    private void queueToSqs(int requestId) {
    }
}
