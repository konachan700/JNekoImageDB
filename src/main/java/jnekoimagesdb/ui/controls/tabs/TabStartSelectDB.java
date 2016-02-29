package jnekoimagesdb.ui.controls.tabs;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jnekoimagesdb.core.img.XImgCrypto;
import jnekoimagesdb.ui.GUITools;
import static jnekoimagesdb.ui.controls.PreviewTypesList.IMG32_SELECTED;
import static jnekoimagesdb.ui.controls.PreviewTypesList.IMG32_SETPRIMARY;
import jnekoimagesdb.ui.controls.elements.GUIActionListener;
import jnekoimagesdb.ui.controls.elements.SButton;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SElementPair;
import jnekoimagesdb.ui.controls.elements.SFHBox;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.SNumericTextField;
import jnekoimagesdb.ui.controls.elements.SScrollPane;
import jnekoimagesdb.ui.controls.elements.STextField;

public class TabStartSelectDB extends SEVBox {
    public static final int
            ELEMENT_Y_SIZE = 32,
            ELEMENT_LABEL_SIZE = 140,
            ELEMENT_TF_HEIGHT = 24;
    
    public static final Image 
            IMG32_SELECTED = GUITools.loadIcon("selected-32"),
            IMG32_NOT_SELECTED = GUITools.loadIcon("unselected-32"),
            IMG32_NORMAL = GUITools.loadIcon("options-1-32"); 
    
    public static interface DBElListener {
        public void OnItemSelect(DBElement el, Path p, String name);
    }
    
    public static class DBElement extends SFHBox {
        private final Path
                elementPath;
        
        private final SButton
                btnSel;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public DBElement(Path db, DBElListener ale) {
            super(0, 128, 9999, 48, 48, "prevsz_container");
            this.setAlignment(Pos.CENTER);
            
            elementPath = db;
            btnSel = new SButton(
                    IMG32_NOT_SELECTED, 0, 32, 
                    (c, d) -> { 
                        ale.OnItemSelect(this, elementPath, elementPath.toFile().getName()); 
                    }, 
                    "button_prevsz_el");
            this.getChildren().addAll(
                    new ImageView(IMG32_NORMAL),
                    new SFLabel(db.toFile().getName(), 128, 9999, 32, 32, "label_left", "TypesListItem"),
                    btnSel
            );
        }
        
        public void setSelected(boolean s) {
            btnSel.setGraphic((!s) ? new ImageView(IMG32_NOT_SELECTED) : new ImageView(IMG32_SELECTED));
        }
    }
    
    private final SEVBox
            elementsContainer = new SEVBox(2);
    
    private final SScrollPane
            itemsScroll = new SScrollPane();
    
    private Path 
            dbPath = null;
    
    private String 
            dbName = "";
    
    private final DBElListener
            actListener = (el, p, name) -> {
                elementsContainer.getChildren().forEach(n -> {
                    if (n instanceof DBElement) {
                        ((DBElement) n).setSelected(false);
                    }
                });
                el.setSelected(true);
                dbPath = p;
                dbName = name;
            };

    private boolean ifDBFolder(Path p) {
        final Path 
                publicKey = FileSystems.getDefault().getPath(p.toString(), XImgCrypto.PUBLIC_KEY_NAME),
                keystore = FileSystems.getDefault().getPath(p.toString(), XImgCrypto.KEYSTORE_NAME);
        return Files.isReadable(publicKey) && Files.isReadable(keystore);
    }
    
    public String getDBName() {
        return dbName;
    }
    
    public Path getDBPath() {
        return dbPath;
    }
    
    public TabStartSelectDB() {
        super(0);
        final Path 
                currentFolder = FileSystems.getDefault().getPath(".").toAbsolutePath();
        elementsContainer.getChildren().clear();
        
        try {
            Files.list(currentFolder).forEach(c -> {
                if (Files.isDirectory(c)) {
                    if (ifDBFolder(c)) 
                        elementsContainer.getChildren().add(
                                new DBElement(c, actListener)
                        );
                }
            });
            
            itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            itemsScroll.setFitToHeight(true);
            itemsScroll.setFitToWidth(true);
            itemsScroll.setContent(elementsContainer);
        
            this.getChildren().addAll(
                    new SFLabel("Открыть существующую базу данных", 64, 9999, 36, 36, "label_darkgreen", "TypesListItem"),
                    itemsScroll
            );
            
        } catch (IOException ex) {
            Logger.getLogger(TabStartSelectDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


//    private final STextField
//            dbName = new STextField(ELEMENT_TF_HEIGHT);

//        this.getChildren().addAll(
//                new SElementPair(
//                        new SFLabel("Имя БД", 120, 9999, ELEMENT_Y_SIZE, ELEMENT_Y_SIZE, "label", "TypesListItem"),
//                        dbName, 
//                        4, ELEMENT_Y_SIZE, ELEMENT_LABEL_SIZE
//                )
//        );
        
        
        
        //SElementPair