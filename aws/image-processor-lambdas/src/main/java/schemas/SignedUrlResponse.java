package schemas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class SignedUrlResponse {
    private String userID;
    private List<SignedUrl> signedURLs;

    public SignedUrlResponse(String userID, List<SignedUrl> signedUrls) {
        this.signedURLs = signedUrls;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public List<SignedUrl> getSignedURLs() {
        return signedURLs;
    }
    public void setSignedURLs(ArrayList<SignedUrl> signedURLs) {
        this.signedURLs = signedURLs;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}