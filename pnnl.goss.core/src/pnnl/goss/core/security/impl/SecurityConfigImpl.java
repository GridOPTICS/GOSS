package pnnl.goss.core.security.impl;

import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Set;
import java.util.UUID;

import org.apache.felix.dm.annotation.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.northconcepts.exception.SystemException;

import pnnl.goss.core.security.JWTAuthenticationToken;
import pnnl.goss.core.security.SecurityConfig;
import pnnl.goss.core.security.SecurityConstants;


@Component
public class SecurityConfigImpl implements SecurityConfig {

	private String managerUser;
	private String managerPassword;
	private boolean useToken = false;
	private byte[] sharedKey = generateSharedKey();

	private Dictionary<String, Object> properties;
	private static final Logger log = LoggerFactory.getLogger(SecurityConfigImpl.class);
	private static final String ISSUED_BY = "GridOPTICS Software System";

	
	
	
	public SecurityConfigImpl(){
	}
	
	
	protected long getExpirationDate() {
        return 1000 * 60 * 60 * 24 * 5;
    }

    protected String getIssuer(){return ISSUED_BY;}

	
	void updated(Dictionary<String, Object> properties) {
        if (properties != null) {
        	this.properties = properties;
        	
        	// create system realm
        	managerUser = getProperty(SecurityConstants.PROP_SYSTEM_MANAGER
					,null);	
			managerPassword = getProperty(SecurityConstants.PROP_SYSTEM_MANAGER_PASSWORD
					,null);	
			
			String secret = getProperty(SecurityConstants.PROP_SYSTEM_TOKEN_SECRET, null);
			if(secret!=null && secret.trim().length()>0){
				this.sharedKey = secret.getBytes();
			}
			
			String useTokenString = getProperty(SecurityConstants.PROP_SYSTEM_USE_TOKEN
					,null);	
			if(secret!=null && secret.trim().length()>0){
				try{
					this.useToken = new Boolean(useTokenString);
				}catch (Exception e) {
					log.error("Could not parse use token parameter as boolean in security config: '"+useTokenString+"'");
				}
			}
			
			System.out.println("SYSTEM CONFIG UPDATED "+managerUser+" "+managerPassword+" "+this);
        	
        } else {
        	log.error("No core config properties received by security activator");
        	throw new SystemException("No security config properties received by activator", null);
        }
    }
	
	

	public String getProperty(String key, String defaultValue){
		String retValue = defaultValue;
		
		if (key != null && !key.isEmpty() && properties.get(key)!=null){
			String value = properties.get(key).toString();
			// Let the value pass through because it doesn't
			// start with ${
			if (!value.startsWith("${")){
				retValue = value;
			}
		}
	    	
		return retValue;
	}
	
	
	@Override
	public String getManagerUser() {
		return managerUser;
	}

	@Override
	public String getManagerPassword() {
		return managerPassword;
	}



	@Override
	public boolean getUseToken() {
		// TODO Auto-generated method stub
		return false;
	}


	
    private byte[] generateSharedKey() {
        SecureRandom random = new SecureRandom();
        byte[] sharedKey = new byte[32];
        random.nextBytes(sharedKey);
        return sharedKey;
    }
    
    private byte[] getSharedKey(){
    	if (sharedKey==null )
    		sharedKey = generateSharedKey();
		return sharedKey;
	}
    
    public boolean validateToken(String token) {
    	log.debug("Validate token "+token);
        try {
            SignedJWT signed = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifierExtended(getSharedKey(), signed.getJWTClaimsSet());
            boolean verified = signed.verify(verifier);
            log.debug("Verified: "+verified);
            return verified;
        } catch (ParseException ex) {
            return false;
        } catch (JOSEException ex) {
            return false;
        }

    }
    
    public String createToken(Object userId,  Set<String> roles) {
    	log.info("Creating token for user "+userId);
        try {
        	//TODO, should also include roles(permissions)
        	
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
            builder.issuer(getIssuer());
            builder.subject(userId.toString());
            builder.issueTime(new Date());
            builder.notBeforeTime(new Date());
            builder.expirationTime(new Date(new Date().getTime() + getExpirationDate()));
            builder.jwtID(UUID.randomUUID().toString());
            
            JWTAuthenticationToken tokenObj = new JWTAuthenticationToken();
            tokenObj.setIss(getIssuer());
            tokenObj.setSub(userId.toString());
            tokenObj.setIat(new Date().getTime());
            tokenObj.setNbf(new Date().getTime());
            tokenObj.setExp(new Date(new Date().getTime() + getExpirationDate()).getTime());
            tokenObj.setJti(UUID.randomUUID().toString());
            tokenObj.setRoles(new ArrayList<String>(roles));
            Payload payload = new Payload(tokenObj.toString());

//            JWTClaimsSet claimsSet = builder.build();
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            JWSObject jwsObject = new JWSObject(header, payload);

            JWSSigner signer = new MACSigner(getSharedKey());
            jwsObject.sign(signer);
            return jwsObject.serialize();
        } catch (JOSEException ex) {
            return null;
        }
    }
    
    public JWTAuthenticationToken parseToken(String token){
    	try{
	    	SignedJWT signed = SignedJWT.parse(token);
			Payload payload = signed.getPayload();
			String jsonToken = payload.toJSONObject().toJSONString();
			log.info("Json token: "+jsonToken);
			// look up permissions based on roles and add them
			JWTAuthenticationToken tokenObj = JWTAuthenticationToken.parse(jsonToken);
			return tokenObj;
    	}catch (ParseException e) {
			// TODO: handle exception
    		return null;
		}
    }
}
