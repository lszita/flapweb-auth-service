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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


public class LogoutRequest implements HttpDomain{
    
    @NotNull(message = "Username cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Username must contain only alphanumeric symbols")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    private String username;
    @NotNull(message = "refresh_token cannot be null")
    private String refreshToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public void setFromHttpRequest(HttpServletRequest request) {
        this.setUsername(request.getParameter("username"));
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
        Set<ConstraintViolation<LogoutRequest>> violations = validator.validate(this);
        Set<String> errorMessages = new HashSet<>();
        violations.forEach((e) -> errorMessages.add(e.getMessage()));
        return errorMessages;
    }
    
    private String getRefreshTokenCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(("refresh_token").equalsIgnoreCase(cookie.getName()))
                   return cookie.getValue();
            }
        }
        return null;
    }
}
