package tech.flapweb.auth;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lajos Szita
 */
@WebListener
public class App implements ServletContextListener{

    private final Logger logger = LoggerFactory.getLogger(App.class);
    private static RSAPrivateKey PK = null;
    private static RSAPublicKey PUB = null;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Auth service starting up");
        
        Properties prop = new Properties();
    	InputStream input = null;
        String env = ("prod".equalsIgnoreCase(System.getProperty("ENV"))) ? "prod" : "dev";
        logger.info("Environment is: {}", env);
        try {
            
            input = App.class.getClassLoader().getResourceAsStream("app.properties");
            prop.load(input);
            
            // PRIVATE KEY SETUP
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(prop.getProperty(env + ".ssl.pk.location")));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PK = (RSAPrivateKey) kf.generatePrivate(privateSpec);
            
            //PUBLIC KEY SETUP
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(prop.getProperty(env + ".ssl.pub.location")));
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            PUB = (RSAPublicKey) kf.generatePublic(publicSpec);
            
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException ex) {
            logger.error("Cannot start application cannot set up keys",ex);
        } finally {
            try {
                if(input != null) input.close();
            } catch (IOException ex) {
                logger.warn("Cannot close IO stream",ex);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
    
    public static RSAPrivateKey getPK() throws AppSettingsException{
        if(PK == null){
            throw new AppSettingsException();
        } else {
            return PK;
        }
    }
    
    public static RSAPublicKey getPUB() throws AppSettingsException{
        if(PUB == null){
            throw new AppSettingsException();
        } else {
            return PUB;
        }
    }
    
}
