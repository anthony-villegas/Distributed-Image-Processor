package schemas;

import java.util.Map;

public class CognitoEvent {
    private String version;
    private String triggerSource;
    private String region;
    private String userPoolId;
    private String userName;
    private CallerContext callerContext;
    private Request request;
    private Map<String, String> response;

    public String getVersion() {
        return version;
    }

    public String getTriggerSource() {
        return triggerSource;
    }

    public String getRegion() {
        return region;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public String getUserName() {
        return userName;
    }

    public CallerContext getCallerContext() {
        return callerContext;
    }

    public Request getRequest() {
        return request;
    }

    public Map<String, String> getResponse() {
        return response;
    }

    public void setCallerContext(CallerContext callerContext) {
        this.callerContext = callerContext;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setResponse(Map<String, String> response) {
        this.response = response;
    }

    public void setTriggerSource(String triggerSource) {
        this.triggerSource = triggerSource;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CognitoEvent {\n");
        sb.append("  version: ").append(version).append("\n");
        sb.append("  triggerSource: ").append(triggerSource).append("\n");
        sb.append("  region: ").append(region).append("\n");
        sb.append("  userPoolId: ").append(userPoolId).append("\n");
        sb.append("  userName: ").append(userName).append("\n");
        sb.append("  callerContext: ").append(callerContext).append("\n");
        sb.append("  request: ").append(request).append("\n");
        sb.append("  response: ").append(response).append("\n");
        sb.append("}");
        return sb.toString();
    }
}