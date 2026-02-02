package pnnl.goss.core.security.impl;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.shiro.mgt.DefaultActiveMqSecurityManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.SystemException;

import pnnl.goss.core.security.GossSecurityManager;

public class SecurityManagerImpl extends DefaultActiveMqSecurityManager implements GossSecurityManager{
	private static final Logger log = LoggerFactory.getLogger(SecurityManagerImpl.class);

	private Dictionary<String, Object> properties;
	
	
	void updated(Dictionary<String, Object> properties) {
        if (properties != null) {
        	this.properties = properties;
        	
        	// create system realm
        	String systemManager = getProperty(PROP_SYSTEM_MANAGER
					,null);	
			String systemManagerPassword = getProperty(PROP_SYSTEM_MANAGER_PASSWORD
					,null);	
        	
        	Realm defaultRealm;
			try {
				defaultRealm = new SystemRealm(systemManager, systemManagerPassword);
			
	    		Set<Realm> realms = new HashSet();
	    		realms.add(defaultRealm);
	    		
	    		setRealms(realms);
	    		
	    		SecurityUtils.setSecurityManager(this);
    		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
}
