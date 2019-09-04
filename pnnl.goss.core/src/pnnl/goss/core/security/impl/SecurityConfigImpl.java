package pnnl.goss.core.security.impl;

import java.util.Dictionary;

import org.apache.felix.dm.annotation.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.security.SecurityConfig;
import pnnl.goss.core.security.SecurityConstants;


@Component
public class SecurityConfigImpl implements SecurityConfig {

	private String managerUser;
	private String managerPassword;
	
	private Dictionary<String, Object> properties;
	private static final Logger log = LoggerFactory.getLogger(SecurityConfigImpl.class);

	
	public SecurityConfigImpl(){
	}
	
	
	
	void updated(Dictionary<String, Object> properties) {
        if (properties != null) {
        	this.properties = properties;
        	
        	// create system realm
        	managerUser = getProperty(SecurityConstants.PROP_SYSTEM_MANAGER
					,null);	
			managerPassword = getProperty(SecurityConstants.PROP_SYSTEM_MANAGER_PASSWORD
					,null);	
        	
        	
        } else {
        	log.error("No core config properties received by security activator");
        	throw new SystemException("No security config properties received by activator", null);
        }
    }
	
	

	public String getProperty(String key, String defaultValue){
		String retValue = defaultValue;
		
		if (key != null && !key.isEmpty() && properties.get(key)!=null){
			String value = properties.get(key).toString();
			// Let the value pass through because it doesn't
			// start with ${
			if (!value.startsWith("${")){
				retValue = value;
			}
		}
	    	
		return retValue;
	}
	
	
	@Override
	public String getManagerUser() {
		return managerUser;
	}

	@Override
	public String getManagerPassword() {
		return managerPassword;
	}
	
	
	

}
