package pnnl.goss.core;

public class GossCoreContants {
	
//	// Confguration file to use
//	public static final String PROP_CORE_CONFIG = "pnnl.goss.core";
//	public static final String PROP_CORE_CLIENT_CONFIG = "pnnl.goss.core.client";
	
	// Different protocol uris
	public static final String PROP_OPENWIRE_URI = "goss.openwire.uri";
	public static final String PROP_STOMP_URI = "goss.stomp.uri";
	public static final String PROP_SSL_ENABLED = "ssl.enabled";
	public static final String PROP_SSL_URI = "goss.ssl.uri";
	public static final String PROP_SSL_CLIENT_TRUSTSTORE = "client.truststore";
	public static final String PROP_SSL_CLIENT_TRUSTSTORE_PASSWORD = "client.truststore.password";
		
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
	
	public static final String PROP_TICK_TOPIC = "pnnl.goss.tick.topic";
	
	// Topic that requests for tokens will be sent from the client to the server on
	public static final String PROP_TOKEN_QUEUE = "pnnl.goss.token.topic";
}
