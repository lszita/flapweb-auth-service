/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.flapweb.auth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import tech.flapweb.auth.domain.LoginUser;

/**
 *
 * @author Lajos Szita
 */
public class UserDAO {
    
    private static final Logger LOGGER = Log.getLogger(UserDAO.class);
    
    public Boolean exists(LoginUser user) throws LoginDBException {
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Boolean exists = false;
        
        try{
            Context initContext = new InitialContext();
            Context webContext = (Context) initContext.lookup("java:/comp/env");

            DataSource ds = (DataSource) webContext.lookup("jdbc/flapweb");
            con = ds.getConnection();

            stmt = con.prepareStatement("SELECT username, email_address FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());

            rs = stmt.executeQuery();
            exists = rs.isBeforeFirst();
            if(exists){
                rs.next();
                LOGGER.info(String.format("Retrieved user %s , email %s", rs.getString(1), rs.getString(2)));
            }
            
        } catch(NamingException | SQLException ex) {
            LOGGER.warn(ex);
            throw new LoginDBException("DB error while retrieving user");
        } finally {
            try { if(rs != null) rs.close(); } catch (SQLException e) { LOGGER.warn(e); }
            try { if(stmt != null) stmt.close(); } catch (SQLException e) { LOGGER.warn(e); }
            try { if(con != null) con.close(); } catch (SQLException e) { LOGGER.warn(e); }
        }
        return exists;
    }
    
    public class LoginDBException extends Exception {
        public LoginDBException(){};
        public LoginDBException(String msg){ super(msg); };
    }
}
