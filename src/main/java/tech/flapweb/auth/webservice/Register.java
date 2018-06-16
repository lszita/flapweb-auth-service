package tech.flapweb.auth.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.App;
import tech.flapweb.auth.AppSettingsException;
import tech.flapweb.auth.captcha.Captcha;
import tech.flapweb.auth.captcha.CaptchaException;
import tech.flapweb.auth.dao.UserDAO;
import tech.flapweb.auth.dao.UserDAO.AuthDBException;
import tech.flapweb.auth.domain.RegisterUser;

@WebServlet(name = "Register", urlPatterns = {"/register"})
public class Register extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(Login.class);
    private UserDAO userDAO;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        userDAO = new UserDAO();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder errors = Json.createArrayBuilder();
        
        RegisterUser user = new RegisterUser();
        user.setFromHttpRequest(request);
     
        try {
            // VALIDATE TOKEN
            if(!Captcha.isValid(App.getCaptchaSecret(), user.getCaptchaToken())){
                errors.add("Invalid captcha token");
                responseObjectBuilder.add("status", "error");
                responseObjectBuilder.add("errors", errors.build());
                response.setStatus(400);
            } else {
                // VALIDATE IF EXISTS
                List<String> dbViolations = userDAO.validate(user);
                if(dbViolations.size() > 0){
                    dbViolations.forEach(v -> errors.add(v));
                    responseObjectBuilder.add("status", "error");
                    responseObjectBuilder.add("errors", errors.build());
                    response.setStatus(400);
                } else {
                    userDAO.createUser(user);
                    responseObjectBuilder.add("status", "success");
                    response.setStatus(201);
                }
            }
        } catch(AuthDBException | AppSettingsException | CaptchaException ex){
            logger.error("Exception",ex);
            responseObjectBuilder.add("status", "error");
            response.setStatus(500);
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.println(responseObjectBuilder.build().toString());
        }
    }

    @Override
    public String getServletInfo() {
        return "Register servlet for creating new users";
    }
}
