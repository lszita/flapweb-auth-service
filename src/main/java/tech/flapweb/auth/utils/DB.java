package tech.flapweb.auth.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB {
    
    private static final String JNDI_NAME = "jdbc/flapweb";
    private static final String SCHEMA = "schema.sql";
    private static final Logger LOGGER = LoggerFactory.getLogger(DB.class);
    
    public static Connection getConnection() throws DBException{
        try{
            Context initContext = new InitialContext();
            Context webContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) webContext.lookup(JNDI_NAME);
            return ds.getConnection();
        } catch (NamingException | SQLException ex){
            LOGGER.error("Error in creating datasource",ex);
            throw new DBException();
        }
    }
    
    public static void createSchema() throws DBException{
        try{
            Connection connection = getConnection();
            RunScript.execute(connection, new FileReader(SCHEMA));
            connection.close();
        } catch (DBException | FileNotFoundException | SQLException ex ){
            LOGGER.error("Error in creating datasource",ex);
            throw new DBException();
        }
    }
    
}
