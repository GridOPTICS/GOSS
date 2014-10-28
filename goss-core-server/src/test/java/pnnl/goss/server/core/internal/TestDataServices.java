package pnnl.goss.server.core.internal;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

//import org.apache.directory.api.ldap.aci.UserClass.ThisEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pnnl.goss.server.core.GossDataServices;

public class TestDataServices {
	
	private GossDataServicesImpl dataServices;
	
	@Before
	public void setupService(){
		this.dataServices = new GossDataServicesImpl();
	}
	
	@Test
	public void canAddAndRetrieveDataService() {
		String key = "AnyServiceKey";
		String value = "A Value";
		
		this.dataServices.registerData(key, value);
		Assert.assertTrue(this.dataServices.getAvailableDataServices().size() == 1);
		Assert.assertEquals(value,  this.dataServices.getDataService(key));
	}
	
	@Test
	public void canUpdateDictionary(){
		@SuppressWarnings("rawtypes")
		Dictionary dict = new Hashtable<String, String>();
		
		dict.put("yes", "sir");
		this.dataServices.update(dict);
		
		Assert.assertEquals(1, this.dataServices.getPropertyKeys().size());
		Assert.assertEquals("yes", this.dataServices.getPropertyKeys().iterator().next());
		Assert.assertEquals("sir", this.dataServices.getPropertyValue("yes"));
		
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void dataServiceListIsNotModifyable(){
		Collection<String> unmodifyable = this.dataServices.getAvailableDataServices();
		unmodifyable.add("junk");
	}
}
