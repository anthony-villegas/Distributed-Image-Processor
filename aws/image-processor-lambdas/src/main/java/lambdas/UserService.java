package lambdas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.secretsmanager.caching.SecretCache;

public class UserService implements RequestHandler<Object, String> {
    private final SecretCache cache  = new SecretCache();
    private final String dbUrl = System.getenv("DB_ENDPOINT_ADDRESS");
    private final String secretArn = System.getenv("DB_SECRET_ARN");
    @Override
    public String handleRequest(Object input, Context context) {

        // Retrieve database credentials from AWS Secrets Manager

        String username = null;
        String password = null;


        final String secret  = cache.getSecretString(secretArn);
        return secret;

        /*
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().build();
        GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretArn);
        GetSecretValueResult result = client.getSecretValue(request);

        if (result.getSecretString() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result.getSecretString());
            username = jsonNode.get("username").asText();
            password = jsonNode.get("password").asText();
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            // Create a query
            String query = "SELECT * FROM your_table";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                // Execute the query
                try (ResultSet rs = stmt.executeQuery()) {
                    // Process the results
                    while (rs.next()) {
                        // Process each row
                        String rowData = rs.getString("column_name");
                        System.out.println(rowData);
                    }
                }
            }
            return "Query executed successfully";
        } catch (SQLException e) {
            // Handle any SQL errors
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }

         */
    }
}