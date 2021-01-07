package pnnl.goss.core.security.jwt;

public interface UserRepository {
	
    public UserDefault findByUserId(Object userId);

    public UserDefault findById(Object id);

//    public byte[] generateSharedKey();

    public long getExpirationDate() ;

    public String getIssuer();


//    public TokenResponse createToken(UserDefault user) ;

    public String createToken(Object userId) ;

    public boolean validateToken(String token);
}