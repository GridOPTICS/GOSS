package pnnl.goss.core.itests;

import static org.amdatu.testing.configurator.TestConfigurator.configuration;

import org.amdatu.testing.configurator.ConfigurationSteps;

import pnnl.goss.core.ClientFactory;

public class TestSteps {
	
	public static ConfigurationSteps configureServerAndClientPropertiesConfig(){
		
		return ConfigurationSteps.create()
				.add(configuration("pnnl.goss.core.server")
					.set("goss.openwire.uri", "tcp://localhost:6000")
					.set("goss.stomp.uri",  "tcp://localhost:6001") //vm:(broker:(tcp://localhost:6001)?persistent=false)?marshal=false")
					.set("goss.start.broker", "true")
					.set("goss.broker.uri", "tcp://localhost:6000"))
				.add(configuration(ClientFactory.CONFIG_PID)
					.set("goss.openwire.uri", "tcp://localhost:6000")
					.set("goss.stomp.uri",  "tcp://localhost:6001"))
				.add(configuration("org.ops4j.pax.logging")
					.set("log4j.rootLogger", "DEBUG, out, osgi:*")
					.set("log4j.throwableRenderer", "org.apache.log4j.OsgiThrowableRenderer")

					//# CONSOLE appender not used by default
					.set("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender")
					.set("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout")
					.set("log4j.appender.stdout.layout.ConversionPattern", "%-5.5p| %c{1} (%L) | %m%n")
					//#server.core.internal.GossRequestHandlerRegistrationImpl", "DEBUG,stdout
					.set("log4j.logger.pnnl.goss", "DEBUG, stdout")
					.set("log4j.logger.org.apache.aries", "INFO")

					//# File appender
					.set("log4j.appender.out", "org.apache.log4j.RollingFileAppender")
					.set("log4j.appender.out.layout", "org.apache.log4j.PatternLayout")
					.set("log4j.appender.out.layout.ConversionPattern", "%d{ISO8601} | %-5.5p | %-16.16t | %-32.32c{1} | %X{bundle.id} - %X{bundle.name} - %X{bundle.version} | %m%n")
					.set("log4j.appender.out.file", "felix.log")
					.set("log4j.appender.out.append", "true")
					.set("log4j.appender.out.maxFileSize", "1MB")
					.set("log4j.appender.out.maxBackupIndex", "10"));
		
	}
	
	public static ConfigurationSteps configureSSLServerAndClientPropertiesConfig(){
		
		return ConfigurationSteps.create()
				.add(configuration("pnnl.goss.core.server")
					.set("goss.ssl.uri", "ssl://localhost:61611")
					.set("goss.start.broker", "true")
					.set("server.keystore", "resources/keystores/mybroker.ks")
					.set("server.keystore.password", "GossServerTemp")
					.set("server.truststore", "")
					.set("server.truststore.password", "")
					.set("client.truststore", "resources/keystores/myclient.ts")
					.set("client.truststore.password", "GossClientTrust")
					.set("client.keystore", "resources/keystores/myclient.ks")
					.set("client.keystore.password", "GossClientTemp")
					.set("ssl.enabled", "true"))
				.add(configuration(ClientFactory.CONFIG_PID)
					.set("goss.ssl.uri", "ssl://localhost:61611")
					.set("client.truststore", "resources/keystores/myclient.ts")
					.set("client.truststore.password", "GossClientTrust")
					.set("ssl.enabled", "true"))
				.add(configuration("org.ops4j.pax.logging")
					.set("log4j.rootLogger", "DEBUG, out, osgi:*")
					.set("log4j.throwableRenderer", "org.apache.log4j.OsgiThrowableRenderer")

					//# CONSOLE appender not used by default
					.set("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender")
					.set("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout")
					.set("log4j.appender.stdout.layout.ConversionPattern", "%-5.5p| %c{1} (%L) | %m%n")
					//#server.core.internal.GossRequestHandlerRegistrationImpl", "DEBUG,stdout
					.set("log4j.logger.pnnl.goss", "DEBUG, stdout")
					.set("log4j.logger.org.apache.aries", "INFO")

					//# File appender
					.set("log4j.appender.out", "org.apache.log4j.RollingFileAppender")
					.set("log4j.appender.out.layout", "org.apache.log4j.PatternLayout")
					.set("log4j.appender.out.layout.ConversionPattern", "%d{ISO8601} | %-5.5p | %-16.16t | %-32.32c{1} | %X{bundle.id} - %X{bundle.name} - %X{bundle.version} | %m%n")
					.set("log4j.appender.out.file", "felix.log")
					.set("log4j.appender.out.append", "true")
					.set("log4j.appender.out.maxFileSize", "1MB")
					.set("log4j.appender.out.maxBackupIndex", "10"));
		
	}

}
