package pnnl.goss.core.security;

import java.util.Set;

import org.apache.shiro.realm.Realm;

public interface GossRealm extends Realm {
	
	Set<String> getPermissions(String identifier);
	
	boolean hasIdentifier(String identifier);
	
}
