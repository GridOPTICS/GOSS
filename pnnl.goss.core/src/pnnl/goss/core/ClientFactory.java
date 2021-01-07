package pnnl.goss.core;

import java.util.Map;

import org.apache.http.auth.Credentials;

import pnnl.goss.core.Client.PROTOCOL;

public interface ClientFactory {
	
	static final String CONFIG_PID = "pnnl.goss.core.client";
	static final String DEFAULT_OPENWIRE_URI = "goss.openwire.uri";
	static final String DEFAULT_STOMP_URI = "goss.stomp.uri";

    /**
     * Creates a client instance that can be used to connect to goss.
     *
     * @param protocol
     * @return
     */
    Client create(PROTOCOL protocol, Credentials credentials) throws Exception ;
    

    /**
     * Creates a client instance that can be used to connect to goss.
     *
     * @param protocol
     * @return
     */
    Client create(PROTOCOL protocol, Credentials credentials, boolean useToken) throws Exception ;

    /**
     * Retrieve a client instance from a uuid.  If not available then returns
     * null.
     *
     * @param uuid
     * @return
     */
    Client get(String uuid);
    
    Map<String, PROTOCOL> list();

    /**
     * Destroy all client instances that have been created with the factory.
     */
    void destroy();

}
