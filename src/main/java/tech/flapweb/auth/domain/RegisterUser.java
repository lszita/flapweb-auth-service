package tech.flapweb.auth.domain;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RegisterUser extends LoginUser{
    
    @NotNull(message="Email address cannot be null")
    @Email(message = "Email address must be a valid email address")
    @Size(max = 50, message = "Email address must be 3-30 characters")
    private String emailAddress;
    
    @NotNull(message="Missing captcha token")
    private String captchaToken;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
    
}
