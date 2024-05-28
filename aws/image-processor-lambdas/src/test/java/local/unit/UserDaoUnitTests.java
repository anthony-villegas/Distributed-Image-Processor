package local.unit;

import com.zaxxer.hikari.HikariDataSource;
import lambdas.beans.UserBean;
import lambdas.daos.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDaoUnitTests {
    private static Jdbi jdbi;
    @BeforeAll
    public static void setUp() {
        // Initialize an in-memory H2 database
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");

        // Initialize Jdbi with the H2 database
        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
    }

    @AfterAll
    public static void resetDatabase() {
        // Reset the database state before each test
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS user");
        });
    }

    @AfterEach
    public void resetUserTable() {
        // Reset the database state before each test
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS user");
        });
    }

    @Test
    public void UserDao_insertUser_UserPresent() {
        UserBean user = jdbi.withExtension(UserDao.class, dao -> {
            dao.createTable();

            dao.insert("ABC", "john@gmail.com");
            return dao.getUser("ABC");
        });

        assertTrue(user.getId().equals("ABC") &&
                    user.getEmail().equals("john@gmail.com") &&
                    user.getCreationTime() != null);
    }

    @Test
    public void UserDao_insertUserBeans_UsersPresent() {
        UserBean user1 = new UserBean("1", "user1@example.com", Timestamp.valueOf("2024-01-01 10:10:10"));
        UserBean user2 = new UserBean("2", "user2@example.com", Timestamp.valueOf("2024-01-02 11:11:11"));
        UserBean user3 = new UserBean("3", "user3@example.com", Timestamp.valueOf("2024-01-03 12:12:12"));

        List<UserBean> insertedUsers = Arrays.asList(user1, user2, user3);

        List<UserBean> retrievedUsers = jdbi.withExtension(UserDao.class, dao -> {
            dao.createTable();

            dao.insertBean(user1);
            dao.insertBean(user2);
            dao.insertBean(user3);

            return dao.listUsers();
        });
        assertTrue(retrievedUsers.containsAll(insertedUsers));
    }
}
