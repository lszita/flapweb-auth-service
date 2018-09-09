package tech.flapweb.auth.domain;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RegisterUser extends LoginUser{
    
    @NotNull(message="Email address cannot be null")
    @Email(message = "Email address must be a valid email address")
    @Size(max = 50, message = "Email address must be 3-30 characters")
    private String emailAddress;
    
    public RegisterUser(){}
    
    @Override
    public void setFromHttpRequest(HttpServletRequest request){
        this.setUsername(request.getParameter("username"));
        this.setPassword(request.getParameter("password"));
        this.setEmailAddress(request.getParameter("email_address"));
        this.setCaptchaToken(request.getParameter("captcha_token"));
    }
    
    @Override
    public Set<String> validate(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<RegisterUser>> violations = validator.validate(this);
        Set<String> errorMessages = new HashSet<>();
        violations.forEach((e) -> errorMessages.add(e.getMessage()));
        return errorMessages;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}