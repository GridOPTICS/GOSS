//package pnnl.goss.server.core.specs
//
//import javax.jms.ObjectMessage
//import javax.jms.TextMessage
//import javax.jms.Message
//import javax.jms.Connection
//
//import pnnl.goss.core.Request
//import pnnl.goss.core.server.InvalidConfigurationException
//import pnnl.goss.core.server.GossRequestHandlerRegistrationService
//import pnnl.goss.core.server.internal.GridOpticsServer;
//import pnnl.goss.core.server.internal.ServerListener
//import spock.lang.Specification;
//import static pnnl.goss.core.GossCoreContants.PROP_USE_AUTHORIZATION;
//import static pnnl.goss.security.util.GossSecurityConstants.ROLE_CREDENTIALS;
//
//class ServerListenerSpecs extends Specification {
//
//    def handlerConfig = {
//        GossRequestHandlerRegistrationService regHandler = Mock(GossRequestHandlerRegistrationService)
//        Hashtable<String, Object> config = [PROP_USE_AUTHORIZATION: "true"]
//        ['handler': regHandler, 'config': config]
//    }
//
//
//    def "null-parameter-specs"(){
//        def params = handlerConfig.call()
//
//        when: 'null config object passed'
//        def listener = new ServerListener(null, params.handler)
//
//        then:
//        def e=thrown(IllegalArgumentException)
//
//        when: 'null handlerservice passed'
//        listener = new ServerListener(params.config, null)
//
//        then:
//        thrown(IllegalArgumentException)
//    }
//
//    def "handling-request-spec"(){
//        def params = handlerConfig()
//
//        def listener=new ServerListener(params.config, params.handler)
//        def connection = Mock(Connection)
//
//        def server=  Mock(GridOpticsServer)
//
//        def request = Mock(Request)
//        def message = Mock(ObjectMessage) {
//            getObject() >> request
//        }
//
//        when: "Sending a regular request"
//        listener.onMessage(message)
//        then:
//        notThrown(Exception)
//
//
//
//
//    }
//
//}
