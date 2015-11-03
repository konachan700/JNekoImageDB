package albums;

import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.Lang;
import java.io.File;
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
        private TextField itemLabel;
        
        public ACListItem(AlbumsCategory ac) {
            super(0);
            AC = ac;

            this.getStyleClass().add("AlbumsCategories_ACListItem_itemHBox");
            GUITools.setMaxSize(this, 9999, 32);
            
            itemLabel = new TextField(ac.name);
            itemLabel.getStyleClass().add("AlbumsCategories_ACListItem_itemLabel");
            GUITools.setMaxSize(itemLabel, 9999, 32);
            
            ImageView i = new ImageView(new Image(new File("./icons/albgrp.png").toURI().toString()));
            i.setFitHeight(32);
            i.setFitWidth(32);
            
            Button itemButton = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/"+((ac.state==1) ? "delete2.png" : "selected.png")).toURI().toString())));
            itemButton.getStyleClass().add("AlbumsCategories_ACListItem_itemButton");
            GUITools.setFixedSize(itemButton, 32, 32);
            itemButton.setOnMouseClicked((MouseEvent event) -> {
                if (AC.state == 0) {
                    AC.state = 1;
                    AC.saveChanges();
                } else {
                    AC.state = 0;
                    AC.saveChanges();
                }
                RefreshAll();
            });
            
            Button itemButton2 = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/save2.png").toURI().toString())));
            itemButton2.getStyleClass().add("AlbumsCategories_ACListItem_itemButton");
            GUITools.setFixedSize(itemButton2, 32, 32);
            itemButton2.setOnMouseClicked((MouseEvent event) -> {
                if (itemLabel.getText().trim().length() <= 1) return;
                ac.name = itemLabel.getText().trim();
                ac.saveChanges();
                RefreshAll();
            });
            
            this.getChildren().addAll(i, itemLabel, itemButton2, itemButton);
        }
    }
    
    private final VBox 
            mainPane = new VBox(2);
    
    private final HBox
            toolbox = new HBox(2);
    
    private final TextField
            txtAddNew = new TextField();
    
    private final Button
            todbImg = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/plus32.png").toURI().toString())));
            //new Button(Lang.NullString, new ImageView(new Image(new File("./icons/add-to-album.png").toURI().toString())));
       
    private MenuGroupItem MGI;
    
    public AlbumsCategories(MenuGroupItem mgi) {
        super();
        MGI = mgi;
        
        this.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("AlbumsCategories_Pane");
        this.setContent(mainPane);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setMaxSize(9999, 9999);
        this.setPrefSize(9999, 9999);
        
        mainPane.getStyleClass().add("AlbumsCategories_mainPane");
        
        txtAddNew.setPromptText(Lang.AlbumsCategories_txtAddNew);
        
        toolbox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        toolbox.getStyleClass().add("AlbumsCategories_toolbox");
        GUITools.setMaxSize(toolbox, 9999, 32);
        txtAddNew.getStyleClass().add("AlbumsCategories_txtAddNew");
        GUITools.setMaxSize(txtAddNew, 9999, 32);
        GUITools.setFixedSize(todbImg, 32, 32);
        todbImg.getStyleClass().add("AlbumsCategories_todbImg");
        toolbox.getChildren().addAll(txtAddNew, todbImg);
        
        todbImg.setOnMouseClicked((MouseEvent event) -> {
            if (txtAddNew.getText().trim().length() <= 1) return;
            DBWrapper.addNewAlbumGroup(txtAddNew.getText().trim()); 
            RefreshAll();
        });
    }
    
//    public HBox getToolbox() {
//        return toolbox;
//    }
    
    public final void RefreshAll() {
        ArrayList<AlbumsCategory> alac = DBWrapper.getAlbumsGroupsID();
        if (alac == null) {

            return;
        }
        
        mainPane.getChildren().clear();
        MGI.clearAll();
        MGI.addLabel(Long.toString(ImageEngine.ALBUM_ID_FAVORITES), Lang.AlbumsCategories_MenuItem_FAVORITES);
        
        alac.stream().forEach((ac) -> {
            ACListItem acli = new ACListItem(ac);
            mainPane.getChildren().add(acli);
            if (ac.state == 0) {
                MGI.addLabel(Lang.NullString + ac.ID, ac.name);
            }
        });
        
        mainPane.getChildren().add(toolbox);

        MGI.addLabel(Long.toString(ImageEngine.ALBUM_ID_DELETED), Lang.AlbumsCategories_MenuItem_DELETED);
        MGI.Commit();
    }
}
