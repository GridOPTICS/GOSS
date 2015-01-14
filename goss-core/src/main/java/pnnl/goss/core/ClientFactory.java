package pnnl.goss.core;

import pnnl.goss.core.Client.PROTOCOL;

public interface ClientFactory {

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

    /**
     * Destroy all client instances that have been created with the factory.
     */
    void destroy();

}
