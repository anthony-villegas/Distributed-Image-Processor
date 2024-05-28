package lambdas.daos;

import lambdas.beans.RequestBean;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface RequestDao {
    @SqlUpdate("CREATE TABLE IF NOT EXISTS request (" +
                    "id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                    "userId VARCHAR(100)," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "status ENUM('pending', 'processing', 'completed')," +
                    "FOREIGN KEY (userId) REFERENCES user(id)" +
                    "ON DELETE CASCADE " +
                    "ON UPDATE CASCADE" +
                ");\n")
    void createTable();

    @SqlUpdate("INSERT INTO request (userId, status) VALUES (:userId, :status)")
    @GetGeneratedKeys("id")
    int insert(@Bind("userId") String userId, @Bind("status") RequestBean.Status status);

    @SqlUpdate("INSERT INTO request (id, userId, timestamp, status) VALUES (:id, :userId, :timestamp, :status)")
    void insertBean(@BindBean RequestBean user);

    @SqlUpdate("UPDATE request SET status = :status WHERE id = :id")
    void updateStatus(@Bind("id") int id, @Bind("status") RequestBean.Status status);

    @SqlQuery("SELECT * FROM request WHERE id = :id")
    @RegisterBeanMapper(RequestBean.class)
    RequestBean getRequest(@Bind("id") int id);

    @SqlQuery("SELECT * FROM request ORDER BY timestamp")
    @RegisterBeanMapper(RequestBean.class)
    List<RequestBean> listRequests();
}