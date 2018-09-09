package tech.flapweb.auth.domain;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

public class RenewalRequest implements HttpDomain{
    
    @NotNull(message = "provide valid access_token")
    private String accessToken;
    @NotNull(message = "provide valid refresh_token")
    private String refreshToken;

    public RenewalRequest() {
    }

    public RenewalRequest(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public void setFromHttpRequest(HttpServletRequest request) {
        this.setAccessToken(request.getParameter("access_token"));
        if(("COOKIE").equalsIgnoreCase(request.getParameter("refresh_token"))){
            this.setRefreshToken(getRefreshTokenCookie(request));
        } else {
            this.setRefreshToken(request.getParameter("refresh_token"));
        }
    }

    @Override
    public Set validate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<RenewalRequest>> violations = validator.validate(this);
        Set<String> errorMessages = new HashSet<>();
        violations.forEach((e) -> errorMessages.add(e.getMessage()));
        return errorMessages;
    }
    
    private String getRefreshTokenCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(("access_token").equalsIgnoreCase(cookie.getName()))
                   return cookie.getValue();
            }
        }
        return null;
    }
}
