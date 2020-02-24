package pnnl.goss.core.security;

public interface GossLoginManager {
	
	public boolean login(String username, byte[] password);
	public boolean tokenLogin(String token);
	public String getToken(String username, byte[] password);

}
