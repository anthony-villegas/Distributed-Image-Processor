package schemas;

public class User {
    private String userID;
    private String email;
    private String creationTime;

    public String getEmail() {
        return email;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public String getUserID() { return userID; }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setUserID(String userID) { this.userID = userID; }
}
