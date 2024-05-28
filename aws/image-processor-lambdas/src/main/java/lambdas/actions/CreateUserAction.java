package lambdas.actions;

import com.amazonaws.services.lambda.runtime.Context;
import lambdas.daos.UserDao;
import lambdas.helpers.ResponseMessage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import lambdas.beans.UserBean;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class CreateUserAction implements UserAction {
    private final Jdbi jdbi;

    public CreateUserAction(Jdbi jdbi) {
        this.jdbi = jdbi;
        this.jdbi.installPlugin(new SqlObjectPlugin());
    }
    @Override
    public void doAction(UserBean user, Context context) {
        createUserInDatabase(user, context);
        context.getLogger().log(ResponseMessage.USER_CREATED_SUCCESSFULLY);
    }

    private void createUserInDatabase(UserBean user, Context context) {
        context.getLogger().log("Creating user: " + user.getId());

        jdbi.useExtension(UserDao.class, dao -> {
            dao.createTable();
            dao.insert(user.getId(), user.getEmail());
        });
    }
}
