package tech.flapweb.auth.webservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.App;
import tech.flapweb.auth.AppSettingsException;
import tech.flapweb.auth.domain.LoginUser;
import tech.flapweb.auth.domain.RenewalRequest;

@WebServlet(name = "Renew", urlPatterns = {"/renew"}) 
public class Renew extends HttpServlet {
    
    private final Logger LOGGER = LoggerFactory.getLogger(Renew.class);
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    
        RenewalRequest ren = new RenewalRequest();
        ren.setFromHttpRequest(request);
        
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();
        try {
            DecodedJWT jwt = JWT.decode(ren.getAccessToken());
            if( ren.getRefreshToken().equals( App.getActiveUserStore().get(jwt.getSubject()))) {
                
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(200);
                responseObjectBuilder
                    .add("status", "success")
                    .add("access_token", accessToken(jwt.getSubject()));
                
            } else {
                LOGGER.info("user is not logged in, no token");
                response.setStatus(400);
            }
        } catch(JWTDecodeException ex) {
            response.setStatus(400);
            LOGGER.info("cannot decode token, invalid token");
        } catch(AppSettingsException ex) {
            response.setStatus(500);
            LOGGER.error("Cannot generate token",ex);
        }
        
        
        try (PrintWriter out = response.getWriter()) {
            out.println(responseObjectBuilder.build().toString());
        }
    }
    
    
    private String accessToken(String username) throws AppSettingsException{
        Algorithm algorithmRS = Algorithm.RSA256(null, App.getPK());
        return JWT.create()
                .withIssuer("flapweb_auth")
                .withSubject(username)
                .withExpiresAt(new Date(new Date().getTime() + 2 * 60000 ))
                .sign(algorithmRS);
    }
    
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
