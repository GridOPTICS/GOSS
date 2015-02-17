package pnnl.goss.core.client

import javax.jms.Session
import spock.lang.Specification


class ActivemqTestBrokerSpec extends Specification {
	
	ActivemqTestBroker broker;
	
	def setup(){
		broker = new ActivemqTestBroker()
	}
	
	def cleanup(){
		if (broker.status == "STARTED") {
			broker.stop()
		}
		broker = null
	}
	
	def "when starting test broker"(){
		given: "Broker is instnatiated"
			
		expect: "Broker status is STOPPED"
			assert broker.status == "STOPPED"
		when: "startNormal is called"
			broker.startNormal()
		then: "status should be STARTED"
			assert broker.status == "STARTED"
		when: "stop is executed"
			broker.stop()
		then: "Status should be STOPPED"
			assert broker.status == "STOPPED"
		
	}
	
	def "when creating a session"() {
		given: "A broker started as normal"
			broker.startNormal()
		when: "Aquiring a session"
			Session session = broker.createSession()
		then:
			assert session != null
	}
	
	def "when creating a queue"() {
		given: "A broker started as normal"
			broker.startNormal()
		when: "Creating a queue with 'Request' as destination"
			Session session = broker.createQueue("Request")
		then: "Session isn't null"
			assert session != null
	}
}
