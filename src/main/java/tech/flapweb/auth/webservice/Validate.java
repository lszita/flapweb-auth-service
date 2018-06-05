package tech.flapweb.auth.webservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.App;
import tech.flapweb.auth.AppSettingsException;

@WebServlet(name = "Validate", urlPatterns = {"/validate"})
public class Validate extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(Login.class);
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String token = request.getParameter("token");
            
            try {
                Algorithm algorithmRS = Algorithm.RSA256(App.getPUB(), null);
                JWTVerifier verifier = JWT.require(algorithmRS)
                    .withIssuer("flapweb_auth")
                    .build();
                DecodedJWT jwt = verifier.verify(token);
                out.println(jwt.getSubject());
            }catch (JWTVerificationException ex) {
                response.setStatus(400);
                out.println("invalid");
                logger.error("An invalid token was sent for validation",ex);
            } catch (AppSettingsException ex) {
               logger.error("Exception",ex);
               response.setStatus(500);
            }
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
