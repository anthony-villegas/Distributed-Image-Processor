package resources;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class MockLambdaLogger implements LambdaLogger {
    @Override
    public void log(String message) {}
    @Override
    public void log(byte[] bytes) {}
}
