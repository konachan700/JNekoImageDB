package img;

import datasources.HibernateUtil;
import datasources.SettingsUtil;
import img.gui.dialogs.DialogMessageBox;
import img.gui.dialogs.XImageUpload;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
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
        
    private static EnumMap<PreviewType, DB>
            levelDB = new EnumMap(PreviewType.class);
    
    private static final XImgCrypto
        cryptoEx = new XImgCrypto(() -> {
            
            return null;
        });
    
    private static final XImgPreviewSizes
            psizes = new XImgPreviewSizes();
    
    private static final XImageUpload 
            imgUpl = new XImageUpload();
    
//    private static final XImgPreviewGen.PreviewGeneratorProgressListener
//            progressInd = new XImgPreviewGen.PreviewGeneratorProgressListener() {
//                @Override
//                public void OnStartThread(int itemsCount, int tID) {
//                    progressDialog.itemProgresss(tID);
//                }
//
//                @Override
//                public void OnNewItemGenerated(int itemsCount, Path p, int tID, String quene) {
//                    progressDialog.itemSetInfo(tID, p, itemsCount, quene);
//                }
//
//                @Override
//                public void OnError(int tID) {
//
//                }
//
//                @Override
//                public void OnComplete(int tID) {
//                    progressDialog.itemComplete(tID);
//                }
//
//                @Override
//                public void OnCreated(int tID) {
//                    progressDialog.itemCreate(tID);
//                }   
//
//                @Override
//                public void OnInfoUpdate(int tID, String info) {
//                    progressDialog.itemSetInfo(tID, info); 
//                }
//            };
    
//    private static TabAddImagesToDB         addNewImagesTab;
//    private static DialogMTPrevGenProgress  progressDialog = new DialogMTPrevGenProgress();
    private static final DialogMessageBox   messageBox = new DialogMessageBox();
    private static String                   rootDatabaseName;

    public static DB getDB(PreviewType name) {
        return levelDB.get(name);
    }
    
    public static void initIDB(PreviewType dbName) {
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
        
        initIDB(PreviewType.cache);
        initIDB(PreviewType.previews);
        XImgDatastore.init(cryptoEx, databaseName); 
        HibernateUtil.hibernateInit(rootDatabaseName, "jneko", cryptoEx.getPassword());
        SettingsUtil.init();
        psizes.refreshPreviewSizes();
        imgUpl.init();
        
//        addNewImagesTab = new TabAddImagesToDB(cryptoEx, databaseName);
    }
    
    public static XImgPreviewSizes getPSizes() {
        return psizes;
    }

    public static void wipeFSCache() {
        levelDB.get(PreviewType.cache).forEach((c) -> {
            levelDB.get(PreviewType.cache).delete(c.getKey());
        });
    }
    
    public static void dispose() {
//        addNewImagesTab.dispose();
        HibernateUtil.dispose();
        imgUpl.dispose();
        final Set<PreviewType> s = levelDB.keySet();
        s.forEach((x) -> {
             try {
                levelDB.get(x).close();
            } catch (IOException ex) { }
        });
    }

    public static XImgCrypto getCrypt() {
        return cryptoEx;
    }
    
//    public static TabAddImagesToDB getAddImagesTab() {
//        return addNewImagesTab;
//    }
    
//    public static XImgPreviewGen.PreviewGeneratorProgressListener getProgressListener() {
//        return progressInd;
//    }
//    
//    public static void progressShow() {
//        progressDialog.show();
//    }
    
    public static void msgbox(String text) {
        messageBox.showMsgbox(text);
    }
    
    public static XImageUpload getUploadBox() {
        return imgUpl;
    }
}
