import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.client.Client;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossClient.PROTOCOL;
import pnnl.goss.fusiondb.launchers.DataStreamLauncher;


public class DataStreamLauncherTest {

	static Client client = new GossClient(new UsernamePasswordCredentials("pmu_user", "password"), PROTOCOL.STOMP);
	
	public static void main(String[] args){
		
		DataStreamLauncher launcher = new DataStreamLauncher();
		launcher.startLauncher();
		
	}

}
