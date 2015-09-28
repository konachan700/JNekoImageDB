package albums;

import dataaccess.DBWrapper;
import dialogs.DialogWindow;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

public class AlbumSelectDialog {
    private interface ASDElementActionListener {
        void OnCheck(Long id, ASDElement e);
        void OnUncheck(Long id, ASDElement e);
        void OnItemClick(Long id, ASDElement e);
        void OnSave(Long id, ASDElement e, String newTitle);
    }
    
    private interface ASDNewElementActionListener {
        void OnNew(long parent, String title);
    }
    
    private class ASDNewElement extends HBox{
        private final Button
                addBtn = new Button("", new ImageView(new Image(new File("./icons/plus32.png").toURI().toString())));
        
        private final TextField
                newItemName = new TextField();
        
        private final Label       
                newTitle = new Label("Добавить альбом");
        
        private final ASDNewElementActionListener
                elAL;
        
        private final long
                parent_id;
        
        public ASDNewElement(ASDNewElementActionListener al, long pid) {
            super();
            this.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            this.getStyleClass().add("ASDElementHBox");
            
            elAL = al;
            parent_id = pid;
            
            GUITools.setFixedSize(newTitle, 196, 32);
            newTitle.setAlignment(Pos.CENTER_LEFT);
            newTitle.getStyleClass().add("newTitle");
            
            GUITools.setFixedSize(addBtn, 32, 32);
            addBtn.getStyleClass().add("ImgButtonG");
            addBtn.setOnMouseClicked((MouseEvent event) -> {
                if (newItemName.getText().trim().length() > 0) elAL.OnNew(parent_id, newItemName.getText().trim());
            });
            
            GUITools.setMaxSize(newItemName, 9999, 32);
            newItemName.getStyleClass().add("newItemName");
            
            this.getChildren().addAll(newTitle, newItemName, addBtn);
        }
    }
    
    private class ASDElement extends HBox{
        private final Image
            sel = new Image(new File("./icons/selected16.png").toURI().toString()),
            unsel = new Image(new File("./icons/unselected16.png").toURI().toString());

        private boolean     selectState     = false;
        private final Long  ID, parent;
        
        private final TextField       
                title = new TextField();
        
        private final ASDElementActionListener
                elementAL;
        
        private final Button
                checkBtn = new Button(),
                saveBtn = new Button("", new ImageView(new Image(new File("./icons/save16.png").toURI().toString())));
        
        public ASDElement(Long id, Long pid, String xtitle, ASDElementActionListener al) {
            super();
            this.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            this.getStyleClass().add("ASDElementHBox");
            
            ID          = id;
            elementAL   = al;
            parent      = pid;
            
            title.setText(xtitle);
            
            _init();
        }
        
