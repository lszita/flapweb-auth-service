package tech.flapweb.auth.captcha;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import redis.clients.jedis.Jedis;
import tech.flapweb.auth.App;

public class CaptchaTest {
    
    public CaptchaTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        App app = new App();
        app.contextInitialized(null);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isValid method, of class Captcha.
     */
    @Test
    public void testIsValid() throws Exception {
        String token = "";
        boolean expResult = false;
        System.out.println("SECRET:" + App.getCaptchaSecret());
        boolean result = Captcha.isValid(App.getCaptchaSecret(),token);
        assertEquals(expResult, result);
    }
    
    
}
