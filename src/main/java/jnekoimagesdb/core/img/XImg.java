package jnekoimagesdb.core.img;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.DSImageIDListCache;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.controls.PagedImageList;
import jnekoimagesdb.ui.controls.dialogs.XAlbumsExport;
import jnekoimagesdb.ui.controls.dialogs.XDialogImgCacheRebuild;
import jnekoimagesdb.ui.controls.dialogs.XDialogMessageBox;
import jnekoimagesdb.ui.controls.dialogs.XDialogOpenDirectory;
import jnekoimagesdb.ui.controls.dialogs.XImageUpload;
import jnekoimagesdb.ui.controls.tabs.TabAlbumImageList;
import jnekoimagesdb.ui.controls.tabs.TabAllImages;
import jnekoimagesdb.ui.controls.tabs.TabAllTags;
import org.hibernate.criterion.Projections;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class XImg {
    private static String                   
            rootDatabaseName;
        
    public static enum PreviewType {
        cache, previews
    }
    
    public static final Path 
            LOG_DIR = FileSystems.getDefault().getPath("./logs/").toAbsolutePath();
    
    private static final TextArea
            taLOG = new TextArea();
        
    private static EnumMap<PreviewType, DB>
            levelDB = new EnumMap(PreviewType.class);
    
    @SuppressWarnings("StaticNonFinalUsedInInitialization")
    private static final XImgCrypto
        cryptoEx = new XImgCrypto(() -> {
            // для windows-систем. Ищет ключ на флешках, если находит - автоматом его пробует, не задавая лишних вопросов.
            // Для линуксов реализовать сложнее ввиду разных точек монтирования, потому там будет просто вылетать диалог выбора файла.
            final File[] fa = File.listRoots();
            Path p;
            for (File f : fa) {
                p = FileSystems.getDefault().getPath(f.getAbsolutePath(), "imgDB/"+rootDatabaseName+".pkey");
                if (Files.exists(p) && Files.isRegularFile(p) && Files.isReadable(p)) {
                    try {
                        return Files.readAllBytes(p);
                    } catch (IOException e) {}
                }
            }
            
            XImg.msgbox("Приватный ключ не найден!");
            // todo: добавить диалог выбора файла приватного ключа
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
    
    private static final XDialogMessageBox
            messageBox = new XDialogMessageBox();
    
    private static final XDialogOpenDirectory
            openDirDialog  = new XDialogOpenDirectory();
    
    private static final XAlbumsExport
            albumExportDialog = new XAlbumsExport();
    
    private static final TabAllTags
            tabAllTags = new TabAllTags();
    
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
        openDirDialog.init();
        albumExportDialog.init();
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
        XDialogImgCacheRebuild.get().dispose();
        HibernateUtil.dispose();
        imgUpl.dispose();
        pagedImageList.dispose();
        final Set<PreviewType> s = levelDB.keySet();
        s.forEach((x) -> {
             try {
                levelDB.get(x).close();
            } catch (IOException ex) { }
        });
        openDirDialog.dispose();
        albumExportDialog.dispose();
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
        messageBox.show(text);
    }
    
    public static XImageUpload getUploadBox() {
        return imgUpl;
    }
    
    public static TextArea getTALog() {
        return taLOG;
    }
    
    public static XDialogOpenDirectory openDir() {
        return openDirDialog;
    }
    
    public static XAlbumsExport exportAlbum() {
        return albumExportDialog;
    }
}
