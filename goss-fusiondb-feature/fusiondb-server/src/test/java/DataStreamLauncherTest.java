import org.apache.http.auth.UsernamePasswordCredentials;

import goss.pnnl.fusiondb.launchers.DataStreamLauncher;
import pnnl.goss.core.client.Client;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossClient.PROTOCOL;


public class DataStreamLauncherTest {

	static Client client = new GossClient(new UsernamePasswordCredentials("pmu_user", "password"), PROTOCOL.STOMP);
	
	public static void main(String[] args){
		
		DataStreamLauncher launcher = new DataStreamLauncher();
		launcher.startLauncher();
		
	}

}
