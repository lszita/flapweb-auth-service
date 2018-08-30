package tech.flapweb.auth.utils;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

public class TokenGenerator {
    
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase(Locale.ROOT);
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUM = UPPER + LOWER + DIGITS;
    
    private final char[] symbols;
    private final char[] buffer;
    
    public TokenGenerator(int length){
        this.symbols = ALPHANUM.toCharArray();
        this.buffer = new char[length];
    }
    
    public String getNextToken(){
        Random r = new SecureRandom();
        for(int i =0; i < buffer.length; i++){
            buffer[i] = symbols[r.nextInt(symbols.length)];
        }
        
        return new String(buffer);
    }
}
