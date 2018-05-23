package tech.flapweb.auth.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.dao.UserDAO;
import tech.flapweb.auth.dao.UserDAO.AuthDBException;
import tech.flapweb.auth.domain.RegisterUser;

/**
 *
 * @author lszita
 */
@WebServlet(name = "Register", urlPatterns = {"/register"})
public class Register extends HttpServlet {

    
    private final Logger logger = LoggerFactory.getLogger(Login.class);
    private Validator validator;
    private UserDAO userDAO;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userDAO = new UserDAO();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();
        
        RegisterUser user = new RegisterUser();
        user.setUsername(request.getParameter("username"));
        user.setPassword(request.getParameter("password"));
        user.setEmailAddress(request.getParameter("email_address"));
        
        Set<ConstraintViolation<RegisterUser>> violations
                = validator.validate(user);
        
        // INVALID REQUEST
        if (violations.size() > 0) {
            JsonArrayBuilder errors = Json.createArrayBuilder();
            violations.forEach(v -> errors.add(v.getMessage()));
            responseObjectBuilder
                    .add("status", "error")
                    .add("errors", errors.build());
            response.setStatus(400);
        
        // VALID REQUEST
        } else {
            try {
                List<String> dbViolations = userDAO.validate(user);
                
                if(dbViolations.size() > 0){
                    JsonArrayBuilder errors = Json.createArrayBuilder();
                    dbViolations.forEach(v -> errors.add(v));
                    responseObjectBuilder
                            .add("status", "error")
                            .add("errors", errors.build());
                    response.setStatus(400);
                } else {
                    userDAO.createUser(user);
                    responseObjectBuilder.add("status", "success");
                    response.setStatus(201);
                }
            } catch(AuthDBException ex){
                logger.error("Exception",ex);
                responseObjectBuilder.add("status", "error");
                response.setStatus(500);
            }
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
