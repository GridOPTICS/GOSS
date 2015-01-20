//package pnnl.goss.itest.specs
//
//import static pnnl.goss.core.GossCoreContants.PROP_ACTIVEMQ_CONFIG;
//import static pnnl.goss.core.GossCoreContants.PROP_USE_AUTHORIZATION
//import static pnnl.goss.core.GossCoreContants.PROP_OPENWIRE_URI;
//import static pnnl.goss.core.GossCoreContants.PROP_STOMP_URI;
//
//import org.apache.http.auth.UsernamePasswordCredentials
//import pnnl.goss.core.DataResponse
//import pnnl.goss.core.client.internal.GossClient
//import pnnl.goss.core.server.internal.GossDataServicesImpl
//import pnnl.goss.core.server.internal.GossRequestHandlerRegistrationImpl
//import pnnl.goss.core.server.internal.GridOpticsServer
//import pnnl.goss.itest.requests.EchoAuthorizedRequest
//import pnnl.goss.itest.requests.EchoRequest
//import spock.lang.Specification
//
//class SecuredEchoResponseSpec extends Specification {
//
//    static GossClient client
//    static GridOpticsServer server;
//    static GossRequestHandlerRegistrationImpl registrationHandler
//    static GossDataServicesImpl dataServices
//
//    def "secured request response specifications"(){
//        expect:
//            assert client != null
//        when: "getting response from hello world EchoRequest"
//            DataResponse response = client.getResponse(new EchoAuthorizedRequest("hello world"))
//        then:
//            assert response != null
//            assert response.data == "hello world"
//            assert response.isResponseComplete() == true
//    }
//
//    def setupSpec() {
//        dataServices  = new GossDataServicesImpl()
//        registrationHandler = new GossRequestHandlerRegistrationImpl(dataServices)
//        Properties config = new Properties()
//        config.setProperty(PROP_OPENWIRE_URI, "tcp://0.0.0.0:51515")
//        config.setProperty(PROP_STOMP_URI, "stomp://0.0.0.0:51516")
//        // TODO Make Auth Work Properly!  (Set to true and handle all other instances of Make Auth Work Properly!
//        config.setProperty(PROP_USE_AUTHORIZATION, "false")
//
//        // TODO Make Auth Work Properly!
//        // config.setProperty(PROP_ACTIVEMQ_CONFIG, "build/resources/test/test-broker-secured.xml")
//        config.setProperty(PROP_ACTIVEMQ_CONFIG, "build/resources/test/test-broker-no-security.xml")
//        // config.setProperty(GossCoreContants.PROP_SYSTEM_USER, "fuzzy")
//        // config.setProperty(GossCoreContants.PROP_SYSTEM_PASSWORD, "buckets")
//        registrationHandler.setCoreServerConfig(config)
//        registrationHandler.addHandlersFromClassPath();
//        server = new GridOpticsServer(registrationHandler, true)
//        client = new GossClient(config)
//        // TODO Make Auth Work Properly!
//        //client.setCredentials(new UsernamePasswordCredentials("test_user", "test_pass"))
//
//    }
//
//    def cleanupSpec() {
//        try{
//            client.close()
//        }
//        finally{
//            client = null
//        }
//        try{
//            registrationHandler.shutdown()
//        }
//        finally{
//            registrationHandler = null
//        }
//
//        try{
//            server.close()
//        }
//        finally{
//            dataServices = null
//        }
//
//    }
//
//}
