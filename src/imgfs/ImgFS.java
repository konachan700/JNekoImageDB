package imgfs;

import dialogs.DialogMTPrevGenProgress;
import dialogs.DialogMessageBox;
import imgfstabs.TabAddImagesToDB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class ImgFS {
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
            };
    
    private static TabAddImagesToDB         addNewImagesTab;
    private static ImgFSDatastore           datastore;
    private static DialogMTPrevGenProgress  progressDialog = new DialogMTPrevGenProgress();
    private static DialogMessageBox         messageBox = new DialogMessageBox();
    private static String                   rootDatabaseName;

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

        datastore = new ImgFSDatastore(cryptoEx, databaseName);
        datastore.init();
        
        addNewImagesTab = new TabAddImagesToDB(cryptoEx, databaseName);
    }
    
    public static void dispose() {
        datastore.close();
        addNewImagesTab.dispose();

        final Set<String> s = levelDB.keySet();
        s.forEach((x) -> {
             try {
                levelDB.get(x).close();
            } catch (IOException ex) { }
        });
    }
    
    public static ImgFSCrypto getCrypt() {
        return cryptoEx;
    }
    
    public static TabAddImagesToDB getAddImagesTab() {
        return addNewImagesTab;
    }
    
    public static ImgFSDatastore getDatastore() {
        return datastore;
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
