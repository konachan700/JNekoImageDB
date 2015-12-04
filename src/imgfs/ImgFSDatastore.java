package imgfs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.h2.jdbcx.JdbcConnectionPool;

public class ImgFSDatastore {
    public static final int
            MINIMUM_IMAGE_SIZE = 1024,
            SINGLE_CONNECTION_TTL = 4096;
    
    private volatile long
            readConnCounter = 0,
            writeConnCounter = 0, 
            prepareStatementWCounter = 0;
    
    private final ImgFSCrypto
            dsCrypto;
    
    private final String
            databaseName, storeBasePath;
    
    private final File
            storeRootDirectory;
    
    private JdbcConnectionPool 
            pool;
    
    private Connection
            dbRConnection, dbWConnection;
    
    private Statement
            wStatement = null;
    
    private PreparedStatement
            addItemsPS = null;
    
    public ImgFSDatastore(ImgFSCrypto c, String dbname) {
        dsCrypto = c;
        databaseName = dbname;
        storeBasePath = "." + File.separator + databaseName + File.separator + "store";
        storeRootDirectory = new File(storeBasePath);
    }
    
    public void init() throws Exception {
        h2Connect();
        
        
    }
    
    public void commit() {
        try {
            dbExecAddItemPS();
            addItemsPS = dbGetAddItemPS();
            dbRConnection.clearWarnings();
            dbWConnection.clearWarnings();
            dbWConnection.commit();
        } catch (SQLException ex) { 
            
        } 
    }
    
    public void close() {
        try {
            addItemsPS.close();
            dbWConnection.clearWarnings();
            dbWConnection.commit();
            dbWConnection.close();
            dbRConnection.clearWarnings();
            dbRConnection.commit();
            dbRConnection.close();
        } catch (SQLException ex) { }
    }

    public void removeFile(byte[] md5) throws IOException {
        final String p = getPathString(md5);
        final Path out = FileSystems.getDefault().getPath(p);
        Files.delete(out); 
    }
    
    public void saveFile(byte[] md5, Path savePath) throws IOException {
        final byte[] out = getFile(md5);
        Files.write(savePath, out);
    }
    
    public byte[] getFile(byte[] md5) throws IOException {
        final String p = getPathString(md5);
        final Path out = FileSystems.getDefault().getPath(p);
        
        final byte[] nc = Files.readAllBytes(out);
        if (nc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: file too small;");
        
        final byte[] cc = dsCrypto.Decrypt(nc);
        if (cc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: strange crypt error;");
        
        return cc;
    }

    public void pushFile(byte[] md5, Path file) throws Exception {
        final String p = getPathString(md5);
        
        final byte[] nc = Files.readAllBytes(file);
        if (nc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: file too small;");
        
        final byte[] cc = dsCrypto.Crypt(nc);
        if (cc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: strange crypt error;");
        
        final Path out = FileSystems.getDefault().getPath(p);
        Files.write(out, cc); 
       
        dbAddItem(md5);
    }
    
    private String getPathString(byte[] md5) throws IOException {
        if (md5.length != 16) throw new IOException("ImgFSDatastore: Input data is not correct;");
        
        if (!storeRootDirectory.mkdirs())
            if (!storeRootDirectory.exists()) throw new IOException("ImgFSDatastore: Cannot create root directory;");
        
        final String md5s = DatatypeConverter.printHexBinary(md5).toLowerCase();
        final String dir = storeBasePath + File.separator + md5s.substring(0, 2) + File.separator + md5s.substring(2, 4);
        final File tdir = new File(dir);
        if (!tdir.mkdirs())
            if (!tdir.exists()) throw new IOException("ImgFSDatastore: Cannot create end directory;");
                
        final String path = dir + File.separator + md5s.substring(4);
        return path;
    }
    
    
    
    
    
    
    @SuppressWarnings("ConvertToTryWithResources")
    private synchronized void h2Connect() throws SQLException {
        try {
            Class.forName("org.h2.Driver").newInstance();
            pool = JdbcConnectionPool.create(
                    "jdbc:h2:."+File.separator+databaseName+File.separator+"metadata;CIPHER=AES;MODE=MySQL;", "jneko", dsCrypto.getPassword()+" "+dsCrypto.getPassword());
            
            dbWConnection = pool.getConnection();
            dbWConnection.setAutoCommit(false);

            dbRConnection = pool.getConnection();
            dbRConnection.setAutoCommit(true);
            dbRConnection.setReadOnly(true);
            
            final Statement dbInitStatement = dbWConnection.createStatement();
            dbInitStatement.setQueryTimeout(33);
            dbInitStatement.executeUpdate("CREATE TABLE if not exists `images` (iid bigint not null primary key auto_increment, xmd5 BINARY(16) not null);");
            dbInitStatement.executeUpdate("CREATE TABLE if not exists `tags` (iid bigint not null primary key auto_increment, xtag char(175));");
            dbInitStatement.executeUpdate("CREATE TABLE if not exists `albums` (iid bigint not null primary key auto_increment, piid bigint not null, xname char(64), flags bigint);");
            dbInitStatement.close();
            dbWConnection.commit();
            
            addItemsPS = dbGetAddItemPS();
            
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            throw new SQLException("Database error: org.h2.Driver");
        }
    }
    
    private synchronized PreparedStatement dbGetAddItemPS() throws SQLException {
        final PreparedStatement ps = getWConn().prepareStatement("INSERT INTO `images` VALUES (default, ?);");
        prepareStatementWCounter++;
        return ps;
    }
    
    private synchronized void dbExecAddItemPS() throws SQLException {
        if (addItemsPS != null) {
            addItemsPS.executeBatch();
            addItemsPS.clearWarnings();
            addItemsPS.close();
            prepareStatementWCounter--;
        }
    }
    
    private synchronized void dbAddItem(byte[] xmd5) throws SQLException {
        if (addItemsPS != null) {
            addItemsPS.setBytes(1, xmd5);
            addItemsPS.addBatch();
        }
    }
    
    private synchronized Connection getWConn() {
        writeConnCounter++;
        return dbWConnection;
    }
    
    private synchronized Connection getRConn() {
        readConnCounter++;
        return dbRConnection;
    }
}
