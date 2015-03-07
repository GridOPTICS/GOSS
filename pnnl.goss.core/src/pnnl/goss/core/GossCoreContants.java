package pnnl.goss.core;

public class GossCoreContants {
	
//	// Confguration file to use
//	public static final String PROP_CORE_CONFIG = "pnnl.goss.core";
//	public static final String PROP_CORE_CLIENT_CONFIG = "pnnl.goss.core.client";
	
	// Different protocol uris
	public static final String PROP_OPENWIRE_URI = "goss.openwire.uri";
	public static final String PROP_STOMP_URI = "goss.stomp.uri";
	
	// System users for accessing the message broker
	public static final String PROP_SYSTEM_USER = "goss.system.user";
	public static final String PROP_SYSTEM_PASSWORD = "goss.system.password";
	
	// LDap configuration
	public static final String PROP_LDAP_URI = "goss.ldap.uri";
	public static final String PROP_LDAP_ADMIN_USER = "goss.ldap.admin.user";
	public static final String PROP_LDAP_ADMIN_PASSWORD = "goss.ldap.admin.password";
	
	// Authorization module enablement
	public static final String PROP_USE_AUTHORIZATION = "goss.use.authorization";
	
	// Config file to monitor datasources.
	public static final String PROP_DATASOURCES_CONFIG = "pnnl.goss.datasources";
	
	// Config file used to start broker in standalone mode
	public static final String PROP_ACTIVEMQ_CONFIG = "pnnl.goss.activemq.config";
	
	// Topic that requests will be sent from the client to the server on
	public static final String PROP_REQUEST_QUEUE = "pnnl.goss.request.topic";
	
}
