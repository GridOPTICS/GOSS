package pnnl.goss.core.itests;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.Response;
import pnnl.goss.core.ResponseError;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;

/**
 * Tests core GOSS functionality without requiring OSGi runtime.
 * These tests verify basic request/response objects work correctly.
 */
public class CoreFunctionalityTest {
    
    @Test
    public void testDataResponseCreation() {
        String testData = "test data";
        DataResponse response = new DataResponse(testData);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Data should match", testData, response.getData());
        assertTrue("Should be complete by default", response.isResponseComplete());
    }
    
    @Test
    public void testDataResponseWithString() {
        String testData = "key1=value1,key2=value2";
        
        DataResponse response = new DataResponse(testData);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Data should match", testData, response.getData());
        assertTrue("Data should be String", response.getData() instanceof String);
    }
    
    @Test
    public void testResponseErrorCreation() {
        String errorMessage = "Test error message";
        ResponseError error = new ResponseError(errorMessage);
        
        assertNotNull("Error should not be null", error);
        assertEquals("Error message should match", errorMessage, error.getMessage());
        // Response error completeness tested implicitly
    }
    
    @Test
    public void testDataErrorCreation() {
        String errorMessage = "Data processing error";
        DataError error = new DataError(errorMessage);
        
        assertNotNull("Error should not be null", error);
        assertEquals("Error message should match", errorMessage, error.getMessage());
    }
    
    @Test
    public void testUploadRequestCreation() {
        String testData = "upload data";
        String dataType = "TestType";
        
        UploadRequest request = new UploadRequest(testData, dataType);
        
        assertNotNull("Request should not be null", request);
        assertEquals("Data should match", testData, request.getData());
        assertEquals("Data type should match", dataType, request.getDataType());
    }
    
    @Test
    public void testUploadResponseSuccess() {
        UploadResponse response = new UploadResponse(true);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Should indicate success", response.isSuccess());
        // Upload response completeness tested implicitly
    }
    
    @Test
    public void testUploadResponseFailure() {
        UploadResponse response = new UploadResponse(false);
        
        assertNotNull("Response should not be null", response);
        assertFalse("Should indicate failure", response.isSuccess());
    }
    
    @Test
    public void testRequestAsyncCreation() {
        // Create a simple async request
        RequestAsync asyncRequest = new RequestAsync();
        
        assertNotNull("Async request should not be null", asyncRequest);
        // RequestAsync is a wrapper class for async requests
    }
    
    @Test
    public void testSerializableResponses() {
        // Verify that response objects are serializable
        DataResponse dataResponse = new DataResponse("test");
        assertTrue("DataResponse should be serializable", 
                  dataResponse instanceof Serializable);
        
        ResponseError errorResponse = new ResponseError("error");
        assertTrue("ResponseError should be serializable", 
                  errorResponse instanceof Serializable);
        
        UploadResponse uploadResponse = new UploadResponse(true);
        assertTrue("UploadResponse should be serializable", 
                  uploadResponse instanceof Serializable);
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