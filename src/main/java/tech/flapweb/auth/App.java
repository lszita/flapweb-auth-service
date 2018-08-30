package tech.flapweb.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class App implements ServletContextListener{
    
    private static final String PROPERTIES_RESOURCE = "app.properties";
    
    private static final Properties PROPERTIES = new Properties();
    private static final ConcurrentMap<String,String> ACTIVE_USER_STORE = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Auth service starting up");
        
        if (!getPropsFromWAR()){
            LOGGER.info("no {} found in WAR, loading properties from PATH", PROPERTIES_RESOURCE);
            getPropsFromPath();
        }
    }

    public static RSAPrivateKey getPK() throws AppSettingsException{
        try {
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(PROPERTIES.getProperty("ssl.pk.location")));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return (RSAPrivateKey) kf.generatePrivate(privateSpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOGGER.error("Cannot initiate private key",ex);
            throw new AppSettingsException();
        }
    }
    
    public static RSAPublicKey getPUB() throws AppSettingsException{
        try {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(PROPERTIES.getProperty("ssl.pub.location")));
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(publicSpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOGGER.error("Cannot initiate public key",ex);
            throw new AppSettingsException();
        }
    }
    
    public static String getCaptchaSecret() throws AppSettingsException{
        String secret = PROPERTIES.getProperty("google.captcha.secret");
        if(secret == null){
            LOGGER.error("No captcha secret found in properties file");
            throw new AppSettingsException();
        }
        return secret;
    }
        
    private boolean getPropsFromWAR(){
        try (InputStream input = App.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)){
            if(input == null) return false;
            PROPERTIES.load(input);
        } catch (IOException ex) {
            LOGGER.error("Cannot read properties file",ex);
            return false;
        } 
        return true;
    }
    
    private boolean getPropsFromPath(){
        String path = System.getenv("jetty_base") + "/resources/" + PROPERTIES_RESOURCE;
        try (InputStream input = new FileInputStream(new File(path))){
            PROPERTIES.load(input);
        } catch (IOException ex) {
            LOGGER.error("Cannot read properties file",ex);
            return false;
        }
        return true;
    }
    
    public static ConcurrentMap<String,String> getActiveUserStore(){
        return ACTIVE_USER_STORE;
    }
        
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("SHUT DOWN");
    }
    
}