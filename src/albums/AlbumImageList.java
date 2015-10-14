package albums;

import dataaccess.ImageEngine;
import dataaccess.DBEngine;
import imagelist.ImageList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

public class AlbumImageList extends VBox {
    private final HBox 
            tabsHBox = new HBox(6);
    
    private final VBox
            albumList = new VBox(2);
    
    private final Label
            imgLabel = new Label("Картинки"),
            albLabel = new Label("Альбомы");
    
    private final ImageEngine 
            IM;
    
    private final ImageList
            IML;
    
    private final DBEngine SQL;
    
    public AlbumImageList(ImageEngine im, DBEngine sql, Pane parent) {
        super(2);
        IM = im;
        SQL = sql;
        IML = new ImageList(IM, parent);
        init();
    }
    
    public ImageList getImageList() {
        return IML;
    }
    
    private void init() {
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("AlbumImageList");
        GUITools.setMaxSize(this, 9999, 9999);
        
        GUITools.setMaxSize(albumList, 9999, 9999);
        
        imgLabel.setOnMouseClicked((MouseEvent event) -> {
            this.getChildren().remove(albumList);
            this.getChildren().add(IML);
            event.consume();
        });
        imgLabel.getStyleClass().add("tabLabelB");
        imgLabel.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(imgLabel, 9999, 27);
        
        albLabel.setOnMouseClicked((MouseEvent event) -> {
            this.getChildren().remove(IML);
            this.getChildren().add(albumList);
            event.consume();
        });
        albLabel.getStyleClass().add("tabLabelA");
        albLabel.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(albLabel, 9999, 27);
        
        tabsHBox.getStyleClass().add("tabsHBox");
        tabsHBox.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(tabsHBox, 9999, 27);
        tabsHBox.setMinHeight(27); 
        
        tabsHBox.getChildren().addAll(imgLabel, albLabel);
        this.getChildren().addAll(tabsHBox, IML);
    }
}
