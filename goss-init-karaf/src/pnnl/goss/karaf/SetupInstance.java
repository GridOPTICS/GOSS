package pnnl.goss.karaf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.ConnectFuture;

public class SetupInstance {
	
	static ClientChannel channel = null;
	static SshClient client = null;
	static String gossVersion = "";
	
	/**
	 * Expects a format string for the feature string to add i.e. the mvn:pnnl.goss.goss-core-feature/0.1.1/xml/features
	 */
	public final String FEATURE_REPO_ADD = "feature:repo-add %s\n";
	public final String FEATURE_INSTALL = "feature:install %s\n";
	
	
	public void installCoreFeatures(PipedOutputStream pipedIn) throws IOException{
		ArrayList<String> repositories = new ArrayList<String>();
		ArrayList<String> toinstall = new ArrayList<String>();
		
		repositories.add("activemq 5.9.0");
		repositories.add("cxf 2.7.10");
		
		toinstall.add("activemq-broker");
		toinstall.add("war");
		toinstall.add("cxf");
				
		for(String repo:repositories){
			String cmd = String.format(FEATURE_REPO_ADD, repo);
			pipedIn.write(cmd.getBytes());
			pipedIn.flush();
		}
		
		
		for(String install:toinstall){
			String cmd = String.format(FEATURE_INSTALL, install);
			pipedIn.write(cmd.getBytes());
			pipedIn.flush();
		}
		
		
		
	}
	
	public void installGossFeatures(PipedOutputStream pipedIn) throws IOException{
		ArrayList<String> repositories = new ArrayList<String>();
		ArrayList<String> toinstall = new ArrayList<String>();
		
		String namespace = "mvn:pnnl.goss/%s/" + String.format("%s/xml/features", SetupInstance.gossVersion);
				
		toinstall.add("goss-core-feature");
		toinstall.add("goss-powergrid-feature");
		toinstall.add("goss-tool-sharedperspective-feature");
		toinstall.add("goss-dsa-feature");
		toinstall.add("goss-tool-gridpack-feature");
		toinstall.add("goss-tool-mdart-feature");
		toinstall.add("goss-gridmw-feature");
		toinstall.add("goss-fusiondb-feature");
		toinstall.add("goss-kairosdb-feature");
		toinstall.add("goss-demo-feature");
		
		
		for(String install: toinstall){
			repositories.add(String.format(namespace, install));
		}
				
		for(String repo:repositories){
			String cmd = String.format(FEATURE_REPO_ADD, repo);
			pipedIn.write(cmd.getBytes());
			pipedIn.flush();
		}
		
		
		// Only install the first 4 by default.  That gives me access to
		// all the dsa stuff.
		for(int i=0; i<4; i++){
			String cmd = String.format(FEATURE_INSTALL, toinstall.get(i));
			pipedIn.write(cmd.getBytes());
			pipedIn.flush();
		}
		
//		for(String install:toinstall){
//			String cmd = String.format(FEATURE_INSTALL, install);
//			pipedIn.write(cmd.getBytes());
//			pipedIn.flush();
//		}
	}
	

	public static void main(String[] args) {
		if (args.length != 2){
			System.err.println("Invalid arguments must have port and goss version number specified!");
			System.exit(500);
		}
		String host = "localhost";
        int port = Integer.parseInt(args[0]);
        String user = "karaf";
        String password = "karaf";
        SetupInstance.gossVersion = args[1];

        
        try {
            client = SshClient.setUpDefaultClient();
            client.start();
            ConnectFuture future = client.connect(host, port);
            future.await();
            ClientSession session = future.getSession();
            session.authPassword(user, password);
            
            ByteArrayOutputStream sent = new ByteArrayOutputStream();
            PipedOutputStream pipedIn = new LayeredPipedOutputStream(sent);
            channel = session.createChannel("shell");
            channel.setIn(new PipedInputStream(pipedIn));
            channel.setOut(System.out);
            channel.setErr(System.err);
            channel.open().await();
            
            SetupInstance instance = new SetupInstance();
            instance.installCoreFeatures(pipedIn);
            instance.installGossFeatures(pipedIn);
            
            //client.stop();
            channel.waitFor(ClientChannel.CLOSED, 0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        } finally {
            try {
                client.stop();
            } catch (Throwable t) { }
        }
        System.exit(0);
    }

}
