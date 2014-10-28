package pnnl.goss.core;

import org.junit.Assert;
import org.junit.Test;

public class TestDataError {

	@Test
	public void canSetGetMessage(){
		String message = "Invalid argument or something funky happened!";
		DataError err = new DataError(message);
		Assert.assertEquals(message, err.getMessage());
	}
}
