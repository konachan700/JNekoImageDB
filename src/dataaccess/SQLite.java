package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnekoimagesdb.JNekoImageDB;

public class SQLite {   
    public static final String QUOTE = "";
    
    private Statement 
            gStatement      = null;
    
    private Connection 
            gConnection     = null;
    
    private final Crypto
            xCrypto;

    public SQLite(Crypto c) {
        xCrypto = c;
    }
    
    public int Connect(String filename) {
        try {
//            Class.forName("org.sqlite.JDBC");
//            Class.forName("org.h2.Driver").newInstance();
//            gConnection = DriverManager.getConnection("jdbc:sqlite:" + filename); jdbc:h2:
//            gConnection = DriverManager.getConnection("jdbc:h2:" + filename+";CIPHER=AES;DB_CLOSE_DELAY=-1;", "jnekolab", xCrypto.getPasswordFromMasterKey() + " " + xCrypto.getPasswordFromMasterKey()); 
            gConnection = DriverManager.getConnection("jdbc:mysql://localhost:43001/jnekoimdb", "jneko", "jneko");
            gConnection.setAutoCommit(true);
            gStatement = gConnection.createStatement();
            gStatement.setQueryTimeout(25);
            
            gStatement.executeUpdate("CREATE TABLE if not exists "+SQLite.QUOTE+"AlbumsGroup"+SQLite.QUOTE+"(oid int not null primary key, groupName blob, state int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+SQLite.QUOTE+"previews_list"+SQLite.QUOTE+" (oid bigint not null primary key, idid bigint, pdid bigint, imgtype int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+SQLite.QUOTE+"images_albums"+SQLite.QUOTE+" (oid bigint not null primary key, imgoid bigint, alboid bigint);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+SQLite.QUOTE+"images_basic_meta"+SQLite.QUOTE+" (oid bigint not null primary key, imgoid bigint, width int, height int, wh1 double, wh2 bigint, fn_md5 BINARY(16));");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"StringSettings"+QUOTE+" (xname char(64), xvalue char(250), UNIQUE(xname));");
            gStatement.executeUpdate("CREATE TABLE if not exists "+SQLite.QUOTE+"previews_files"+SQLite.QUOTE+"(oid bigint not null primary key, idid bigint, md5 BINARY(16));");
            
        } catch (SQLException ex) {
            _L(ex.getMessage());
            return -1;
        }
//        } catch (SQLException | ClassNotFoundException ex) {
//            _L(ex.getMessage());
//            return -1;
//        } catch (InstantiationException | IllegalAccessException ex) {
//            Logger.getLogger(SQLite.class.getName()).log(Level.SEVERE, null, ex);
//        }
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

    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s);
    }
}
