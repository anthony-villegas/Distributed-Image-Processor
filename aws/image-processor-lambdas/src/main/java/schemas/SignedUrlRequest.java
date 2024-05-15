package schemas;

import java.util.ArrayList;
import java.util.List;

public class SignedUrlRequest {
    private String userID;
    private List<String> images;

    public String getUserID() {
        return userID;
    }

    public List<String> getImages() {
        return images;
    }
}
