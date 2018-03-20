package tech.flapweb.auth.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LoginUser {
    
    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    private String username;
    @NotNull(message = "Password cannot be null")
    @Size(min = 3, max = 30, message = "Password must be 3-30 characters")
    private String password; 

    public LoginUser() {
    }

    public LoginUser(String username, String password) {
        this.username = username;
        this.password = password;
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
