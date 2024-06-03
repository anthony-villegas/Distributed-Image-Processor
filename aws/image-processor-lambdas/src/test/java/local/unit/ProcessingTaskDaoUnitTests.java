package local.unit;

import com.zaxxer.hikari.HikariDataSource;
import lambdas.beans.ProcessingTaskBean;
import lambdas.beans.RequestBean;
import lambdas.beans.UserBean;
import lambdas.daos.ProcessingTaskDao;
import lambdas.daos.RequestDao;
import lambdas.daos.SourceImageDao;
import lambdas.daos.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessingTaskDaoUnitTests {
    private static Jdbi jdbi;
    private static UserBean user1;
    private static int requestId1;
    private static String imageId1 = "A";

    @BeforeAll
    public static void setUp() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");

        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        user1 = new UserBean("1", "user1@example.com", Timestamp.valueOf("2024-01-01 10:10:10"));
        jdbi.useExtension(UserDao.class, dao -> {
            dao.createTable();
            dao.insertBean(user1);
        });

        requestId1 = jdbi.withExtension(RequestDao.class, dao -> {
            dao.createTable();
            return dao.insert(user1.getId(), RequestBean.Status.PENDING);
        });

        jdbi.useExtension(SourceImageDao.class, dao -> {
            dao.createTable();
            dao.insert(imageId1, requestId1);
        });
    }

    @AfterEach
    public void resetTable() {
        // Reset the database state before each test
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS processing_task");
        });
    }

    @AfterAll
    public static void resetDatabase() {
        // Reset the database state before each test
        jdbi.useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS processing_task");
            handle.execute("DROP TABLE IF EXISTS source_image");
            handle.execute("DROP TABLE IF EXISTS request");
            handle.execute("DROP TABLE IF EXISTS user");
        });
    }

    @Test
    public void ProcessingTaskDao_insertProcessingTask_ProcessingTaskPresent() {
        ProcessingTaskBean task = new ProcessingTaskBean();
        task.setImageId(imageId1);
        task.setStatus(ProcessingTaskBean.Status.QUEUED);
        task.setRetries(0);
        task.setQueuedAt(new Timestamp(System.currentTimeMillis()));
        task.setAction(ProcessingTaskBean.Action.RESIZE);

        int taskId = jdbi.withExtension(ProcessingTaskDao.class, dao -> {
            dao.createTable();
            return dao.insert(task);
        });

        ProcessingTaskBean retrievedTask = jdbi.withExtension(ProcessingTaskDao.class, dao -> dao.getProcessingTask(taskId));

        assertNotNull(retrievedTask);
        assertEquals(taskId, retrievedTask.getId());
        assertEquals(task.getImageId(), retrievedTask.getImageId());
        assertEquals(task.getStatus(), retrievedTask.getStatus());
        assertEquals(task.getRetries(), retrievedTask.getRetries());
        assertEquals(task.getAction(), retrievedTask.getAction());
    }

    @Test
    public void ProcessingTaskDao_updateProcessingTask_ProcessingTaskUpdated() {
        ProcessingTaskBean task = new ProcessingTaskBean();
        task.setImageId(imageId1);
        task.setStatus(ProcessingTaskBean.Status.QUEUED);
        task.setRetries(0);
        task.setQueuedAt(new Timestamp(System.currentTimeMillis()));
        task.setAction(ProcessingTaskBean.Action.RESIZE);

        int taskId = jdbi.withExtension(ProcessingTaskDao.class, dao -> {
            dao.createTable();
            return dao.insert(task);
        });

        ProcessingTaskBean retrievedTask = jdbi.withExtension(ProcessingTaskDao.class, dao -> dao.getProcessingTask(taskId));

        assertNotNull(retrievedTask);

        // Update task
        retrievedTask.setStatus(ProcessingTaskBean.Status.PROCESSING);
        retrievedTask.setStartedAt(new Timestamp(System.currentTimeMillis()));

        jdbi.useExtension(ProcessingTaskDao.class, dao -> dao.update(retrievedTask));

        ProcessingTaskBean updatedTask = jdbi.withExtension(ProcessingTaskDao.class, dao -> dao.getProcessingTask(taskId));

        assertEquals(ProcessingTaskBean.Status.PROCESSING, updatedTask.getStatus());
        assertNotNull(updatedTask.getStartedAt());
    }

    @Test
    public void ProcessingTaskDao_listProcessingTasksByImageId_TasksListed() {
        ProcessingTaskBean task1 = new ProcessingTaskBean();
        task1.setImageId(imageId1);
        task1.setStatus(ProcessingTaskBean.Status.QUEUED);
        task1.setRetries(0);
        task1.setQueuedAt(new Timestamp(System.currentTimeMillis()));
        task1.setAction(ProcessingTaskBean.Action.RESIZE);
        task1.setId(1);

        ProcessingTaskBean task2 = new ProcessingTaskBean();
        task2.setImageId(imageId1);
        task2.setStatus(ProcessingTaskBean.Status.QUEUED);
        task2.setRetries(0);
        task2.setQueuedAt(new Timestamp(System.currentTimeMillis()));
        task2.setAction(ProcessingTaskBean.Action.RESIZE);
        task2.setId(2);

        List<ProcessingTaskBean> tasks = jdbi.withExtension(ProcessingTaskDao.class, dao -> {
            dao.createTable();
            dao.insert(task1);
            dao.insert(task2);
            return dao.listProcessingTasksByImageId(imageId1);
        });

        assertTrue(tasks.containsAll(Arrays.asList(task1, task2)));
    }
}