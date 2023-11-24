package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class UserService {
    public String handleRequest(String input) {
        return "Hello, " + input + "!";
    }
}
