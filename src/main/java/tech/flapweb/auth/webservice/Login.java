package tech.flapweb.auth.webservice;

import com.auth0.jwt.JWT;
import java.io.IOException;
import java.io.PrintWriter;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tech.flapweb.auth.App;
import tech.flapweb.auth.AppSettingsException;
import tech.flapweb.auth.dao.UserDAO;
import tech.flapweb.auth.dao.UserDAO.AuthDBException;
import tech.flapweb.auth.domain.LoginUser;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.utils.TokenGenerator;

@WebServlet(name = "Login", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(Login.class);
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();

        LoginUser user = new LoginUser();
        user.setFromHttpRequest(request);
        
        try {
            if(userDAO.exists(user)) {
                responseObjectBuilder
                    .add("status", "success")
                    .add("access_token", accessToken(user))
                    .add("refresh_token", refreshToken(user));
                response.setStatus(200);
            } else {
                responseObjectBuilder.add("status", "failed");
                response.setStatus(400);
            }
        } catch (AuthDBException | AppSettingsException | JWTCreationException ex) {
            logger.error("Exception",ex);
            responseObjectBuilder.add("status", "error");
            response.setStatus(500);
        }
        

        try (PrintWriter out = response.getWriter()) {
            out.println(responseObjectBuilder.build().toString());
        }
    }
    
    private String accessToken(LoginUser user) throws AppSettingsException{
        Algorithm algorithmRS = Algorithm.RSA256(null, App.getPK());
        return JWT.create()
                .withIssuer("flapweb_auth")
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(new Date().getTime() + 2 * 60000 ))
                .sign(algorithmRS);
    }
    
    private String refreshToken(LoginUser user){
        String token = new TokenGenerator(128).getNextToken();
        App.getActiveUserStore().put(user.getUsername(), token);
        return token;
    }

    @Override
    public String getServletInfo() {
        return "Login Servlet providing JWT token";
    }
}