        private void _init() {
            GUITools.setFixedSize(saveBtn, 16, 16);
            saveBtn.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            saveBtn.getStyleClass().add("SSaveBox");
            saveBtn.setOnMouseClicked((MouseEvent event) -> {
                if (title.getText().trim().length() > 0) elementAL.OnSave(ID, this, title.getText().trim()); 
            });
            
            GUITools.setFixedSize(checkBtn, 16, 16);
            checkBtn.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            checkBtn.getStyleClass().add("SCheckBox");
            checkBtn.setOnMouseClicked((MouseEvent event) -> {
                selectState = !selectState;
                if (ID > 0) 
                    checkBtn.setGraphic(new ImageView((selectState) ? sel : unsel));
                
                if (selectState)  
                    if (ID > 0) elementAL.OnCheck(ID, this); 
                else 
                    if (ID > 0) elementAL.OnUncheck(ID, this); 
                
                event.consume();
            }); 
            
            GUITools.setMaxSize(title, 9999, 16);
            title.setAlignment(Pos.CENTER_LEFT);
            title.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            title.getStyleClass().add("SEC_Label");
            title.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1)
                    elementAL.OnItemClick(ID, this);
            });
            
            if (ID > 0) {
                this.getChildren().addAll(checkBtn, title, saveBtn);
            } else {
                title.setEditable(false);
                this.getChildren().addAll(checkBtn, title);
            }
        }
    }
    
    private long 
            albumID = 0;
    
    private final ASDNewElementActionListener
            newAL = (long parent, String title) -> {
                DBWrapper.addNewAlbumGroup(title, albumID);
                refresh();
    };
    
    private final ASDElementActionListener
            elAL = new ASDElementActionListener() {
                @Override
                public void OnCheck(Long id, ASDElement e) {
                    selectedElements.add(e);
                }

                @Override
                public void OnUncheck(Long id, ASDElement e) {
                    selectedElements.remove(e);
                }

                @Override
                public void OnItemClick(Long id, ASDElement e) {
                    if (id > 0) 
                        genAlbList(id);
                    else {
                        long parent_el = DBWrapper.getParentAlbum(e.parent);
                        genAlbList(parent_el);
                    }
                        
                }

                @Override
                public void OnSave(Long id, ASDElement e, String t) {
                    DBWrapper.saveAlbumsCategoryChanges(t, 0, id);
                }
            };
    
    private final ArrayList<ASDElement>
            selectedElements = new ArrayList<>();

    private final Button 
            yesImg = new Button("", new ImageView(new Image(new File("./icons/d_yes.png").toURI().toString()))), 
            noImg  = new Button("", new ImageView(new Image(new File("./icons/d_no.png").toURI().toString())));
    
    private final TextArea 
            messageStr = new TextArea("");
        
    private final DialogWindow 
            dw = new DialogWindow(700, 800);
    
    private final ScrollPane
            sp = new ScrollPane();
    
    private final HBox 
            panel = new HBox();
    
    private final VBox
            mainContainer = new VBox();
    
    public AlbumSelectDialog() {
        GUITools.setMaxSize(panel, 9999, 64);
        panel.setMinSize(128, 64);
        panel.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        panel.getStyleClass().add("topPanel");
        
        messageStr.setText("Выберите альбомы");
        messageStr.setEditable(false);
        messageStr.setWrapText(true);
        
        GUITools.setFixedSize(yesImg, 64, 64);
        GUITools.setFixedSize(noImg, 64, 64);
        GUITools.setMaxSize(messageStr, 9999, 64);
        
        setStyle(yesImg, "DYesButton");
        setStyle(noImg, "DNoButton");
        setStyle(messageStr, "DMessageStr");
        
        panel.getChildren().addAll(messageStr, noImg, yesImg);
        dw.getToolbox().getChildren().add(panel);
        
        sp.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        sp.getStyleClass().add("ImageList");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        
        GUITools.setMaxSize(sp, 9999, 9999);
        
        mainContainer.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        mainContainer.getStyleClass().add("mainContainer");
        GUITools.setMaxSize(mainContainer, 9999, 9999);
        
        sp.setContent(mainContainer);
        dw.getMainContainer().getChildren().add(sp);
        
        genAlbList(0);
    }
    
    private void genAlbList(long aid) {
        mainContainer.getChildren().clear();
        ArrayList<AlbumsCategory> alac = DBWrapper.getAlbumsGroupsID(aid);
        if (alac == null) return;
        
        albumID = aid;
        if (albumID > 0) {
            final ASDElement el_root = new ASDElement(-1L, albumID, "...", elAL);
            mainContainer.getChildren().add(el_root);
        }
        
        alac.stream().map((a) -> new ASDElement(a.ID, a.parent, a.name, elAL)).forEach((el) -> {
            mainContainer.getChildren().add(el);
        });
        
        if (albumID > 0) {
            final ASDNewElement ne = new ASDNewElement(newAL, albumID);
            mainContainer.getChildren().add(ne);
        }
    }
    
    private void refresh() {
        genAlbList(albumID);
    }
    
    public int Show(ArrayList<Long> iids) {
        /* TODO: дописать добавление элементов */
        
        dw.show();
        
        return 1;
    }

    private void setStyle(Region n, String styleID) {
        n.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        n.getStyleClass().add(styleID);
    }
}
