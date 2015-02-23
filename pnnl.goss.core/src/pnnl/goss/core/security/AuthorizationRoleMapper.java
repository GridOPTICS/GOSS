package pnnl.goss.core.security;

import java.util.List;
import java.util.Map;

public interface AuthorizationRoleMapper {
	List<String> getRolesForUser(String identifier);
}
