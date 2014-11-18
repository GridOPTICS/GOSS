package pnnl.goss.itest.specs

import pnnl.goss.core.DataResponse
import pnnl.goss.core.client.internal.GossClient
import pnnl.goss.core.server.internal.GossDataServicesImpl
import pnnl.goss.core.server.internal.GossRequestHandlerRegistrationImpl
import pnnl.goss.core.server.internal.GridOpticsServer
import pnnl.goss.itest.requests.EchoRequest
import spock.lang.Specification

class EchoRequestResponseSpecs extends Specification {
	
	GossClient client
	GridOpticsServer server;
	GossDataServicesImpl dataServices
	GossRequestHandlerRegistrationImpl registrationHandler
	
	def "request response specifications"(){
		expect:
			assert client != null
		when: "getting response from hello world EchoRequest"
			DataResponse response = client.getResponse(new EchoRequest("hello world"))
		then:
			assert response != null
			assert response.data == "hello world"
			assert response.isResponseComplete() == true
	}
	
	
	def setup() {
		dataServices  = new GossDataServicesImpl()
		registrationHandler = new GossRequestHandlerRegistrationImpl(dataServices)
		Properties config = new Properties()
		config.setProperty("goss.openwire.uri", "vm://echotestopenwire")
		config.setProperty("goss.stomp.uri", "vm://echoteststomp")
		// config.setProperty(GossCoreContants.PROP_SYSTEM_USER, "fuzzy")
		// config.setProperty(GossCoreContants.PROP_SYSTEM_PASSWORD, "buckets")
		registrationHandler.setCoreServerConfig(config)
		registrationHandler.addHandlersFromClassPath();
		server = new GridOpticsServer(registrationHandler, true)
		client = new GossClient(config)
	}
	
	def cleanup() {
		client.close()
		client = null
		registrationHandler.shutdown()
		registrationHandler = null
		server.close()
		dataServices = null
		
	}

}
