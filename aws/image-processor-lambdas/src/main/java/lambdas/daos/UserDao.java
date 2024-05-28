package lambdas.daos;

import lambdas.beans.UserBean;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface UserDao {
    @SqlUpdate("CREATE TABLE IF NOT EXISTS user (" +
                    "id VARCHAR(100) PRIMARY KEY NOT NULL," +
                    "email VARCHAR(255) UNIQUE NOT NULL," +
                    "creationTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");")
    void createTable();

    @SqlUpdate("INSERT INTO user (id, email, creationTime) VALUES (:id, :email, :creationTime)")
    void insertBean(@BindBean UserBean user);

    @SqlUpdate("INSERT INTO user (id, email) VALUES (:id, :email)")
    void insert(@Bind("id") String id, @Bind("email") String email);

    @SqlQuery("SELECT * FROM user WHERE id = :id")
    @RegisterBeanMapper(UserBean.class)
    UserBean getUser(@Bind("id") String id);

    @SqlQuery("SELECT * FROM user ORDER BY creationTime")
    @RegisterBeanMapper(UserBean.class)
    List<UserBean> listUsers();
}
