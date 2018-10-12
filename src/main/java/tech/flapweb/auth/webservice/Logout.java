
package tech.flapweb.auth.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.App;
import tech.flapweb.auth.domain.LogoutRequest;


@WebServlet(name = "Logout", urlPatterns = {"/logout"})
public class Logout extends HttpServlet {
    
    private final Logger LOGGER = LoggerFactory.getLogger(Logout.class);
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LogoutRequest logOut = new LogoutRequest();
        logOut.setFromHttpRequest(request);
                
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();
        if(logOut.getRefreshToken().equals(App.getActiveUserStore().get(logOut.getUsername()))){
            App.getActiveUserStore().remove(logOut.getUsername());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            responseObjectBuilder.add("status", "success");
            Cookie cookie = new Cookie("refresh_token","");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setSecure(true);
            response.addCookie(cookie);
            LOGGER.info("{} logged out", logOut.getUsername());
        } else {
            response.setStatus(400);
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.println(responseObjectBuilder.build().toString());
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
