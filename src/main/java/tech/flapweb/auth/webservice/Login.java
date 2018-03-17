package tech.flapweb.auth.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import tech.flapweb.auth.domain.LoginUser;

@WebServlet(name = "Login", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    private static final Logger LOGGER = Log.getLogger(Login.class);
    private Validator validator;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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

        if (violations.size() > 0) {
            JsonArrayBuilder errors = Json.createArrayBuilder();
            violations.forEach(v -> errors.add(v.getMessage()));
            responseObjectBuilder
                    .add("status", "error")
                    .add("errors", errors.build());
            response.setStatus(400);
        } else {
            try {
                Boolean succ = findUser(user);
                LOGGER.info(succ.toString());
                
                responseObjectBuilder
                    .add("status", "success")
                    .add("token", "heres a token you bitch ass ho");
                
            } catch (LoginDBException ex) {
                LOGGER.warn(ex);
                responseObjectBuilder
                    .add("status", "error");
            } 
            
        }

        try (PrintWriter out = response.getWriter()) {
            out.println(responseObjectBuilder.build().toString());
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private Boolean findUser(LoginUser user) throws LoginDBException {
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Boolean res = false;
        
        try{
            Context initContext = new InitialContext();
            Context webContext = (Context) initContext.lookup("java:/comp/env");

            DataSource ds = (DataSource) webContext.lookup("jdbc/flapweb");
            con = ds.getConnection();

            stmt = con.prepareStatement("SELECT username, email_address FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());

            rs = stmt.executeQuery();
            res = rs.isBeforeFirst();
            if(res){
                rs.next();
                LOGGER.info(String.format("Retrieved user %s , email %s", rs.getString(1), rs.getString(2)));
            }
            
        } catch(NamingException | SQLException ex) {
            LOGGER.warn(ex);
            throw new LoginDBException("DB error while retrieving user");
        } finally {
            try { if(rs != null) rs.close(); } catch (SQLException e) { LOGGER.warn(e); }
            try { if(stmt != null) stmt.close(); } catch (SQLException e) { LOGGER.warn(e); }
            try { if(con != null) con.close(); } catch (SQLException e) { LOGGER.warn(e); }
            
        }
        
        
        
        return res;
    }
    
    private class LoginDBException extends Exception {
        public LoginDBException(){};
        public LoginDBException(String msg){ super(msg); };
    }

}
