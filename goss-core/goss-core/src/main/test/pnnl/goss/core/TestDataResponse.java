package pnnl.goss.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDataResponse {
	
	private DataResponse dataResponse;
	
	@Before
	public void setup(){
		dataResponse = new DataResponse();
	}
	
	@Test
	public void canSetAndGetData(){
		String originalData = "This is data";
		dataResponse.setData(originalData);
		Assert.assertEquals(originalData,  dataResponse.getData());
	}
	
	@Test
	public void canSetDataThroughConstructor(){
		String originalData = "Hey original data";
		dataResponse = new DataResponse(originalData);
		Assert.assertEquals(originalData,  dataResponse.getData());
	}
	
	@Test
	public void responseCompleteIsFalseByDefault(){
		Assert.assertFalse(dataResponse.isResponseComplete());
	}
	
	@Test
	public void responseCompleeteCanChange(){
		dataResponse.setResponseComplete(true);
		Assert.assertTrue(dataResponse.isResponseComplete());
	}

}
