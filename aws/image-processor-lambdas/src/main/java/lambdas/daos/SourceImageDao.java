package lambdas.daos;

import lambdas.beans.SourceImageBean;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface SourceImageDao {
    @SqlUpdate("CREATE TABLE IF NOT EXISTS source_image (" +
                    "id VARCHAR(100) PRIMARY KEY, " +
                    "requestId INT UNSIGNED, " +
                    "FOREIGN KEY (requestId) REFERENCES request(id) " +
                    "ON DELETE CASCADE " +
                    "ON UPDATE CASCADE" +
                ");")
    void createTable();

    @SqlUpdate("INSERT INTO source_image (id, requestId) VALUES (:id, :requestId)")
    void insert(@Bind("id") String id, @Bind("requestId") int requestId);

    @SqlUpdate("INSERT INTO source_image (id, requestId) VALUES (:id, :requestId)")
    void insertBean(@BindBean SourceImageBean sourceImage);

    @SqlQuery("SELECT * FROM source_image WHERE id = :id")
    @RegisterBeanMapper(SourceImageBean.class)
    SourceImageBean getSourceImage(@Bind("id") String id);

    @SqlQuery("SELECT * FROM source_image ORDER BY id")
    @RegisterBeanMapper(SourceImageBean.class)
    List<SourceImageBean> listSourceImages();

    @SqlQuery("SELECT * FROM source_image WHERE requestId = :requestId")
    @RegisterBeanMapper(SourceImageBean.class)
    List<SourceImageBean> listSourceImagesByRequestId(@Bind("requestId") int requestId);
}