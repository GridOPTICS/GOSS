package pnnl.goss.core.testutil;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration utilities for GOSS integration tests.
 * Provides standard configuration maps that can be used with OSGi ConfigurationAdmin.
 * 
 * @author Craig Allwardt
 */
public class CoreConfigSteps {
	
	/**
	 * Minimal configuration for GOSS server
	 * @return Map of configuration properties
	 */
	public static Map<String, Object> getServerConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("goss.openwire.uri", "tcp://localhost:6000");
		config.put("goss.stomp.uri", "stomp://localhost:6001");
		config.put("goss.ws.uri", "ws://localhost:6002");
		config.put("goss.start.broker", "true");
		config.put("goss.broker.uri", "tcp://localhost:6000");
		return config;
	}
	
	/**
	 * Minimal configuration for GOSS client
	 * @return Map of configuration properties
	 */
	public static Map<String, Object> getClientConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("goss.openwire.uri", "tcp://localhost:6000");
		config.put("goss.stomp.uri", "stomp://localhost:6001");
		config.put("goss.ws.uri", "ws://localhost:6002");
		return config;
	}
	
	/**
	 * Logging configuration
	 * @return Map of logging properties
	 */
	public static Map<String, Object> getLoggingConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("log4j.rootLogger", "DEBUG, out, osgi:*");
		config.put("log4j.throwableRenderer", "org.apache.log4j.OsgiThrowableRenderer");
		config.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
		config.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
		config.put("log4j.appender.stdout.layout.ConversionPattern", "%-5.5p| %c{1} (%L) | %m%n");
		config.put("log4j.logger.pnnl.goss", "DEBUG, stdout");
		config.put("log4j.logger.org.apache.aries", "INFO");
		config.put("log4j.appender.out", "org.apache.log4j.RollingFileAppender");
		config.put("log4j.appender.out.layout", "org.apache.log4j.PatternLayout");
		config.put("log4j.appender.out.layout.ConversionPattern", 
			"%d{ISO8601} | %-5.5p | %-16.16t | %-32.32c{1} | %X{bundle.id} - %X{bundle.name} - %X{bundle.version} | %m%n");
		config.put("log4j.appender.out.file", "felix.log");
		config.put("log4j.appender.out.append", "true");
		config.put("log4j.appender.out.maxFileSize", "1MB");
		config.put("log4j.appender.out.maxBackupIndex", "10");
		return config;
	}
	
	/**
	 * SSL configuration for server
	 * @return Map of SSL server properties
	 */
	public static Map<String, Object> getSSLServerConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("goss.ssl.uri", "ssl://localhost:61611");
		config.put("goss.start.broker", "true");
		config.put("server.keystore", "resources/keystores/mybroker.ks");
		config.put("server.keystore.password", "GossServerTemp");
		config.put("server.truststore", "");
		config.put("server.truststore.password", "");
		config.put("client.truststore", "resources/keystores/myclient.ts");
		config.put("client.truststore.password", "GossClientTrust");
		config.put("client.keystore", "resources/keystores/myclient.ks");
		config.put("client.keystore.password", "GossClientTemp");
		config.put("ssl.enabled", "true");
		return config;
	}
	
	/**
	 * SSL configuration for client
	 * @return Map of SSL client properties
	 */
	public static Map<String, Object> getSSLClientConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("goss.ssl.uri", "ssl://localhost:61611");
		config.put("client.truststore", "resources/keystores/myclient.ts");
		config.put("client.truststore.password", "GossClientTrust");
		config.put("ssl.enabled", "true");
		return config;
	}
	
	/**
	 * Convert Map to Dictionary for OSGi ConfigurationAdmin
	 */
	public static Dictionary<String, Object> toDictionary(Map<String, Object> map) {
		Dictionary<String, Object> dict = new Hashtable<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			dict.put(entry.getKey(), entry.getValue());
		}
		return dict;
	}
}
