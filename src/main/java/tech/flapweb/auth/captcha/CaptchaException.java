package tech.flapweb.auth.captcha;

public class CaptchaException extends Exception{
    CaptchaException(){};
    CaptchaException(String msg){ super(msg); };
}
