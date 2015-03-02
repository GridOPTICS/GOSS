package pnnl.goss.core.security;

import java.util.Set;

public interface PermissionAdapter {
	
	Set<String> getPermissions(String identifier);

}
