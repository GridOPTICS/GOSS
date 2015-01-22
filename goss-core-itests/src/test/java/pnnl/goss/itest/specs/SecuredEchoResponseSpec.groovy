package pnnl.goss.itest.specs

import static pnnl.goss.core.GossCoreContants.PROP_ACTIVEMQ_CONFIG;
import static pnnl.goss.core.GossCoreContants.PROP_USE_AUTHORIZATION
import static pnnl.goss.core.GossCoreContants.PROP_OPENWIRE_URI;
import static pnnl.goss.core.GossCoreContants.PROP_STOMP_URI;
import static pnnl.goss.security.util.GossSecurityConstants.PROP_USE_AUTH
import static pnnl.goss.security.util.GossSecurityConstants.PROP_SYSTEM_PW
import static pnnl.goss.core.GossCoreContants.PROP_SYSTEM_USER
import static pnnl.goss.core.GossCoreContants.PROP_SYSTEM_PASSWORD



import javax.jms.JMSException
import javax.jms.JMSSecurityException
import org.apache.http.auth.UsernamePasswordCredentials

import pnnl.goss.core.DataError
import pnnl.goss.core.DataResponse
import pnnl.goss.core.GossCoreContants;
import pnnl.goss.core.Client.PROTOCOL
import pnnl.goss.core.client.internal.GossClient
import pnnl.goss.core.server.internal.GossDataServicesImpl
import pnnl.goss.core.server.internal.GossRequestHandlerRegistrationImpl
import pnnl.goss.core.server.internal.GridOpticsServer
import pnnl.goss.itest.requests.EchoAuthorizedRequest
import pnnl.goss.itest.requests.EchoRequest
import pnnl.goss.security.core.internal.GossSecurityHandlerImpl
import spock.lang.Specification

class SecuredEchoResponseSpec extends Specification {

    GossClient client
    GridOpticsServer server;
    GossRequestHandlerRegistrationImpl registrationHandler
    GossDataServicesImpl dataServices
    static final String OPENWIRE = "tcp://0.0.0.0:51515"
    static final String STOMP = "tcp://0.0.0.0:51516"

    def setupClient(PROTOCOL protocol, String username, String password){
        Properties clientConfig = new Properties()
        clientConfig.setProperty(PROP_OPENWIRE_URI, OPENWIRE)
        clientConfig.setProperty(PROP_STOMP_URI, STOMP)
        client = new GossClient(clientConfig, protocol)
        client.setCredentials(new UsernamePasswordCredentials(username, password))
    }

    def "secured request response specifications credentials"(){
        def username = "test_user"
        setupClient(PROTOCOL.OPENWIRE, username, "wrong_pass")

        expect:
            assert client != null
        when: "getting response from hello world EchoRequest"
            DataResponse response = client.getResponse(new EchoAuthorizedRequest("hello world"))
        then:
            def ex = thrown(JMSSecurityException)
            assert ex != null
            ex.message == "User name [${username}] or password is invalid."
    }


    def "secured request response specifications"(){
        def username = "test_user"
        setupClient(PROTOCOL.OPENWIRE, username, "test_pass")

        expect:
            assert client != null
            assert server != null
        when: "getting response from hello world"
            DataResponse response = client.getResponse(new EchoAuthorizedRequest("hello world"))
        then:
            assert response != null
            assert response.data == "hello world"
            assert response.isResponseComplete() == true
//        where:
//            p << [PROTOCOL.OPENWIRE,PROTOCOL.STOMP]
    }

    def "secured request response specifications invalid role"(){
        def username = "test_user2"
        setupClient(PROTOCOL.OPENWIRE, username, "test_pass")

        expect:
            assert client != null
        when: "getting response from hello world EchoAuthorizedRequest"
            DataResponse response = client.getResponse(new EchoAuthorizedRequest("hello world"))
        then:

            assert response != null
            assert response.data instanceof DataError
            DataError err = (DataError)response.data
            assert err.message == 'Access Denied for the requested data'
            assert response.isResponseComplete() == true
    }

    def setup() {
        dataServices  = new GossDataServicesImpl()
        registrationHandler = new GossRequestHandlerRegistrationImpl(dataServices,
            new GossSecurityHandlerImpl())
        Properties config = new Properties()
        Properties clientConfig = new Properties()
        config.setProperty(PROP_OPENWIRE_URI, OPENWIRE)
        config.setProperty(PROP_STOMP_URI, STOMP)

        clientConfig.setProperty(PROP_OPENWIRE_URI, OPENWIRE)
        clientConfig.setProperty(PROP_STOMP_URI, "stomp://0.0.0.0:51516")

        // TODO Make Auth Work Properly!  (Set to true and handle all other instances of Make Auth Work Properly!
        config.setProperty(PROP_USE_AUTHORIZATION, "true")
        config.setProperty(PROP_SYSTEM_USER, "test_user")
        config.setProperty(PROP_SYSTEM_PASSWORD, "test_pass")
        config.setProperty(PROP_USE_AUTH, "true")

        // TODO Make Auth Work Properly!
        config.setProperty(PROP_ACTIVEMQ_CONFIG, "build/resources/test/test-broker-secured.xml")
        //config.setProperty(PROP_ACTIVEMQ_CONFIG, "build/resources/test/test-broker-no-security.xml")
        // config.setProperty(GossCoreContants.PROP_SYSTEM_USER, "fuzzy")
        // config.setProperty(GossCoreContants.PROP_SYSTEM_PASSWORD, "buckets")
        registrationHandler.setCoreServerConfig(config)
        registrationHandler.addHandlersFromClassPath();
        server = new GridOpticsServer(registrationHandler, config, true)
    }

    def cleanup() {
        try{
            if (client != null){
                client.close()
            }
        }
        finally{
            client = null
        }
        try{
            if (registrationHandler != null){
                registrationHandler.shutdown()
            }
        }
        finally{
            registrationHandler = null
        }

        try{
            if (server != null){
                server.close()
            }
        }
        finally{
            server = null
            dataServices = null
        }

    }

}
