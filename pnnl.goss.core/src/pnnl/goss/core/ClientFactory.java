package pnnl.goss.core;

import java.util.Map;

import pnnl.goss.core.Client.PROTOCOL;

public interface ClientFactory {
	
	static final String CONFIG_PID = "pnnl.goss.core.client";

    /**
     * Creates a client instance that can be used to connect to goss.
     *
     * @param protocol
     * @return
     */
    Client create(PROTOCOL protocol);

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
