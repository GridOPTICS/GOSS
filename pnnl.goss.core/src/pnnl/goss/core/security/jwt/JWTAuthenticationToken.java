package pnnl.goss.core.security.jwt;

import java.util.List;

import org.apache.shiro.authc.AuthenticationToken;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JWTAuthenticationToken { //implements AuthenticationToken {

	private static final long serialVersionUID = -6203085918969990507L;
	private String sub;
    private long nbf;
    private String iss;
    private long exp;
    private long iat;
    private String jti;
    private List<String> roles;
    
    

    public JWTAuthenticationToken() {
        
    }



	public String getSub() {
		return sub;
	}



	public void setSub(String sub) {
		this.sub = sub;
	}



	public long getNbf() {
		return nbf;
	}



	public void setNbf(long nbf) {
		this.nbf = nbf;
	}



	public String getIss() {
		return iss;
	}



	public void setIss(String iss) {
		this.iss = iss;
	}



	public long getExp() {
		return exp;
	}



	public void setExp(long exp) {
		this.exp = exp;
	}



	public long getIat() {
		return iat;
	}



	public void setIat(long iat) {
		this.iat = iat;
	}



	public String getJti() {
		return jti;
	}



	public void setJti(String jti) {
		this.jti = jti;
	}



	public List<String> getRoles() {
		return roles;
	}



	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

    
	
	@Override
	public String toString() {
		Gson  gson = new Gson();
		return gson.toJson(this);
	}
	
	public static JWTAuthenticationToken parse(String jsonString){
		Gson  gson = new Gson();
		JWTAuthenticationToken obj = gson.fromJson(jsonString, JWTAuthenticationToken.class);
		if(obj.sub==null)
			throw new JsonSyntaxException("Expected attribute sub not found");
		return obj;
	}

}