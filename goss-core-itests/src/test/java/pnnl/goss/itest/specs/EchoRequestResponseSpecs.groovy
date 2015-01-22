package pnnl.goss.itest.specs

import static pnnl.goss.core.GossCoreContants.PROP_ACTIVEMQ_CONFIG;
import static pnnl.goss.core.GossCoreContants.PROP_OPENWIRE_URI;
import static pnnl.goss.core.GossCoreContants.PROP_STOMP_URI;
import pnnl.goss.core.Client
import pnnl.goss.core.DataResponse
import pnnl.goss.core.client.internal.GossClient
import pnnl.goss.core.server.BasicDataSourceCreator;
import pnnl.goss.core.server.internal.GossDataServicesImpl
import pnnl.goss.core.server.internal.GossRequestHandlerRegistrationImpl
import pnnl.goss.core.server.internal.GridOpticsServer
import pnnl.goss.itest.requests.EchoRequest
import spock.lang.Specification

class EchoRequestResponseSpecs extends Specification {

    Client client
    GridOpticsServer server;
    GossRequestHandlerRegistrationImpl registrationHandler
    GossDataServicesImpl dataServices

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
        dataServices  = new GossDataServicesImpl(Mock(BasicDataSourceCreator))
        registrationHandler = new GossRequestHandlerRegistrationImpl(dataServices)
        Properties config = new Properties()
        config.setProperty(PROP_OPENWIRE_URI, "tcp://0.0.0.0:51515")
        config.setProperty(PROP_STOMP_URI, "stomp://0.0.0.0:51516")
        //def file = new File(getClass().protectionDomain.codeSource.location.path).parent
//        println file
        config.setProperty(PROP_ACTIVEMQ_CONFIG, "build/resources/test/test-broker-no-security.xml")
        // config.setProperty(GossCoreContants.PROP_SYSTEM_USER, "fuzzy")
        // config.setProperty(GossCoreContants.PROP_SYSTEM_PASSWORD, "buckets")
        registrationHandler.setCoreServerConfig(config)
        registrationHandler.addHandlersFromClassPath();
        server = new GridOpticsServer(registrationHandler, true)
        client = new GossClient(config)

    }

    def cleanup() {
        try{
            client.close()
        }
        finally{
            client = null
        }
        try{
            registrationHandler.shutdown()
        }
        finally{
            registrationHandler = null
        }

        try{
            server.close()
        }
        finally{
            dataServices = null
        }

    }

}
