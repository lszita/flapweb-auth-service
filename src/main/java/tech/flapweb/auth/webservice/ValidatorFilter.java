package tech.flapweb.auth.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tech.flapweb.auth.domain.HttpDomain;
import tech.flapweb.auth.domain.LoginUser;
import tech.flapweb.auth.domain.RegisterUser;
import tech.flapweb.auth.domain.RenewalRequest;

@WebFilter(servletNames={"Register","Login","Renew"})
public class ValidatorFilter implements Filter{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
               
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        if(!request.getMethod().equalsIgnoreCase("POST")){
            response.setStatus(405);
            return;
        }
        
        response.setContentType("application/json;charset=UTF-8");
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder errors = Json.createArrayBuilder();
        
        String uri = request.getRequestURI().toLowerCase();
        HttpDomain httpDomainClass;
        
        if(uri != null && uri.contains("register")){
            httpDomainClass = new RegisterUser();
        } else if(uri != null && uri.contains("login")){
            httpDomainClass = new LoginUser();
        } else if(uri != null && uri.contains("renew")){
            httpDomainClass = new RenewalRequest();
        } else return;
        
        httpDomainClass.setFromHttpRequest(request);
        Set<String> violations = httpDomainClass.validate();
        
        if (violations.size() > 0) {
            violations.forEach(v -> errors.add(v));
            responseObjectBuilder
                    .add("status", "error")
                    .add("errors", errors.build());
            response.setStatus(400);
            
            try (PrintWriter out = response.getWriter()) {
                out.println(responseObjectBuilder.build().toString());
            }
        } else {
            chain.doFilter(request, res);
        }
    }    

    @Override
    public void destroy() {}
}