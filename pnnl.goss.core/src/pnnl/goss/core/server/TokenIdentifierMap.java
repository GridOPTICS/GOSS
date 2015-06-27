package pnnl.goss.core.server;

/**
 * TokenIdentifierMap is a container of tokens that have been 
 * authenticated with the user login service.
 *  
 * @author Craig Allwardt
 *
 */
public interface TokenIdentifierMap {
	
	String registerIdentifier(String ip, String identifier);
	
	void registerIdentifier(String ip, String token, String identifier);
	
	String getIdentifier(String ip, String token);
	
}
