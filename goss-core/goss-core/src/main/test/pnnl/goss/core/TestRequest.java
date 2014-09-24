package pnnl.goss.core;

import org.junit.Assert;
import org.junit.Test;

import pnnl.goss.core.Request.RESPONSE_FORMAT;

public class TestRequest {
	
	@Test
	public void twoRequestsDontHaveSameId(){
		Request req = new Request();
		Request req2 = new Request();
		
		Assert.assertNotEquals(req.getId(), req2.getId());
	}
	
	@Test
	public void reqestXmlFormatIsDefault(){
		Request req = new Request();
		Assert.assertEquals(RESPONSE_FORMAT.XML, req.getResponseFormat());
	}
	
	
	// TODO Add validation for URL being of proper format.
	

}
