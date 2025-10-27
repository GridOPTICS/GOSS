package pnnl.goss.core.runner;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.usage.SystemUsage;

import java.net.URI;

/**
 * Simple GOSS Runner - No OSGi, just plain Java This bypasses all the OSGi
 * complexity and just starts the core services
 */
public class GossSimpleRunner {

	private BrokerService brokerService;

	public static void main(String[] args) {
		System.out.println("Starting GOSS Simple Runner...");

		GossSimpleRunner runner = new GossSimpleRunner();

		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down GOSS...");
			runner.stop();
		}));

		try {
			runner.start();
			System.out.println("GOSS Simple Runner started successfully!");
			System.out.println("Press Ctrl+C to stop");

			// Keep running
			Thread.currentThread().join();

		} catch (Exception e) {
			System.err.println("Failed to start GOSS: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void start() throws Exception {
		System.out.println("Starting ActiveMQ Broker...");
		startBroker();

		System.out.println("Security: Using default (no authentication)");

		System.out.println("GOSS Core services are running");
		System.out.println("ActiveMQ Broker: tcp://0.0.0.0:61617");
		System.out.println("STOMP: tcp://0.0.0.0:61618");
		System.out.println("WebSocket: disabled (to avoid Jetty dependencies)");
	}

	public void stop() {
		try {
			if (brokerService != null) {
				brokerService.stop();
			}
			// No security manager to clean up
		} catch (Exception e) {
			System.err.println("Error stopping GOSS: " + e.getMessage());
		}
	}

	private void startBroker() throws Exception {
		brokerService = new BrokerService();
		brokerService.setBrokerName("goss-broker");
		brokerService.setDataDirectory("data");

		// Configure system usage
		SystemUsage systemUsage = brokerService.getSystemUsage();
		systemUsage.getMemoryUsage().setLimit(64 * 1024 * 1024); // 64MB
		systemUsage.getStoreUsage().setLimit(1024 * 1024 * 1024); // 1GB

		// Add connectors with different ports
		TransportConnector openwireConnector = new TransportConnector();
		openwireConnector.setUri(new URI("tcp://0.0.0.0:61617"));
		openwireConnector.setName("openwire");
		brokerService.addConnector(openwireConnector);

		TransportConnector stompConnector = new TransportConnector();
		stompConnector.setUri(new URI("stomp://0.0.0.0:61618"));
		stompConnector.setName("stomp");
		brokerService.addConnector(stompConnector);

		// WebSocket connector removed - requires Jetty dependencies

		brokerService.start();
	}

}
