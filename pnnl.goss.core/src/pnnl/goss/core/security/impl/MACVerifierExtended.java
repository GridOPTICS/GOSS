package pnnl.goss.core.security.impl;

import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;

public class MACVerifierExtended extends MACVerifier {

    private final JWTClaimsSet claimsSet;

    public MACVerifierExtended(final byte[] sharedSecret, JWTClaimsSet claimsSet) throws JOSEException {
        super(sharedSecret);
        this.claimsSet = claimsSet;
    }

    public MACVerifierExtended(final String sharedSecretString, JWTClaimsSet claimsSet) throws JOSEException {
        super(sharedSecretString);
        this.claimsSet = claimsSet;
    }

    @Override
    public boolean verify(final JWSHeader header, final byte[] signingInput, final Base64URL signature) throws JOSEException {
        boolean value = super.verify(header, signingInput, signature);
//        long time = System.currentTimeMillis();
        
        long time = new Date().getTime();
        time=time*1000;
        
        boolean notBeforeTime = claimsSet.getNotBeforeTime().getTime() <= time;
        boolean beforeExpiration = time < claimsSet.getExpirationTime().getTime();
        
        
        return value && notBeforeTime && beforeExpiration;
    }
}
