package local.unit;

import com.zaxxer.hikari.HikariDataSource;
import lambdas.beans.RequestBean;
import lambdas.beans.SourceImageBean;
import lambdas.beans.UserBean;
import lambdas.daos.RequestDao;
import lambdas.daos.SourceImageDao;
import lambdas.daos.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SourceImageDaoUnitTests {
    private static Jdbi jdbi;
    private static UserBean user1;
    private static int requestId1;
    private static int requestId2;

    @BeforeAll
    public static void setUp() {
        // Initialize an in-memory H2 database
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");

        // Initialize Jdbi with the H2 database
        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        // Create and insert a user and request for images
        user1 = new UserBean("1", "user1@example.com", Timestamp.valueOf("2024-01-01 10:10:10"));
        jdbi.useExtension(UserDao.class, dao -> {
            dao.createTable();
            dao.insertBean(user1);
        });

        requestId1 = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            return dao.insert(user1.getId(), RequestBean.Status.PENDING);
        });

        requestId2 = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            return dao.insert(user1.getId(), RequestBean.Status.PENDING);
        });
    }

    @AfterEach
    public void resetTable() {
        // Reset the database state before each test
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS source_image");
        });
    }

    @AfterAll
    public static void resetDatabase() {
        // Reset the database state before each test
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS source_image");
            handle.execute("DROP TABLE IF EXISTS request");
            handle.execute("DROP TABLE IF EXISTS user");
        });
    }

    @Test
    public void SourceImageDao_insertSourceImage_SourceImagePresent() {
        SourceImageBean sourceImage = jdbi.withExtension(SourceImageDao.class, dao -> {
            dao.createTable();
            dao.insert("A", requestId1);
            return dao.getSourceImage("A");
        });

        assertNotNull(sourceImage);
        assertEquals("A", sourceImage.getId());
        assertEquals(requestId1, sourceImage.getRequestId());
    }

    @Test
    public void SourceImageDao_insertSourceImageBeans_SourceImagesPresent() {
        SourceImageBean sourceImage1 = new SourceImageBean("A", requestId1);
        SourceImageBean sourceImage2 = new SourceImageBean("B", requestId1);
        SourceImageBean sourceImage3 = new SourceImageBean("C", requestId2);

        List<SourceImageBean> insertedSourceImages = Arrays.asList(sourceImage1, sourceImage2, sourceImage3);

        List<SourceImageBean> retrievedSourceImages = jdbi.withExtension(SourceImageDao.class, dao -> {
            dao.createTable();

            dao.insertBean(sourceImage1);
            dao.insertBean(sourceImage2);
            dao.insertBean(sourceImage3);

            return dao.listSourceImages();
        });

        assertTrue(retrievedSourceImages.containsAll(insertedSourceImages));
    }

    @Test
    public void SourceImageDao_listSourceImageBeansByRequest_SourceImagesPresent() {
        SourceImageBean sourceImage1 = new SourceImageBean("A", requestId1);
        SourceImageBean sourceImage2 = new SourceImageBean("B", requestId1);
        SourceImageBean sourceImage3 = new SourceImageBean("C", requestId1);

        List<SourceImageBean> insertedSourceImages = Arrays.asList(sourceImage1, sourceImage2, sourceImage3);

        List<SourceImageBean> retrievedSourceImages = jdbi.withExtension(SourceImageDao.class, dao -> {
            dao.createTable();

            dao.insertBean(sourceImage1);
            dao.insertBean(sourceImage2);
            dao.insertBean(sourceImage3);

            return dao.listSourceImagesByRequestId(requestId1);
        });

        assertTrue(retrievedSourceImages.containsAll(insertedSourceImages));
    }
}
