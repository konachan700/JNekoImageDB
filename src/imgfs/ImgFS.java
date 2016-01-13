package imgfs;

import dialogs.DialogMTPrevGenProgress;
import dialogs.DialogMessageBox;
import imgfstabs.TabAddImagesToDB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import org.h2.jdbcx.JdbcConnectionPool;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class ImgFS {
    public static enum PreviewType {
        cache, previews
    }
        
    private static Map<String, DB>
            levelDB = new HashMap<>();
    
    private static final ImgFSCrypto
        cryptoEx = new ImgFSCrypto(() -> {
            
            return null;
        });
    
    private static final ImgFSPreviewGen.PreviewGeneratorProgressListener
            progressInd = new ImgFSPreviewGen.PreviewGeneratorProgressListener() {
                @Override
                public void OnStartThread(int itemsCount, int tID) {
                    progressDialog.itemProgresss(tID);
                }

                @Override
                public void OnNewItemGenerated(int itemsCount, Path p, int tID, String quene) {
                    progressDialog.itemSetInfo(tID, p, itemsCount, quene);
                }

                @Override
                public void OnError(int tID) {

                }

                @Override
                public void OnComplete(int tID) {
                    progressDialog.itemComplete(tID);
                }

                @Override
                public void OnCreated(int tID) {
                    progressDialog.itemCreate(tID);
                }   

                @Override
                public void OnInfoUpdate(int tID, String info) {
                    progressDialog.itemSetInfo(tID, info); 
                }
            };
    
    private static TabAddImagesToDB         addNewImagesTab;
    private static DialogMTPrevGenProgress  progressDialog = new DialogMTPrevGenProgress();
    private static final DialogMessageBox   messageBox = new DialogMessageBox();
    private static String                   rootDatabaseName;
    private static JdbcConnectionPool       h2pool;

    public static DB getDB(String name) {
        return levelDB.get(name);
    }
    
    public static void initIDB(String dbName) {
        final File levelDBFile = new File(rootDatabaseName + File.separator + dbName);
        Options options = new Options();
        options.createIfMissing(true);   
        try {
            levelDB.put(dbName, factory.open(levelDBFile, options));
        } catch (IOException ex) {
            Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void init(String databaseName) throws Exception {
        rootDatabaseName = databaseName;
        cryptoEx.init(databaseName);
        h2Connect();
        
        addNewImagesTab = new TabAddImagesToDB(cryptoEx, databaseName);
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    private static void h2Connect() throws SQLException {
        try {
            Class.forName("org.h2.Driver").newInstance();
            h2pool = JdbcConnectionPool.create(
                    "jdbc:h2:."+File.separator+rootDatabaseName+File.separator+"metadata;CIPHER=AES;MODE=MySQL;", "jneko", cryptoEx.getPassword()+" "+cryptoEx.getPassword());
            h2pool.setMaxConnections(64);
            h2pool.setLoginTimeout(60);
            
            final Connection dbWConnection = h2pool.getConnection();
            dbWConnection.setAutoCommit(false);
            
            final Statement dbInitStatement = dbWConnection.createStatement();
            dbInitStatement.executeUpdate("CREATE TABLE if not exists `images` (iid bigint not null primary key auto_increment, xmd5 BINARY(16) not null);");
            dbInitStatement.executeUpdate("CREATE TABLE if not exists `tags` (iid bigint not null primary key auto_increment, xtag char(255));");
            dbInitStatement.executeUpdate("CREATE TABLE if not exists `albums` (iid bigint not null primary key auto_increment, piid bigint not null, xname char(64), xtext mediumblob, flags bigint);");
            dbInitStatement.executeUpdate("CREATE TABLE if not exists `imggal` (img_iid bigint not null, gal_iid bigint not null primary key, UNIQUE(img_iid, gal_iid));");
            
            dbInitStatement.close();
            
            dbWConnection.commit();
            dbWConnection.close();
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            throw new SQLException("Database error: org.h2.Driver");
        }
    }
    
    public static void dispose() {
        addNewImagesTab.dispose();
        h2pool.dispose();

        final Set<String> s = levelDB.keySet();
        s.forEach((x) -> {
             try {
                levelDB.get(x).close();
            } catch (IOException ex) { }
        });
    }
    
    public static synchronized Connection getH2Connection() {
        try {
            return h2pool.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static ImgFSCrypto getCrypt() {
        return cryptoEx;
    }
    
    public static TabAddImagesToDB getAddImagesTab() {
        return addNewImagesTab;
    }
    
    public static ImgFSPreviewGen.PreviewGeneratorProgressListener getProgressListener() {
        return progressInd;
    }
    
    public static void progressShow() {
        progressDialog.show();
    }
    
    public static void msgbox(String text) {
        messageBox.showMsgbox(text);
    }
}
