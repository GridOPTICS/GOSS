package pnnl.goss.core.server.test.support

import spock.lang.Specification
import static pnnl.goss.core.server.test.support.ActivemqTestBroker.*



class ActivemqTestBrokerSpec extends Specification {
	
	def "when starting test broker"(){
		given: "Broker is instnatiated"
			ActivemqTestBroker broker = new ActivemqTestBroker();
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

}
