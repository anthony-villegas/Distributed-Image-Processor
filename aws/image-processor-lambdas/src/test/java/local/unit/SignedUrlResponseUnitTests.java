package local.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import schemas.SignedUrl;
import schemas.SignedUrlResponse;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SignedUrlResponseUnitTests {

    @Test
    void testSignedUrlResponseToJson_Success() {
        try {
            ArrayList<SignedUrl> signedURLs = new ArrayList<>();
            signedURLs.add(new SignedUrl("abs", "https://example.com/image1.jpg"));
            signedURLs.add(new SignedUrl("123", "https://example.com/image2.jpg") );
            SignedUrlResponse response = new SignedUrlResponse("123", signedURLs);
            Assertions.assertEquals("{\"userID\":\"123\",\"signedURLs\":[{\"imageId\":\"abs\",\"signedUrl\":\"https://example.com/image1.jpg\"},{\"imageId\":\"123\",\"signedUrl\":\"https://example.com/image2.jpg\"}]}", response.toJson());
        } catch (JsonProcessingException e) {
            fail();
        }
    }
}
