package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import jnekoimagesdb.JNekoImageDB;

public class DBEngine {   
    public static final     String      QUOTE           = "";
    private                 Statement   gStatement      = null;
    private                 Connection  
            gConnection     = null,
            gConnectionOld  = null;
    private volatile        int         
            queryCounter    = 0,
            queryPerConnect = 12000;

    public DBEngine() { }
    
    public int Connect(String filename) {
        try {
            gConnection = DriverManager.getConnection("jdbc:mysql://localhost:43001/jneko", "jneko", "jneko");
            gConnection.setAutoCommit(true);
            gStatement = gConnection.createStatement();
            gStatement.setQueryTimeout(25);
            
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"AlbumsGroup"+QUOTE+"(oid bigint not null primary key, groupName blob, state int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"previews_list"+QUOTE+" (oid bigint not null primary key, idid bigint, pdid bigint, imgtype int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"images_albums"+QUOTE+" (oid bigint not null primary key, imgoid bigint, alboid bigint);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"images_basic_meta"+QUOTE+" (oid bigint not null primary key, imgoid bigint, width int, height int, wh1 double, wh2 bigint, fn_md5 BINARY(16));");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"StringSettings"+QUOTE+" (xname char(64), xvalue char(250), UNIQUE(xname));");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"previews_files"+QUOTE+"(oid bigint not null primary key, idid bigint, md5 BINARY(16));");
            
            createHistogramTable("R");
            createHistogramTable("G");
            createHistogramTable("B");
            
        } catch (SQLException ex) {
            _L(ex.getMessage());
            return -1;
        }
        return 0;
    }
    
    private void createHistogramTable(String table) throws SQLException {
        /*
            На первый взгляд это выглядит жутким костылем. Но нет. Этих гистограмм может быть больше миллиона и поиск по ним реален лишь силами MySQL.
            Выгружать пару-тройку миллионов блобов для поиска по ним по 255 байт каждый нереально.
        */
        final StringBuilder q = new StringBuilder();
        q.append("CREATE TABLE if not exists ").append(QUOTE).append("Histogram").append(table).append(QUOTE);
        q.append("(oid bigint not null primary key, iid bigint");
        for (int i=0; i<256; i++) q.append(", b").append(i).append(" SMALLINT");
        q.append(");");
        gStatement.executeUpdate(q.substring(0));
    }

    public synchronized Connection getConnection() {
        /*
            Весь этот странный код тут для того, чтобы устранить пока не отловленную утечку памяти при работе с sql.
            Почему-то, если использовать одно постоянное подключение, происходит небольшая утечка памяти, где-то 30-50мб на 2-3 тысячи insert/update запросов, 
              что в данном конкретном приложении полностью ломало всю идею.
            gConnectionOld нужно для того, чтобы некоторое время жило старое подключение для корректного завершения его использования всеми потоками.
        */
        queryCounter++;
        if (queryCounter == (queryPerConnect / 2)) {
            try {
                if (gConnectionOld != null) {
                    gConnectionOld.close();
                    _L("OLD SQL CONNECTION IS CLOSED;");
                }
            } catch (SQLException ex) {
                _L(ex.getMessage());
                return null;
            }
        }
            
        if (queryCounter >= queryPerConnect) {
            try {
                gConnection.clearWarnings();
                gConnectionOld = gConnection;
            } catch (SQLException ex) {
                _L(ex.getMessage());
                return null;
            }
            
            Connect("");
            queryCounter = 0;
            _L("SQL RECONNECT;");
        }
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
