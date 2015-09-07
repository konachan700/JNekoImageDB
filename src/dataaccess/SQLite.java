package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import jnekoimagesdb.JNekoImageDB;

public class SQLite {   
    private Statement 
            gStatement      = null;
    
    private Connection 
            gConnection     = null;

    public int Connect(String filename) {
        try {
            Class.forName("org.sqlite.JDBC");
            gConnection = DriverManager.getConnection("jdbc:sqlite:" + filename);
            gStatement = gConnection.createStatement();
            gStatement.setQueryTimeout(25);
            gStatement.executeUpdate("CREATE TABLE if not exists 'StringSettings'(name char(64), value char(250), UNIQUE(name));");
        } catch (SQLException | ClassNotFoundException ex) {
            _L(ex.getMessage());
            return -1;
        }
        return 0;
    }
    
    public Statement getStatement() {
        return gStatement;
    }
    
    public Connection getConnection() {
        return gConnection;
    }

    public int ExecuteSQL(String sql) {
        if ((gStatement == null) || (gConnection == null)) return -2;
        try {
            gStatement.executeUpdate(sql);
        } catch (SQLException ex) {
            _L(ex.getMessage());
            return -1;
        }
        return 0;
    }
    
    public void WriteAPPSettingsString(String optName, String value) {
        try {
            PreparedStatement ps = gConnection.prepareStatement("INSERT INTO 'StringSettings' VALUES(?, ?);");
            ps.setString(1, optName);
            ps.setString(2, value); 
            ps.execute();
        } catch (SQLException ex) {
            try {
                PreparedStatement ps = gConnection.prepareStatement("UPDATE 'StringSettings' SET value=? WHERE name=?;");
                ps.setString(1, value);
                ps.setString(2, optName);
                ps.execute();
            } catch (SQLException ex1) {
                System.err.println("ERROR: "+ex1.getMessage());
            }
        }
    }
    
    public String ReadAPPSettingsString(String optName) {
        try {
            PreparedStatement ps = gConnection.prepareStatement("SELECT * FROM 'StringSettings' WHERE name=?;");
            ps.setString(1, optName); 
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                final String retval = rs.getString("value");
                return retval;
            }
        } catch (SQLException ex) {
            System.err.println("ERROR: "+ex.getMessage());
            return "";
        }
        return "";
    }
    
    private void _L(String s) {
        //System.out.println(s);
        JNekoImageDB.L(s);
    }
}
