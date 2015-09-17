package albums;

import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;
import menulist.MenuGroupItem;

public class AlbumsCategories extends ScrollPane {
    public class ACListItem extends HBox {
        public AlbumsCategory AC;
        private TextField lTextField;
        
        public ACListItem(AlbumsCategory ac) {
            super(2);
            AC = ac;
            
            this.getStyleClass().add("itemHBox");
            GUITools.setMaxSize(this, 9999, 32);
            
            //Label l = new Label(ac.name);
            lTextField = new TextField(ac.name);
            //l.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("albgrp.png"))));
            lTextField.getStyleClass().add("itemLabel");
            GUITools.setMaxSize(lTextField, 9999, 32);
            
            ImageView i = new ImageView(new Image(getClass().getResourceAsStream("albgrp.png")));
            i.setFitHeight(32);
            i.setFitWidth(32);
            
            Button b = new Button("", new ImageView(new Image(getClass().getResourceAsStream((ac.state==1) ? "delete2.png" : "selected.png"))));
            b.getStyleClass().add("itemButton");
            GUITools.setFixedSize(b, 32, 32);
            b.setOnMouseClicked((MouseEvent event) -> {
                if (AC.state == 0) {
                    AC.state = 1;
                    AC.saveChanges();
                } else {
                    AC.state = 0;
                    AC.saveChanges();
                }
                RefreshAll();
            });
            
            Button s = new Button("", new ImageView(new Image(getClass().getResourceAsStream("save2.png"))));
            s.getStyleClass().add("itemButton");
            GUITools.setFixedSize(s, 32, 32);
            s.setOnMouseClicked((MouseEvent event) -> {
                if (lTextField.getText().trim().length() <= 1) return;
                ac.name = lTextField.getText().trim();
                ac.saveChanges();
                RefreshAll();
            });
            
            this.getChildren().addAll(i, lTextField, s, b);
        }
    }
    
    private final VBox 
            mainPane = new VBox(2);
    
    private final HBox
            toolbox = new HBox(2);
    
    private final TextField
            txtAddNew = new TextField("Новая группа");
    
    private final Button
            todbImg = new Button("", new ImageView(new Image(getClass().getResourceAsStream("adddef.png"))));
       
    private MenuGroupItem MGI;
    
    public AlbumsCategories(MenuGroupItem mgi) {
        super();
        MGI     = mgi;
        
        this.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("sroll_pane");
        this.setContent(mainPane);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setMaxSize(9999, 9999);
        this.setPrefSize(9999, 9999);
        
        mainPane.getStyleClass().add("mainPane");
        
        toolbox.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        toolbox.getStyleClass().add("toolbox");
        GUITools.setMaxSize(toolbox, 9999, 64);
        txtAddNew.getStyleClass().add("txtAddNew");
        GUITools.setMaxSize(txtAddNew, 9999, 64);
        GUITools.setFixedSize(todbImg, 64, 64);
        todbImg.getStyleClass().add("ImgButtonR");
        toolbox.getChildren().addAll(txtAddNew, todbImg);
        
        todbImg.setOnMouseClicked((MouseEvent event) -> {
            if (txtAddNew.getText().trim().length() <= 1) return;
            DBWrapper.addNewAlbumGroup(txtAddNew.getText().trim()); 
            RefreshAll();
        });
    }
    
    public HBox getToolbox() {
        return toolbox;
    }
    
    public final void RefreshAll() {
        ArrayList<AlbumsCategory> alac = DBWrapper.getAlbumsGroupsID();
        if (alac == null) {

            return;
        }
        
        mainPane.getChildren().clear();
        MGI.clearAll();
        MGI.addLabel(Long.toString(ImageEngine.ALBUM_ID_FAVORITES), "Избранное");
        
        alac.stream().forEach((ac) -> {
            ACListItem acli = new ACListItem(ac);
            mainPane.getChildren().add(acli);
            if (ac.state == 0) {
                MGI.addLabel(""+ac.ID, ac.name);
            }
        });
        
        MGI.addLabel(Long.toString(ImageEngine.ALBUM_ID_DELETED), "Удаленные");
        MGI.Commit();
    }
}
