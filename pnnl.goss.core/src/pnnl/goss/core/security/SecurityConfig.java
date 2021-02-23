package pnnl.goss.core.security;

import java.util.Set;

public interface SecurityConfig {
	public String getManagerUser();
	public String getManagerPassword();
	public boolean getUseToken();
	public boolean validateToken(String token);
	public JWTAuthenticationToken parseToken(String token);
	public String createToken(Object userId,  Set<String> roles);
}
