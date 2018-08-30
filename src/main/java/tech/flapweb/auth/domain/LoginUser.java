package tech.flapweb.auth.domain;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class LoginUser implements HttpDomain{
    
    @NotNull(message = "Username cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Username must contain only alphanumeric symbols")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    private String username;
    @NotNull(message = "Password cannot be null")
    @Size(min = 3, max = 30, message = "Password must be 3-30 characters")
    private String password; 

    public LoginUser() {
    }
    
    @Override
    public void setFromHttpRequest(HttpServletRequest request){
        this.setUsername(request.getParameter("username"));
        this.setPassword(request.getParameter("password"));
    }
    
    @Override
    public Set<String> validate(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<LoginUser>> violations = validator.validate(this);
        Set<String> errorMessages = new HashSet<>();
        violations.forEach((e) -> errorMessages.add(e.getMessage()));
        return errorMessages;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}