package pnnl.goss.core.runner;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.usage.SystemUsage;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;

/**
 * GOSS SSL Runner - Secure version with SSL/TLS support This provides encrypted
 * connections for production environments
 */
public class GossSSLRunner {

    private BrokerService brokerService;

    // SSL Configuration - update these paths for your environment
    private static final String KEYSTORE_PATH = "conf/keystores/server.jks";
    private static final String KEYSTORE_PASSWORD = "changeit";
    private static final String TRUSTSTORE_PATH = "conf/keystores/trust.jks";
    private static final String TRUSTSTORE_PASSWORD = "changeit";

    public static void main(String[] args) {
        System.out.println("Starting GOSS SSL Runner...");

        GossSSLRunner runner = new GossSSLRunner();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down GOSS SSL Runner...");
            runner.stop();
        }));

        try {
            runner.start();
            System.out.println("GOSS SSL Runner started successfully!");
            System.out.println("SSL connections enabled for secure communication");
            System.out.println("Press Ctrl+C to stop");

            // Keep running
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start GOSS SSL Runner: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() throws Exception {
        System.out.println("Starting ActiveMQ Broker with SSL/TLS...");
        startSecureBroker();

        System.out.println("GOSS SSL services are running");
        System.out.println("SSL OpenWire: ssl://0.0.0.0:61443");
        System.out.println("SSL STOMP: stomp+ssl://0.0.0.0:61444");
        System.out.println("Regular OpenWire: disabled for security");
        System.out.println("Regular STOMP: disabled for security");
    }

    public void stop() {
        try {
            if (brokerService != null) {
                brokerService.stop();
            }
        } catch (Exception e) {
            System.err.println("Error stopping GOSS SSL Runner: " + e.getMessage());
        }
    }

    private void startSecureBroker() throws Exception {
        brokerService = new BrokerService();
        brokerService.setBrokerName("goss-ssl-broker");
        brokerService.setDataDirectory("data");

        // Configure system usage
        SystemUsage systemUsage = brokerService.getSystemUsage();
        systemUsage.getMemoryUsage().setLimit(64 * 1024 * 1024); // 64MB
        systemUsage.getStoreUsage().setLimit(1024 * 1024 * 1024); // 1GB

        // Configure SSL Context
        SslContext sslContext = createSSLContext();
        brokerService.setSslContext(sslContext);

        // Add SSL connectors only
        TransportConnector sslOpenwireConnector = new TransportConnector();
        sslOpenwireConnector.setUri(new URI("ssl://0.0.0.0:61443"));
        sslOpenwireConnector.setName("ssl-openwire");
        brokerService.addConnector(sslOpenwireConnector);

        TransportConnector sslStompConnector = new TransportConnector();
        sslStompConnector.setUri(new URI("stomp+ssl://0.0.0.0:61444"));
        sslStompConnector.setName("ssl-stomp");
        brokerService.addConnector(sslStompConnector);

        brokerService.start();
    }

    private SslContext createSSLContext() throws Exception {
        // Load keystore (server certificate and private key)
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream keyStoreStream = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(keyStoreStream, KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
            System.err.println("Warning: Could not load keystore from " + KEYSTORE_PATH);
            System.err.println("Using default self-signed certificate.");
            System.err.println("For production, create proper SSL certificates.");
            // Create a default keystore for demo purposes
            keyStore = createDefaultKeyStore();
        }

        // Load truststore (trusted client certificates)
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream trustStoreStream = new FileInputStream(TRUSTSTORE_PATH)) {
            trustStore.load(trustStoreStream, TRUSTSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
            System.out.println("Using keystore as truststore (self-signed setup)");
            trustStore = keyStore; // Use same keystore as truststore for self-signed
        }

        // Initialize key manager
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // Initialize trust manager
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        // Create SSL context
        SslContext sslContext = new SslContext(keyManagers, trustManagers, null);

        return sslContext;
    }

    private KeyStore createDefaultKeyStore() throws Exception {
        System.out.println("Creating default self-signed certificate for testing...");

        // For production, replace this with proper certificate loading
        // This is a minimal implementation for demo purposes
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null); // Initialize empty keystore

        System.out.println("WARNING: Using empty keystore - SSL will not work properly!");
        System.out.println("Please provide proper SSL certificates in " + KEYSTORE_PATH);

        return keyStore;
    }
}
