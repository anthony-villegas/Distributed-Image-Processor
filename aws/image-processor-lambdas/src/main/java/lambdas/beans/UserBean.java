package lambdas.beans;

import java.sql.Timestamp;
import java.util.Objects;

public class UserBean {
    private String id;
    private String email;
    private Timestamp creationTime;

    public UserBean() {}

    public UserBean(String id, String email, Timestamp creationTime) {
        this.id = id;
        this.email = email;
        this.creationTime = creationTime;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public String getId() { return id; }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setId(String id) { this.id = id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBean userBean = (UserBean) o;
        return Objects.equals(id, userBean.id) &&
                Objects.equals(email, userBean.email) &&
                Objects.equals(creationTime, userBean.creationTime);
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }
}
