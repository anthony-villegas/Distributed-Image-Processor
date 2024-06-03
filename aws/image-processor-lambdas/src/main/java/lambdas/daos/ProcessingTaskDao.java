package lambdas.daos;

import lambdas.beans.ProcessingTaskBean;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface ProcessingTaskDao {
    @SqlUpdate("CREATE TABLE IF NOT EXISTS processing_task (" +
            "id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY, " +
            "imageId VARCHAR(100), " +
            "status ENUM('queued', 'processing', 'timeout', 'failed', 'completed'), " +
            "retries TINYINT UNSIGNED, " +
            "result VARCHAR(100), " +
            "queuedAt TIMESTAMP, " +
            "startedAt TIMESTAMP, " +
            "finishedAt TIMESTAMP, " +
            "action ENUM('resize'), " +
            "FOREIGN KEY (imageId) REFERENCES source_image(id) " +
            "ON DELETE CASCADE " +
            "ON UPDATE CASCADE" +
            ");")
    void createTable();

    @SqlUpdate("INSERT INTO processing_task (imageId, status, retries, result, queuedAt, startedAt, finishedAt, action) " +
            "VALUES (:imageId, :status, :retries, :result, :queuedAt, :startedAt, :finishedAt, :action)")
    @GetGeneratedKeys("id")
    int insert(@BindBean ProcessingTaskBean task);

    @SqlUpdate("UPDATE processing_task SET " +
            "status = :status, " +
            "retries = :retries, " +
            "result = :result, " +
            "queuedAt = :queuedAt, " +
            "startedAt = :startedAt, " +
            "finishedAt = :finishedAt, " +
            "action = :action " +
            "WHERE id = :id")
    void update(@BindBean ProcessingTaskBean task);

    @SqlQuery("SELECT * FROM processing_task WHERE id = :id")
    @RegisterBeanMapper(ProcessingTaskBean.class)
    ProcessingTaskBean getProcessingTask(@Bind("id") int id);

    @SqlQuery("SELECT * FROM processing_task WHERE imageId = :imageId ORDER BY id")
    @RegisterBeanMapper(ProcessingTaskBean.class)
    List<ProcessingTaskBean> listProcessingTasksByImageId(@Bind("imageId") String imageId);
}