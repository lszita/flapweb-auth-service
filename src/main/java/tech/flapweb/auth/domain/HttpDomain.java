package tech.flapweb.auth.domain;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;

public interface HttpDomain<T> {
    
    public void setFromHttpRequest(HttpServletRequest request);
    
    public Set<String> validate();
    
}
