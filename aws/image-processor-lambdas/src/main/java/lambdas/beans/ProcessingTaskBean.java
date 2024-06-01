package lambdas.beans;

import java.sql.Timestamp;
import java.util.Objects;

public class ProcessingTaskBean {
    private int id;
    private String imageId;
    private ProcessingTaskBean.Status status;
    private int retries;
    private String result;
    private Timestamp queuedAt;
    private Timestamp startedAt;
    private Timestamp finishedAt;

    public enum Status {
        QUEUED("queued"),
        PROCESSING("processing"),
        TIMEOUT("timeout"),
        FAILED("failed"),
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

        public static ProcessingTaskBean.Status fromString(String status) {
            for (ProcessingTaskBean.Status s : ProcessingTaskBean.Status.values()) {
                if (s.status.equalsIgnoreCase(status)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    public int getId() {
        return id;
    }

    public String getImageId() {
        return imageId;
    }

    public int getRetries() {
        return retries;
    }

    public Status getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    public Timestamp getFinishedAt() {
        return finishedAt;
    }

    public Timestamp getQueuedAt() {
        return queuedAt;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setFinishedAt(Timestamp finishedAt) {
        this.finishedAt = finishedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setQueuedAt(Timestamp queuedAt) {
        this.queuedAt = queuedAt;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessingTaskBean that = (ProcessingTaskBean) o;
        return id == that.id &&
                retries == that.retries &&
                Objects.equals(imageId, that.imageId) &&
                status == that.status &&
                Objects.equals(result, that.result) &&
                Objects.equals(queuedAt, that.queuedAt) &&
                Objects.equals(startedAt, that.startedAt) &&
                Objects.equals(finishedAt, that.finishedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageId, status, retries, result, queuedAt, startedAt, finishedAt);
    }

    @Override
    public String toString() {
        return "ProcessingTaskBean{" +
                "id=" + id +
                ", imageId='" + imageId + '\'' +
                ", status=" + status +
                ", retries=" + retries +
                ", result='" + result + '\'' +
                ", queuedAt=" + queuedAt +
                ", startedAt=" + startedAt +
                ", finishedAt=" + finishedAt +
                '}';
    }
}
