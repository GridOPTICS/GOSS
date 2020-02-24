package pnnl.goss.core.security;


public interface GossSecurityManager extends org.apache.shiro.mgt.SecurityManager {
	
	public static final String PROP_SYSTEM_MANAGER = "goss.system.manager";
	public static final String PROP_SYSTEM_MANAGER_PASSWORD = "goss.system.manager.password";
	public static final String PROP_GOSS_LOGIN_TOPIC = "goss.system.login";
	
	public String getProperty(String key, String defaultValue);


}
