package gov.pnnl.goss.server.api;

import gov.pnnl.goss.client.api.GossClientContants;

public class GossServerConstants extends GossClientContants {
	
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


}
