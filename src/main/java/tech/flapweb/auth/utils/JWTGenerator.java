package tech.flapweb.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;
import tech.flapweb.auth.App;
import tech.flapweb.auth.AppSettingsException;

public class JWTGenerator {
    
    private static final String ISSUER = "flapweb_auth";
    private static final int EXPIRES_MINUTES = 2;
    
    private final String subject;
    
    public JWTGenerator(String subject){
        this.subject = subject;
    };
    
    public String getNextJWT() throws AppSettingsException{
        Algorithm algorithmRS = Algorithm.RSA256(null, App.getPK());
        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(subject)
            .withExpiresAt(new Date(new Date().getTime() + EXPIRES_MINUTES * 60000 ))
            .sign(algorithmRS);
    }
    
}
