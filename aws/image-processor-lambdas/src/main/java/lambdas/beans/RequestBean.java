package lambdas.beans;

import java.sql.Timestamp;
import java.util.Objects;

public class RequestBean {
    private int id;
    private String userId;
    private Timestamp timestamp;
    private Status status;

    public enum Status {
        PENDING("pending"),
        PROCESSING("processing"),
        COMPLETED("completed");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return this.status;
        }

        public static Status fromString(String status) {
            for (Status s : Status.values()) {
                if (s.status.equalsIgnoreCase(status)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    public RequestBean() {
    }

    public RequestBean(int id, String userId, Timestamp timestamp, Status status) {
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RequestBean{" +
                "requestID=" + id +
                ", userID=" + userId +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestBean that = (RequestBean) o;
        return id == that.id &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(timestamp, that.timestamp) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, timestamp, status);
    }
}

