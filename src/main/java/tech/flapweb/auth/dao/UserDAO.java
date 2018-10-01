package tech.flapweb.auth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.flapweb.auth.domain.LoginUser;
import tech.flapweb.auth.domain.RegisterUser;
import tech.flapweb.auth.utils.DB;
import tech.flapweb.auth.utils.DBException;

public class UserDAO {
    
    private final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    
    public Boolean exists(LoginUser user, boolean activeOnly) throws AuthDBException {
        
        Boolean exists;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try{
            con = DB.getConnection();
            String query = "SELECT username, email_address, password FROM users WHERE username = ?";
            if(activeOnly){
                query += " AND active = TRUE";
            }
            stmt = con.prepareStatement(query);
            stmt.setString(1, user.getUsername());

            rs = stmt.executeQuery();
            exists = rs.isBeforeFirst();
            if(exists){
                rs.next();
                if(BCrypt.checkpw(user.getPassword(), rs.getString(3))){
                    logger.info("Logged in user {} , email {}", 
                                    rs.getString(1), rs.getString(2));
                    return true;
                } 
                return false;
            }
            return exists;
        
        } catch(DBException | SQLException ex) {
            logger.error("Exception",ex);
            throw new AuthDBException("DB error while retrieving user");
        } finally {
            try { if(rs != null) rs.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
            try { if(stmt != null) stmt.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
            try { if(con != null) con.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
        }
        
    }
    
    
    public void createUser(RegisterUser user) throws AuthDBException{
        Connection con = null;
        PreparedStatement stmt = null;
        
        try{
           
            con = DB.getConnection();
            String query = "INSERT INTO users(username, password, email_address, active) VALUES(?,?,?,?)";
            stmt = con.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            stmt.setString(3, user.getEmailAddress());
            stmt.setString(4, user.isActive() ? "true" : "false");
            
            stmt.execute();
            
        } catch(DBException | SQLException ex){
            logger.error("Exception occured",ex);
            throw new AuthDBException("DB error while creating user");
        } finally {
            try { if(stmt != null) stmt.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
            try { if(con != null) con.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
        }
    }
    
    public List<String> validate(RegisterUser user) throws AuthDBException{
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        List<String> violations = new ArrayList<>(2);
        
        try{
            con = DB.getConnection();
            String query = "select 'Username already exists!' as error_msg from users where username = ? " +
                           "union " +
                           "select 'Email already exists!' as error_msg from users where email_address = ?";
            stmt = con.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmailAddress());
            
            rs = stmt.executeQuery();
            while(rs.next()){
                violations.add(rs.getString("error_msg"));
            }
            return violations;
            
        } catch(SQLException | DBException ex){
            logger.error("Exception",ex);
            throw new AuthDBException("DB error while validating user");
        } finally {
            try { if(rs != null) rs.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
            try { if(stmt != null) stmt.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
            try { if(con != null) con.close(); } catch (SQLException ex) { logger.error("Exception",ex); }
        }
    }
    
    public class AuthDBException extends Exception {
        public AuthDBException(){};
        public AuthDBException(String msg){ super(msg); };
    }
}
