package local.unit;

import com.zaxxer.hikari.HikariDataSource;
import lambdas.beans.RequestBean;
import lambdas.beans.UserBean;
import lambdas.daos.RequestDao;
import lambdas.daos.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RequestDaoUnitTests {
    private static Jdbi jdbi;
    private static UserBean user1;
    @BeforeAll
    public static void setUp() {
        // Initialize an in-memory H2 database
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");

        // Initialize Jdbi with the H2 database
        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        user1 = new UserBean("1", "user1@example.com", Timestamp.valueOf("2024-01-01 10:10:10"));
        jdbi.useExtension(UserDao.class, dao -> {
            dao.createTable();
            dao.insertBean(user1);
        });
    }

    @Test
    public void RequestDao_insertTest() {
        int id = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            return dao.insert(user1.getId(), RequestBean.Status.PENDING);
        });

        assertTrue(id > 0);

        RequestBean request = jdbi.withExtension(RequestDao.class, dao -> {
           return dao.getRequest(id);
        });

        assertNotNull(request);
        assertEquals(request.getUserId(), user1.getId());
        assertEquals(request.getId(), id);
        assertNotNull(request.getTimestamp());
        assertEquals(request.getStatus(), RequestBean.Status.PENDING);
    }

    @Test
    public void RequestDao_insertBeanTest() {
        RequestBean inserted = new RequestBean(2, user1.getId(), Timestamp.valueOf("2024-01-01 10:10:10"), RequestBean.Status.PENDING);
        RequestBean retrieved = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            dao.insertBean(inserted);
            return dao.getRequest(inserted.getId());
        });
        assertEquals(inserted, retrieved);
    }

    @Test
    public void RequestDao_updateStatusTest() {
        RequestBean inserted = new RequestBean(4, user1.getId(), Timestamp.valueOf("2024-01-01 10:10:10"), RequestBean.Status.PENDING);
        RequestBean retrieved = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            dao.insertBean(inserted);
            dao.updateStatus(inserted.getId(), RequestBean.Status.COMPLETED);
            return dao.getRequest(inserted.getId());
        });
        assertEquals(retrieved.getStatus(), RequestBean.Status.COMPLETED);
    }

    @Test
    public void RequestDao_getRequestTest_NoRequestPresent() {
        RequestBean request = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            return dao.getRequest(7);
        });
        assertNull(request);
    }

    @Test
    public void RequestDao_listRequestsTest() {
        RequestBean request1 = new RequestBean(8, user1.getId(), Timestamp.valueOf("2024-01-01 10:10:10"), RequestBean.Status.PENDING);
        RequestBean request2 = new RequestBean(9, user1.getId(), Timestamp.valueOf("2024-01-01 10:10:10"), RequestBean.Status.PROCESSING);
        RequestBean request3 = new RequestBean(10, user1.getId(), Timestamp.valueOf("2024-01-01 10:10:10"), RequestBean.Status.COMPLETED);

        List<RequestBean> retrieved = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            dao.insertBean(request1);
            dao.insertBean(request2);
            dao.insertBean(request3);
            return dao.listRequests();
        });
        assertTrue(retrieved.containsAll(Arrays.asList(request1, request2, request3)));
    }
}
