package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import jnekoimagesdb.JNekoImageDB;

public class DBEngine {   
    public static final     String      QUOTE           = "";
    private                 Statement   gStatement      = null;
    private                 Connection  gConnection     = null;

    public DBEngine() { }
    
    public int Connect(String filename) {
        try {
            gConnection = DriverManager.getConnection("jdbc:mysql://localhost:43001/jneko", "jneko", "jneko");
            gConnection.setAutoCommit(true);
            gStatement = gConnection.createStatement();
            gStatement.setQueryTimeout(25);
            
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"AlbumsGroup"+QUOTE+"(oid int not null primary key, groupName blob, state int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"previews_list"+QUOTE+" (oid bigint not null primary key, idid bigint, pdid bigint, imgtype int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"images_albums"+QUOTE+" (oid bigint not null primary key, imgoid bigint, alboid bigint);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"images_basic_meta"+QUOTE+" (oid bigint not null primary key, imgoid bigint, width int, height int, wh1 double, wh2 bigint, fn_md5 BINARY(16));");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"StringSettings"+QUOTE+" (xname char(64), xvalue char(250), UNIQUE(xname));");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"previews_files"+QUOTE+"(oid bigint not null primary key, idid bigint, md5 BINARY(16));");
            
        } catch (SQLException ex) {
            _L(ex.getMessage());
            return -1;
        }
        return 0;
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
