package tech.flapweb.auth.webservice;

import com.auth0.jwt.JWT;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import tech.flapweb.auth.App;
import tech.flapweb.auth.AppSettingsException;
import tech.flapweb.auth.dao.UserDAO;
import tech.flapweb.auth.dao.UserDAO.AuthDBException;
import tech.flapweb.auth.domain.LoginUser;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

@WebServlet(name = "Login", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    private static final Logger LOGGER = Log.getLogger(Login.class);
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

        LoginUser user = new LoginUser();
        user.setUsername(request.getParameter("username"));
        user.setPassword(request.getParameter("password"));

        Set<ConstraintViolation<LoginUser>> violations
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
                String token= null;
                
                if(userDAO.exists(user)) {
                    Algorithm algorithmRS = Algorithm.RSA256(null, App.getPK());
                    token = JWT.create()
                        .withIssuer("flapweb_auth")
                        .withSubject(user.getUsername())
                        .sign(algorithmRS);
                    responseObjectBuilder
                        .add("status", "success")
                        .add("token", token);
                    response.setStatus(200);
                } else {
                    responseObjectBuilder.add("status", "failed");
                    response.setStatus(400);
                }
            } catch (AuthDBException | AppSettingsException | JWTCreationException ex) {
                LOGGER.warn(ex);
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
        return "Login Servlet providing JWT token";
    }

    
}
