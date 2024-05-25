package schemas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SignedUrl {
    private final String imageId;
    private final String signedUrl;

    public SignedUrl(String imageId, String signedUrl) {
        this.imageId = imageId;
        this.signedUrl = signedUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public String getSignedUrl() {
        return signedUrl;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
