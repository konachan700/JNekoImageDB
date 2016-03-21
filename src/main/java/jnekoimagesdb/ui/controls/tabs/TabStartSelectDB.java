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
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SButton;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SFHBox;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.SScrollPane;

public class TabStartSelectDB extends SEVBox {
    public static final int
            ELEMENT_Y_SIZE = 32,
            ELEMENT_LABEL_SIZE = 140,
            ELEMENT_TF_HEIGHT = 24;
    
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
                    GUITools.loadIcon("unselected-32"), ElementsIDCodes.buttonUnknown, 32, 
                    (c, d) -> { 
                        ale.OnItemSelect(this, elementPath, elementPath.toFile().getName()); 
                    }, 
                    "button_prevsz_el");
            this.getChildren().addAll(
                    new ImageView(GUITools.loadIcon("options-1-32")),
                    new SFLabel(db.toFile().getName(), 128, 9999, 32, 32, "label_left", "TypesListItem"),
                    btnSel
            );
        }
        
        public void setSelected(boolean s) {
            btnSel.setGraphic((!s) ? new ImageView(GUITools.loadIcon("unselected-32")) : new ImageView(GUITools.loadIcon("selected-32")));
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