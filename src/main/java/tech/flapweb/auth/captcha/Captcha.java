package tech.flapweb.auth.captcha;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.flapweb.auth.AppSettingsException;

public class Captcha {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Captcha.class);
    private static final String API_URL = "https://www.google.com/recaptcha/api/siteverify";
    
    public static boolean isValid(String secret, String token) throws CaptchaException{
        
        if(token == null || ("").equals(token) || secret == null || ("").equals(secret)){
            return false;
        }
        boolean result = false;
        
        Map<String, String> parameters = new HashMap<>();
        parameters.put("secret", secret);
        parameters.put("response", token);
        
        
        try {
            URL url = new URL(API_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
            con.setRequestProperty( "charset", "utf-8");
            
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(getParamsString(parameters));
            out.flush();
            out.close();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            
            in.close();
            con.disconnect();
            
            LOGGER.info("Response content {}", content );
            JsonObject response = parseResponse(content.toString());
            result = response.getBoolean("success");   
        } catch (MalformedURLException ex) {
            LOGGER.error("Exception",ex);
            throw new CaptchaException(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error("Exception",ex);
            throw new CaptchaException(ex.getMessage());
        }
        return result;
    }
    
    private static String getParamsString(Map<String, String> params) 
      throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
 
        for (Map.Entry<String, String> entry : params.entrySet()) {
          result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
          result.append("=");
          result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
          result.append("&");
        }
 
        String resultString = result.toString();
        return resultString.length() > 0
          ? resultString.substring(0, resultString.length() - 1)
          : resultString;
    }
    
    private static JsonObject parseResponse(String jsonString){
        JsonObject object;
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            object = jsonReader.readObject();
        }
        return object;
    }
    
}
