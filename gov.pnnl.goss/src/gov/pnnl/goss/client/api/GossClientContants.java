package gov.pnnl.goss.client.api;


public class GossClientContants {
	
//	// Confguration file to use
//	public static final String PROP_CORE_CONFIG = "gov.pnnl.goss.core";
//	public static final String PROP_CORE_CLIENT_CONFIG = "gov.pnnl.goss.core.client";
	
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
		
	// Topic that requests will be sent from the client to the server on
	public static final String PROP_REQUEST_QUEUE = "pnnl.goss.request.topic";
	
	public static final String PROP_TICK_TOPIC = "pnnl.goss.tick.topic";
}
