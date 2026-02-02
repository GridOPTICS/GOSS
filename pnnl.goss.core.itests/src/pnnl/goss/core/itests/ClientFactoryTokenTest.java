package pnnl.goss.core.itests;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.client.ClientServiceFactory;
import pnnl.goss.core.client.GossClient;

/**
 * Tests for ClientFactory token authentication support. Verifies that clients
 * can be created with and without token authentication.
 */
public class ClientFactoryTokenTest {

    private static final String OPENWIRE_URI = "tcp://localhost:61616";
    private static final String STOMP_URI = "tcp://localhost:61613";

    private ClientServiceFactory clientFactory;

    @BeforeEach
    public void setUp() throws Exception {
        clientFactory = new ClientServiceFactory();
        clientFactory.setOpenwireUri(OPENWIRE_URI);
    }

    @AfterEach
    public void tearDown() {
        if (clientFactory != null) {
            clientFactory.destroy();
        }
    }

    @Test
    public void testCreateClientWithoutToken() throws Exception {
        // Test creating client without token (default behavior)
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("system", "manager");
        Client client = clientFactory.create(PROTOCOL.OPENWIRE, credentials);

        assertNotNull(client, "Client should be created");
        assertTrue(client instanceof GossClient, "Client should be GossClient instance");

        // Verify the client was created and has an ID
        GossClient gossClient = (GossClient) client;
        assertNotNull(gossClient.getClientId(), "Client should have an ID");
    }

    @Test
    public void testCreateClientWithTokenFalse() throws Exception {
        // Test explicitly setting useToken to false
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("system", "manager");
        Client client = clientFactory.create(PROTOCOL.OPENWIRE, credentials, false);

        assertNotNull(client, "Client should be created");
        assertTrue(client instanceof GossClient, "Client should be GossClient instance");

        GossClient gossClient = (GossClient) client;
        assertNotNull(gossClient.getClientId(), "Client should have an ID");
    }

    @Test
    public void testCreateClientWithTokenTrue() throws Exception {
        // Test creating client with token authentication enabled
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("system", "manager");
        Client client = clientFactory.create(PROTOCOL.OPENWIRE, credentials, true);

        assertNotNull(client, "Client should be created");
        assertTrue(client instanceof GossClient, "Client should be GossClient instance");

        GossClient gossClient = (GossClient) client;
        assertNotNull(gossClient.getClientId(), "Client should have an ID");

        // Note: Actual token validation would require a running GOSS server with token
        // support
        // This test verifies that the client is created with the useToken flag
    }

    @Test
    public void testGossClientConstructorWithToken() {
        // Test direct GossClient constructor with token parameter
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("system", "manager");

        // Test with useToken = false
        GossClient clientWithoutToken = new GossClient(PROTOCOL.OPENWIRE, credentials, OPENWIRE_URI, STOMP_URI,
                false);
        assertNotNull(clientWithoutToken, "Client without token should be created");
        assertNotNull(clientWithoutToken.getClientId(), "Client should have an ID");

        // Test with useToken = true
        GossClient clientWithToken = new GossClient(PROTOCOL.OPENWIRE, credentials, OPENWIRE_URI, STOMP_URI, true);
        assertNotNull(clientWithToken, "Client with token should be created");
        assertNotNull(clientWithToken.getClientId(), "Client should have an ID");
    }

    @Test
    public void testGossClientConstructorBackwardCompatibility() {
        // Test that old constructor (without token param) still works
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("system", "manager");

        GossClient client = new GossClient(PROTOCOL.OPENWIRE, credentials, OPENWIRE_URI, STOMP_URI);
        assertNotNull(client, "Client should be created with old constructor");
        assertNotNull(client.getClientId(), "Client should have an ID");

        // This should default to useToken = false
    }

    @Test
    public void testMultipleClientsWithDifferentTokenSettings() throws Exception {
        // Test creating multiple clients with different token settings
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("system", "manager");

        Client client1 = clientFactory.create(PROTOCOL.OPENWIRE, credentials, false);
        Client client2 = clientFactory.create(PROTOCOL.OPENWIRE, credentials, true);
        Client client3 = clientFactory.create(PROTOCOL.OPENWIRE, credentials, false);

        assertNotNull(client1, "First client should be created");
        assertNotNull(client2, "Second client should be created");
        assertNotNull(client3, "Third client should be created");

        // Cast to GossClient to access getClientId()
        GossClient gossClient1 = (GossClient) client1;
        GossClient gossClient2 = (GossClient) client2;
        GossClient gossClient3 = (GossClient) client3;

        // All clients should have different IDs
        assertNotEquals(gossClient1.getClientId(), gossClient2.getClientId(),
                "Clients should have different IDs");
        assertNotEquals(gossClient2.getClientId(), gossClient3.getClientId(),
                "Clients should have different IDs");
        assertNotEquals(gossClient1.getClientId(), gossClient3.getClientId(),
                "Clients should have different IDs");
    }
}
