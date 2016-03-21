package jnekoimagesdb.ui.controls.tabs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImg.PreviewType;
import jnekoimagesdb.core.img.XImgCrypto;
import jnekoimagesdb.core.img.XImgPreviewSizes;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SButton;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SElementPair;
import jnekoimagesdb.ui.controls.elements.SFHBox;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.SNumericTextField;
import jnekoimagesdb.ui.controls.elements.STabTextButton;
import jnekoimagesdb.ui.controls.elements.STextField;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class TabStartNewDB extends SEVBox {
    private static final ImageView
            errIcon = new ImageView(GUITools.loadIcon("delete-32"));
    
    public static interface TabStartNewDBState {
        public void OnDBCreated(String dbName);
    }
    
    private final TabStartNewDBState
            outActListener;
            
    private final XImgCrypto
        cryptoEx = new XImgCrypto(() -> {
            return null;
        });
    
    private final EnumMap<PreviewType, DB>
            levelDB = new EnumMap(PreviewType.class);
        
    private final XImgPreviewSizes
            psizes = new XImgPreviewSizes();
    
    private final SEVBox
            tab01 = new SEVBox(0),
            tab02 = new SEVBox(2);
    
    private final SFHBox
            addNewItem = new SFHBox(4, 120, 9999, 32+12, 32+12, "AddNewAlbumElement");
    
    private final SNumericTextField
            hf = new SNumericTextField(ElementsIDCodes.textUnknown, 70, 32, null), 
            wf = new SNumericTextField(ElementsIDCodes.textUnknown, 70, 32, null);
    
    private boolean 
            isSqared = false,
            locked = false;
   
    private final ImageView
            imgButton = new ImageView(GUITools.loadIcon("unselected2-16"));
    
    private final SButton
            btn = new SButton(GUITools.loadIcon("unselected2-16"), ElementsIDCodes.textUnknown, 32, (c, d) -> {
                    isSqared = !isSqared;
                    imgButton.setImage((isSqared) ? GUITools.loadIcon("selected-16") : GUITools.loadIcon("unselected2-16")); 
                }, "button_pts");
    
    private final STextField
            txtDBName = new STextField(ElementsIDCodes.textUnknown, -1, 32, (x , y) -> {
                checkField();
            });

    private Path 
            dbPath = null;
    
    private String 
            dbName = null;

    public String getDBName() {
        return dbName;
    }
    
    public Path getDBPath() {
        return dbPath;
    }
    
    public TabStartNewDB(TabStartNewDBState ts) {
        super(0);
        outActListener = ts;
        
        wf.max = 800;
        wf.min = 32;
        hf.max = 800;
        hf.min = 32;
        
        GUITools.setMaxSize(tab01, 9999, 9999);
        GUITools.setMaxSize(tab02, 9999, 9999);
        txtDBName.setHelpText("Ведите имя БД");
        btn.setGraphic(imgButton);
        
        addNewItem.getChildren().addAll(new SFLabel("Ширина:", 64, 64, 32, 32, "label", "TypesListItem"), 
                wf,
                new SFLabel("Высота:", 64, 64, 32, 32, "label", "TypesListItem"),
                hf,
                new SFLabel("Обрезка:", 64, 64, 32, 32, "label", "TypesListItem"),
                btn,
                GUITools.getSeparator()
        );
        GUITools.setMaxSize(addNewItem, 9999, 32);
                
        tab02.getChildren().addAll(
                GUITools.getHSeparator(32),
                new SFLabel("Пожалуйста, подождите...", 64, 9999, 50, 50, "label_darkgreen", "TypesListItem"),
                GUITools.getHNFSeparator(9999)
        );
        
        tab01.getChildren().addAll(
                new SFLabel("Название базы данных", 64, 9999, 20, 20, "label_darkred", "TypesListItem"),
                new SFLabel("В названии БД допускаются только английские символы, минус и нижнее подчеркивание. После ввода имени БД нажмите кнопку \"Далее\".", 
                        36, 9999, 44, 44, "label_darkred_small", "TypesListItem"),
                txtDBName,
                GUITools.getHSeparator(16),
                new SFLabel("Настройка формата превью.", 64, 9999, 20, 20, "label_darkgreen", "TypesListItem"),
                new SFLabel("Размер превью может быть от 64 до 800 пикселей по ширине и от 64 до 800 пикселей по высоте.", 
                        36, 9999, 44, 44, "label_darkgreen_small", "TypesListItem"),
                addNewItem,
                GUITools.getHNFSeparator(9999),
                new SElementPair(
                        errIcon, 
                        4, 32, 32,
                        GUITools.getSeparator(),
                        new STabTextButton("  Создать БД  ", ElementsIDCodes.buttonUnknown , 200, 32, (x, y) -> {
                            if (((wf.getLongValue() >= 64) && (wf.getLongValue() <= 800)) && ((hf.getLongValue() >= 64) && (hf.getLongValue() <= 800))) {
                                if (dbName != null) {
                                    createDBX();
                                } else 
                                    XImg.msgbox("Название БД введено некорректно!");
                            } else 
                                XImg.msgbox("Введены неверные размеры превью!");
                        })
                ).setAlign(Pos.CENTER_RIGHT)
        );

        this.getChildren().add(tab01);
    }
    
    private void createDBX() {
        locked = true;
        this.getChildren().clear();
        this.getChildren().add(tab02);
        
        final Runnable
            dbCreationThread = () -> {
                if (createDB()) {
                    final StringBuilder sb = new StringBuilder();
                    sb
                            .append("Preview_")
                            .append(wf.getLongValue())
                            .append("x")
                            .append(hf.getLongValue())
                            .append("_")
                            .append((isSqared) ? "SQ" : "NS");
                    psizes.addPreviewSize(sb.substring(0), wf.getLongValue(), hf.getLongValue(), isSqared);
                    psizes.get(0).setPrimary(true);
                    
                    SettingsUtil.setLong("mainPreviewGenThreadsCount.value", 4);
                    SettingsUtil.setLong("previewFSCacheThreadsCount.value", 4);
                    
                    HibernateUtil.dispose();

                    Platform.runLater(() -> { outActListener.OnDBCreated(dbName); });
                } else {
                    XImg.msgbox("Невозможно создать БД: диск переполнен или папка недоступна на запись.");
                    Platform.runLater(() -> { 
                        this.getChildren().clear();
                        this.getChildren().add(tab01);
                        locked = false;
                    });
                }

            };
        new Thread(dbCreationThread).start();
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    private void checkField() {
        if (txtDBName.getText().matches("^[a-zA-Z0-9_-]{1,32}$")) {
            errIcon.setImage(GUITools.loadIcon("selected-32")); 
            dbName = txtDBName.getText();
        } else {
            errIcon.setImage(GUITools.loadIcon("delete-32")); 
            dbName = null;
        }
    }
    
    private boolean createDB() {
        if (!new File(dbName).mkdir()) return false;
        
        try {
            cryptoEx.init(dbName);
            initIDB(PreviewType.cache);
            initIDB(PreviewType.previews);
            final Set<PreviewType> s = levelDB.keySet();
            s.forEach((x) -> {
                 try {
                    levelDB.get(x).close();
                } catch (IOException ex) {}
            });
            
            HibernateUtil.hibernateInit(dbName, "jneko", cryptoEx.getPassword());
            SettingsUtil.init();
            psizes.refreshPreviewSizes();
            
            return true;
        } catch (Exception ex) {
            Logger.getLogger(TabStartNewDB.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void initIDB(PreviewType dbNameIDB) {
        final File levelDBFile = new File(dbName + File.separator + dbNameIDB.name());
        Options options = new Options();
        options.createIfMissing(true);   
        try {
            levelDB.put(dbNameIDB, factory.open(levelDBFile, options));
        } catch (IOException ex) {
            Logger.getLogger(XImg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
