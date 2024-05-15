package schemas;

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
}
