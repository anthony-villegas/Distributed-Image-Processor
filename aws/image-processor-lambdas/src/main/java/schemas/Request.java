package schemas;

import java.util.Map;

public class Request {
    private Map<String, String> userAttributes;

    public Map<String, String> getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(Map<String, String> userAttributes) {
        this.userAttributes = userAttributes;
    }

    @Override
    public String toString() {
        return "Request { userAttributes: " + userAttributes + " }";
    }
}