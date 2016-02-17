package jnekoimagesdb.core.img;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import static org.fusesource.leveldbjni.JniDBFactory.factory;

import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.controls.PagedImageList;
import jnekoimagesdb.ui.controls.dialogs.DialogMessageBox;
import jnekoimagesdb.ui.controls.dialogs.XImageUpload;
import jnekoimagesdb.ui.controls.tabs.TabAlbumImageList;
import jnekoimagesdb.ui.controls.tabs.TabAllImages;
import jnekoimagesdb.ui.controls.tabs.TabAllTags;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class XImg {
    public static enum PreviewType {
        cache, previews
    }
    
    public static final Path 
            LOG_DIR = FileSystems.getDefault().getPath("./logs/").toAbsolutePath();
    
    private static final TextArea
            taLOG = new TextArea();
        
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

    private static final PagedImageList
            pagedImageList = new PagedImageList();
    
    private static final TabAllImages
            tabAllImages = new TabAllImages();
    
    private static final TabAlbumImageList
            tabAlbumImageList = new TabAlbumImageList();
    
    private static final DialogMessageBox
            messageBox = new DialogMessageBox();
    
    private static final TabAllTags
            tabAllTags = new TabAllTags();
    
    private static String                   
            rootDatabaseName;

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
        pagedImageList.initDB();
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
        HibernateUtil.dispose();
        imgUpl.dispose();
        pagedImageList.dispose();
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
    
    public static PagedImageList getPagedImageList() {
        return pagedImageList;
    }
    
    public static TabAllTags getTabAllTags() {
        return tabAllTags;
    }
    
    public static TabAllImages getTabAllImages() {
        return tabAllImages;
    }
    
    public static TabAlbumImageList getTabAlbumImageList() {
        return tabAlbumImageList;
    }

    public static void msgbox(String text) {
        messageBox.showMsgbox(text);
    }
    
    public static XImageUpload getUploadBox() {
        return imgUpl;
    }
    
    public static TextArea getTALog() {
        return taLOG;
    }
}
