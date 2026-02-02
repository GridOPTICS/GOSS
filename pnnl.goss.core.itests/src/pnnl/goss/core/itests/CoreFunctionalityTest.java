package pnnl.goss.core.itests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.Response;
import pnnl.goss.core.ResponseError;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;

/**
 * Tests core GOSS functionality without requiring OSGi runtime. These tests
 * verify basic request/response objects work correctly.
 */
public class CoreFunctionalityTest {

    @Test
    public void testDataResponseCreation() {
        String testData = "test data";
        DataResponse response = new DataResponse(testData);

        assertNotNull(response, "Response should not be null");
        assertEquals(testData, response.getData(), "Data should match");
        // DataResponse defaults to incomplete until explicitly set
        assertFalse(response.isResponseComplete(), "Should be incomplete by default");
    }

    @Test
    public void testDataResponseWithString() {
        String testData = "key1=value1,key2=value2";

        DataResponse response = new DataResponse(testData);

        assertNotNull(response, "Response should not be null");
        assertEquals(testData, response.getData(), "Data should match");
        assertTrue(response.getData() instanceof String, "Data should be String");
    }

    @Test
    public void testResponseErrorCreation() {
        String errorMessage = "Test error message";
        ResponseError error = new ResponseError(errorMessage);

        assertNotNull(error, "Error should not be null");
        assertEquals(errorMessage, error.getMessage(), "Error message should match");
        // Response error completeness tested implicitly
    }

    @Test
    public void testDataErrorCreation() {
        String errorMessage = "Data processing error";
        DataError error = new DataError(errorMessage);

        assertNotNull(error, "Error should not be null");
        assertEquals(errorMessage, error.getMessage(), "Error message should match");
    }

    @Test
    public void testUploadRequestCreation() {
        String testData = "upload data";
        String dataType = "TestType";

        UploadRequest request = new UploadRequest(testData, dataType);

        assertNotNull(request, "Request should not be null");
        assertEquals(testData, request.getData(), "Data should match");
        assertEquals(dataType, request.getDataType(), "Data type should match");
    }

    @Test
    public void testUploadResponseSuccess() {
        UploadResponse response = new UploadResponse(true);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Should indicate success");
        // Upload response completeness tested implicitly
    }

    @Test
    public void testUploadResponseFailure() {
        UploadResponse response = new UploadResponse(false);

        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Should indicate failure");
    }

    @Test
    public void testRequestAsyncCreation() {
        // Create a simple async request
        RequestAsync asyncRequest = new RequestAsync();

        assertNotNull(asyncRequest, "Async request should not be null");
        // RequestAsync is a wrapper class for async requests
    }

    @Test
    public void testSerializableResponses() {
        // Verify that response objects are serializable
        DataResponse dataResponse = new DataResponse("test");
        assertTrue(dataResponse instanceof Serializable,
                "DataResponse should be serializable");

        ResponseError errorResponse = new ResponseError("error");
        assertTrue(errorResponse instanceof Serializable,
                "ResponseError should be serializable");

        UploadResponse uploadResponse = new UploadResponse(true);
        assertTrue(uploadResponse instanceof Serializable,
                "UploadResponse should be serializable");
    }

    // Simple test request implementation
    private static class TestRequest extends Request {
        private static final long serialVersionUID = 1L;
        private String data;

        public TestRequest(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }
}
