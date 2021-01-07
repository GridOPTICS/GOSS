package pnnl.goss.core.security.impl;

import java.security.SecureRandom;
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
	private boolean useToken = false;
	private byte[] tokenSecret = generateSharedKey();
	
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
			
			String secret = getProperty(SecurityConstants.PROP_SYSTEM_TOKEN_SECRET, null);
			if(secret!=null && secret.trim().length()>0){
				this.tokenSecret = secret.getBytes();
			}
			
			String useTokenString = getProperty(SecurityConstants.PROP_SYSTEM_USE_TOKEN
					,null);	
			if(secret!=null && secret.trim().length()>0){
				try{
					this.useToken = new Boolean(useTokenString);
				}catch (Exception e) {
					log.error("Could not parse use token parameter as boolean in security config: '"+useTokenString+"'");
				}
			}
			
			System.out.println("SYSTEM CONFIG UPDATED "+managerUser+" "+managerPassword+" "+this);
        	
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



	@Override
	public boolean getUseToken() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public byte[] getTokenSecret() {
		return tokenSecret;
	}
	
	
    private byte[] generateSharedKey() {
        SecureRandom random = new SecureRandom();
        byte[] sharedKey = new byte[32];
        random.nextBytes(sharedKey);
        return sharedKey;
    }

}
