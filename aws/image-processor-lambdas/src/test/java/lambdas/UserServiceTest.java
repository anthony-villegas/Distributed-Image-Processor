package lambdas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void returnsHelloTest() {
        UserService sut = new UserService();
        assertEquals("Hello, World!", sut.handleRequest("World"));
    }
}