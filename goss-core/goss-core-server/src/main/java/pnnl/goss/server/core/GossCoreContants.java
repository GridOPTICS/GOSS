package pnnl.goss.server.core;

public class GossCoreContants {
	
	// Confguration file to use
	public static final String PROP_CORE_CONFIG = "pnnl.goss.core";
	
	// Different protocol uris
	public static final String PROP_OPENWIRE_URI = "goss.openwire.uri";
	public static final String PROP_STOMP_URI = "goss.stomp.uri";
	
	// System users for accessing the message broker
	public static final String PROP_SYSTEM_USER = "goss.system.user";
	public static final String PROP_SYSTME_PASSWORD = "goss.system.password";
	
	// LDap configuration
	public static final String PROP_LDAP_URI = "goss.ldap.uri";
	public static final String PROP_LDAP_ADMIN_USER = "goss.ldap.admin.user";
	public static final String PROP_LDAP_ADMIN_PASSWORD = "goss.ldap.admin.password";
	
	// Authorization module enablement
	public static final String PROP_USE_AUTHORIZATION = "goss.use.authorization";
	
}
