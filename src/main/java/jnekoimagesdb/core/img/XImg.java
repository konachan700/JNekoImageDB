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
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.md.imagelist.PagedImageList;
import jnekoimagesdb.ui.controls.dialogs.XAlbumsExport;
import jnekoimagesdb.ui.controls.dialogs.XDialogImgCacheRebuild;
import jnekoimagesdb.ui.controls.dialogs.XDialogOpenDirectory;
import jnekoimagesdb.ui.controls.dialogs.XImageUpload;
import jnekoimagesdb.ui.md.albums.Albums;
import jnekoimagesdb.ui.md.dialogs.MessageBox;
import jnekoimagesdb.ui.md.dialogs.fs.OpenDirectoryDialog;
import jnekoimagesdb.ui.md.dialogs.fs.OpenSaveFileDialog;
import jnekoimagesdb.ui.md.tags.TagsEditor;
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
            
            MessageBox.show("Приватный ключ не найден!");
            // todo: добавить диалог выбора файла приватного ключа
            return null;
        });
    
    private static final XImgPreviewSizes
            psizes = new XImgPreviewSizes();
    
    private static final XImageUpload
            imgUpl = new XImageUpload();

    private static final Albums
            tabAlbumImageList = new Albums();
    
    private static final XDialogOpenDirectory
            openDirDialog  = new XDialogOpenDirectory();
    
    private static final XAlbumsExport
            albumExportDialog = new XAlbumsExport();
    
    private static final TagsEditor
            tabAllTags = new TagsEditor();
    
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
        PagedImageList.get().initDB();
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
        final Set<PreviewType> s = levelDB.keySet();
        s.forEach((x) -> {
             try {
                levelDB.get(x).close();
            } catch (IOException ex) { }
        });
        OpenDirectoryDialog.dispose();
        OpenSaveFileDialog.dispose();
    }

    public static XImgCrypto getCrypt() {
        return cryptoEx;
    }

    public static TagsEditor getTabAllTags() {
        return tabAllTags;
    }

    public static Albums getTabAlbumImageList() {
        return tabAlbumImageList;
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
