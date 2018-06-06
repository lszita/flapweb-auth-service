package tech.flapweb.auth;

public class AppSettingsException extends Exception {
    public AppSettingsException(){};
    public AppSettingsException(String msg){ super(msg); };
}