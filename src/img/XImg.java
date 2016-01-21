package img;

import datasources.HibernateUtil;
import img.gui.dialogs.DialogMTPrevGenProgress;
import img.gui.dialogs.DialogMessageBox;
import img.gui.tabs.TabAddImagesToDB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class XImg {
    public static enum PreviewType {
        cache, previews
    }
        
    private static Map<String, DB>
            levelDB = new HashMap<>();
    
    private static final XImgCrypto
        cryptoEx = new XImgCrypto(() -> {
            
            return null;
        });
    
    private static final XImgPreviewSizes
            psizes = new XImgPreviewSizes();
    
    private static final XImgPreviewGen.PreviewGeneratorProgressListener
            progressInd = new XImgPreviewGen.PreviewGeneratorProgressListener() {
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
            Logger.getLogger(XImg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void init(String databaseName) throws Exception {
        rootDatabaseName = databaseName;
        cryptoEx.init(databaseName);
        XImgDatastore.init(cryptoEx, databaseName); 
        HibernateUtil.hibernateInit(rootDatabaseName, "jneko", cryptoEx.getPassword());
        psizes.refreshPreviewSizes();
        addNewImagesTab = new TabAddImagesToDB(cryptoEx, databaseName);
    }
    
    public static XImgPreviewSizes getPSizes() {
        return psizes;
    }

    public static void wipeFSCache() {
        levelDB.get(PreviewType.cache.name()).forEach((c) -> {
            levelDB.get(PreviewType.cache.name()).delete(c.getKey());
        });
        addNewImagesTab.reinit();
    }
    
    public static void dispose() {
        addNewImagesTab.dispose();
        HibernateUtil.dispose();

        final Set<String> s = levelDB.keySet();
        s.forEach((x) -> {
             try {
                levelDB.get(x).close();
            } catch (IOException ex) { }
        });
    }

    public static XImgCrypto getCrypt() {
        return cryptoEx;
    }
    
    public static TabAddImagesToDB getAddImagesTab() {
        return addNewImagesTab;
    }
    
    public static XImgPreviewGen.PreviewGeneratorProgressListener getProgressListener() {
        return progressInd;
    }
    
    public static void progressShow() {
        progressDialog.show();
    }
    
    public static void msgbox(String text) {
        messageBox.showMsgbox(text);
    }
}
