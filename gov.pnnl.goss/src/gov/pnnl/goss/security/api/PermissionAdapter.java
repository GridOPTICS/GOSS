package gov.pnnl.goss.security.api;

import java.util.Set;

public interface PermissionAdapter {
	
	Set<String> getPermissions(String identifier);

}
