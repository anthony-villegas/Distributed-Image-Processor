package lambdas.helpers;

public class UserNotFoundException extends Exception {
    public UserNotFoundException() {
        super("User not found");
    }
}