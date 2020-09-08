package pnnl.goss.core.security;

public interface SecurityConfig {
	public String getManagerUser();
	public String getManagerPassword();
	public boolean getUseToken();
	public byte[] getTokenSecret();
}
