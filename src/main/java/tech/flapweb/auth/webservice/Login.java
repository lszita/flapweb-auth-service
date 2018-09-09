package tech.flapweb.auth.webservice;

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
import com.auth0.jwt.exceptions.JWTCreationException;
import javax.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.captcha.Captcha;
import tech.flapweb.auth.captcha.CaptchaException;
import tech.flapweb.auth.utils.JWTGenerator;
import tech.flapweb.auth.utils.TokenGenerator;

@WebServlet(name = "Login", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(Login.class);
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();

        LoginUser user = new LoginUser();
        user.setFromHttpRequest(request);
        
        try {
            if(!Captcha.isValid(App.getCaptchaSecret(), user.getCaptchaToken())){
                responseObjectBuilder.add("status", "error");
                responseObjectBuilder.add("errors", "Invalid captcha token");
                response.setStatus(400);
            } else if(userDAO.exists(user, true)) {
                responseObjectBuilder
                    .add("status", "success")
                    .add("access_token", accessToken(user));
                if(user.isCookieBased()){
                    Cookie cookie = new Cookie("refresh_token",refreshToken(user));
                    cookie.setHttpOnly(true);
                    //cookie.setMaxAge(-1);
                    cookie.setPath("/");
                    cookie.setSecure(true);
                    response.addCookie(cookie);
                    logger.info("cookie added");
                } else {
                    responseObjectBuilder.add("refresh_token", refreshToken(user));
                }
                response.setStatus(200);
            } else {
                responseObjectBuilder.add("status", "failed");
                response.setStatus(400);
            }
        } catch (AuthDBException | AppSettingsException | JWTCreationException | CaptchaException ex) {
            logger.error("Exception",ex);
            responseObjectBuilder.add("status", "error");
            response.setStatus(500);
        }
        

        try (PrintWriter out = response.getWriter()) {
            out.println(responseObjectBuilder.build().toString());
        }
    }
    
    private String accessToken(LoginUser user) throws AppSettingsException{
        return new JWTGenerator(user.getUsername()).getNextJWT();
    }
    
    private String refreshToken(LoginUser user){
        if(App.getActiveUserStore().containsKey(user.getUsername())){
            return App.getActiveUserStore().get(user.getUsername());
        }
        
        String token = new TokenGenerator(128).getNextToken();
        App.getActiveUserStore().put(user.getUsername(), token);
        return token;
    }

    @Override
    public String getServletInfo() {
        return "Login Servlet providing JWT token";
    }
}