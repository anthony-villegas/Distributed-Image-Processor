package helpers;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import org.json.JSONObject;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;

public class DatabaseCredentialsManager {

    private final SecretCache cache;
    private final String secretName;

    public DatabaseCredentialsManager(String secretName) {
        this.secretName = secretName;
        AWSSecretsManager client = AWSSecretsManagerClient.builder()
                .withRegion(System.getenv("AWS_REGION"))
                .build();
        this.cache = new SecretCache(client);
    }

    public record Credentials(String username, String password) {
    }

    public Credentials getSecretCredentials() {
        final String secretString  = cache.getSecretString(this.secretName);

        if (secretString != null) {
            try {
                // Parse the secret string as JSON
                JSONObject jsonObject = new JSONObject(secretString);

                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");

                return new Credentials(username, password);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing secret string", e);
            }
        } else {
            throw new RuntimeException("Secret not found");
        }
    }
}
