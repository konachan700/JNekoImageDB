package albums;

import dataaccess.ImageEngine;
import dataaccess.SQLite;
import imagelist.ImageList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
    
    private final SQLite SQL;
    
    public AlbumImageList(ImageEngine im, SQLite sql) {
        super(2);
        IM = im;
        SQL = sql;
        IML = new ImageList(IM, SQL);
        init();
    }
    
    public ImageList getImageList() {
        return IML;
    }
    
    private void init() {
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("AlbumImageList");
        _s1(this, 9999, 9999);
        
        _s1(albumList, 9999, 9999);
        
        imgLabel.setOnMouseClicked((MouseEvent event) -> {
            this.getChildren().remove(albumList);
            this.getChildren().add(IML);
            event.consume();
        });
        imgLabel.getStyleClass().add("tabLabelB");
        imgLabel.setAlignment(Pos.CENTER);
        _s1(imgLabel, 9999, 24);
        
        albLabel.setOnMouseClicked((MouseEvent event) -> {
            this.getChildren().remove(IML);
            this.getChildren().add(albumList);
            event.consume();
        });
        albLabel.getStyleClass().add("tabLabelA");
        albLabel.setAlignment(Pos.CENTER);
        _s1(albLabel, 9999, 24);
        
        tabsHBox.getStyleClass().add("tabsHBox");
        tabsHBox.setAlignment(Pos.CENTER);
        _s1(tabsHBox, 9999, 24);
        
        tabsHBox.getChildren().addAll(imgLabel, albLabel);
        this.getChildren().addAll(tabsHBox, IML);
    }

    private VBox getSeparator1() {
        VBox sep1 = new VBox();
        _s1(sep1, 9999, 16);
        return sep1;
    }
    
    private VBox getSeparator1(double sz) {
        VBox sep1 = new VBox();
        _s2(sep1, sz, sz);
        return sep1;
    }
    
    private void _s2(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
        n.setMinSize(w, h);
    }
    
    private void _s1(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
    }
}
