package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import jnekoimagesdb.JNekoImageDB;

@Deprecated
public class DBEngine {   
    public static           String      QUOTE           = "";
    private                 Statement   gStatement      = null;
    private                 Connection  
            gConnection     = null,
            gConnectionOld  = null;
    private volatile        int         
            queryCounter    = 0,
            queryPerConnect = 1000;//12000;

    public DBEngine() { }
    
    private void _mysqlConnect() throws SQLException {
        gConnection = DriverManager.getConnection("jdbc:mysql://localhost:43001/jneko", "jneko", "jneko");
        gConnection.setAutoCommit(true);
        gStatement = gConnection.createStatement();
        gStatement.setQueryTimeout(25);
    }
    
    private void _h2Connect() throws SQLException {
        try {
            QUOTE = "`";
            Class.forName("org.h2.Driver").newInstance();
            gConnection = DriverManager.getConnection("jdbc:h2:"+SplittedFile.DATABASE_FOLDER+"datastore;CIPHER=AES;MODE=MySQL;", "jneko", DBWrapper.getDBKey()+" "+DBWrapper.getDBKey());
            gConnection.setAutoCommit(false);
            gStatement = gConnection.createStatement();
            gStatement.setQueryTimeout(33);
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            throw new SQLException("Database error: org.h2.Driver");
        }
    }
    
    public int Connect(String filename) { 
        try { 
            //_mysqlConnect();
            _h2Connect();
            
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"file_MD5"+QUOTE+"(xmd5 BINARY(16) not null primary key, iid bigint not null);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"img_MD5"+QUOTE+"(xmd5 BINARY(16) not null primary key, iid bigint not null);");
            
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"AlbumsGroup"+QUOTE+"(oid bigint not null primary key, groupName blob, paid bigint, state int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"previews_list"+QUOTE+" (oid bigint not null primary key, idid bigint, pdid bigint, imgtype int);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"images_albums"+QUOTE+" (imgoid bigint, alboid bigint, UNIQUE KEY (imgoid, alboid));");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"images_basic_meta"+QUOTE+" (oid bigint not null primary key, imgoid bigint, width int, height int, wh1 double, wh2 bigint, fn_md5 BINARY(16));");
            //gStatement.executeUpdate("CREATE INDEX if not exists "+QUOTE+"imgMetaIndexMD5"+QUOTE+" ON "+QUOTE+"images_basic_meta"+QUOTE+" (fn_md5);");
            gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"StringSettings"+QUOTE+" (xname char(64), xvalue char(250), UNIQUE(xname));");
            //gStatement.executeUpdate("CREATE TABLE if not exists "+QUOTE+"previews_files"+QUOTE+"(oid bigint not null primary key, idid bigint, md5 BINARY(16));");
            
            createHistogramTable("R");
            createHistogramTable("G");
            createHistogramTable("B");
            
            //gStatement.close();
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

    public synchronized Connection getConnection(boolean isUpdate) {
        if (!isUpdate) {
            return gConnection;
        }
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
//                    gConnectionOld.setReadOnly(true);
                    gConnectionOld.commit();
                    gConnectionOld.close();
                    _L(Lang.ERR_DBEngine_OLD_SQL_CONNECTION_CLOSED);
                }
            } catch (SQLException ex) {
                _L(ex.getMessage());
                return null;
            }
        }
            
        if (queryCounter >= queryPerConnect) {
            try {
//                gConnection.setReadOnly(true);
//                gConnection.commit();
                gConnection.clearWarnings();
//                gConnection.setReadOnly(false);
                gConnectionOld = gConnection;
            } catch (SQLException ex) {
                _L(ex.getMessage());
                return null;
            }
            
            Connect("");
            queryCounter = 0;
            _L(Lang.ERR_DBEngine_SQL_CONNECTION_CLOSED);
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
