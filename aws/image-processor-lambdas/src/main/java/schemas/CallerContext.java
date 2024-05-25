package schemas;

public class CallerContext {
    private String awsSdkVersion;
    private String clientId;

    public String getAwsSdkVersion() {
        return awsSdkVersion;
    }

    public String getClientId() {
        return clientId;
    }

    public void setAwsSdkVersion(String awsSdkVersion) {
        this.awsSdkVersion = awsSdkVersion;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "CallerContext { awsSdkVersion: " + awsSdkVersion + ", clientId: " + clientId + " }";
    }
}
