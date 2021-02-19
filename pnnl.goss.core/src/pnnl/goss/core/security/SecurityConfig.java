package pnnl.goss.core.security;

import java.util.Set;

import pnnl.goss.core.security.jwt.JWTAuthenticationToken;

public interface SecurityConfig {
	public String getManagerUser();
	public String getManagerPassword();
	public boolean getUseToken();
	public boolean validateToken(String token);
	public JWTAuthenticationToken parseToken(String token);
	public String createToken(Object userId,  Set<String> roles);
}
