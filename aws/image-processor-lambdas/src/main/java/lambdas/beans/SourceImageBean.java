package lambdas.beans;

import java.util.Objects;

public class SourceImageBean {
    private String id;
    private int requestId;

    public SourceImageBean() {}

    public SourceImageBean(String id, int requestId) {
        this.id = id;
        this.requestId = requestId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRequestId() {
        return this.requestId;
    }
    
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "SourceImageBean{" +
                "id=" + id +
                ", requestId=" + requestId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceImageBean sourceImageBean = (SourceImageBean) o;
        return Objects.equals(id, sourceImageBean.id) &&
                Objects.equals(requestId, sourceImageBean.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, requestId);
    }
}
