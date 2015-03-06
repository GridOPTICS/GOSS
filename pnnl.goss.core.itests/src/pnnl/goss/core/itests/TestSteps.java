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
					.set("goss.stomp.uri",  "tcp://localhost:6001"));
		
	}

}
