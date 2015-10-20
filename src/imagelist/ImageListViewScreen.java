package imagelist;

import albums.AlbumSelectDialog;
import dataaccess.DBWrapper;
import dataaccess.Lang;
import dialogs.DialogWindow;
import java.io.File;
import java.util.ArrayList;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import jnekoimagesdb.GUITools;

public class ImageListViewScreen {
    private final ImageView 
            like = new ImageView(new Image(new File("./icons/like-48.png").toURI().toString())),
            nolike = new ImageView(new Image(new File("./icons/nolike-48.png").toURI().toString())),
            del = new ImageView(new Image(new File("./icons/delete-48.png").toURI().toString())),
            nodel = new ImageView(new Image(new File("./icons/nondelete-48.png").toURI().toString()));
    
    private final Button
            nextBtn = new Button("", new ImageView(new Image(new File("./icons/arrow-right.png").toURI().toString()))),
            prevBtn = new Button("", new ImageView(new Image(new File("./icons/arrow-left.png").toURI().toString()))),
            zoomInBtn = new Button("", new ImageView(new Image(new File("./icons/zoom-in.png").toURI().toString()))),
            zoomOutBtn = new Button("", new ImageView(new Image(new File("./icons/zoom-out.png").toURI().toString()))),
            zoomOrigBtn = new Button("", new ImageView(new Image(new File("./icons/zoom-original.png").toURI().toString()))),
            zoomFitToWinBtn = new Button("", new ImageView(new Image(new File("./icons/zoom-fit-best.png").toURI().toString()))),
            toTempBtn = new Button("", new ImageView(new Image(new File("./icons/addtotemp.png").toURI().toString()))),
            toAlbumBtn = new Button("", new ImageView(new Image(new File("./icons/add-to-album.png").toURI().toString()))),
            toTagBtn = new Button("", new ImageView(new Image(new File("./icons/add-tags.png").toURI().toString()))),
            likeBtn = new Button("", nolike),
            delBtn = new Button("", nodel);
                
    private final DialogWindow 
            dw = new DialogWindow();
    
    private final HBox 
            panel = new HBox();
    
    private final ScrollPane
            sp = new ScrollPane();
    
    private final ImageView
            imgC = new ImageView();
    
    private long 
            IID = 0,
            AID = 0;
    
    private volatile boolean 
            isLiked = false,
            isDeleted = false;
    
    private volatile double 
            scale = 0.95d,
            height = 0,
            width = 0,
            last_x = 0,
            last_y = 0,
            img_h = 0,
            img_w = 0;
    
    private void _next() {
        long oid = DBWrapper.getNextImage(IID);
        if (oid > 0) { 
            scale = 0.95d;
            _show(oid);
            setImg(IID);
        } 
    }
    
    private void _prev() {
        long oid = DBWrapper.getPrevImage(IID);
        if (oid > 0) { 
            scale = 0.95d;
            _show(oid);
            setImg(IID);
        }
    }
    
    public ImageListViewScreen() {
        GUITools.setMaxSize(panel, 9999, 64);
        panel.setMinSize(128, 64);
        panel.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        panel.getStyleClass().add("ImageListViewScreen_panel");
        
        nextBtn.getStyleClass().add("ImageListViewScreen_button");
        prevBtn.getStyleClass().add("ImageListViewScreen_button");
        zoomInBtn.getStyleClass().add("ImageListViewScreen_button");
        zoomOutBtn.getStyleClass().add("ImageListViewScreen_button");
        toTempBtn.getStyleClass().add("ImageListViewScreen_button");
        likeBtn.getStyleClass().add("ImageListViewScreen_button");
        delBtn.getStyleClass().add("ImageListViewScreen_button");
        toAlbumBtn.getStyleClass().add("ImageListViewScreen_button");
        toTagBtn.getStyleClass().add("ImageListViewScreen_button");
        zoomOrigBtn.getStyleClass().add("ImageListViewScreen_button");
        zoomFitToWinBtn.getStyleClass().add("ImageListViewScreen_button");
        
        GUITools.setFixedSize(nextBtn, 64, 64);
        GUITools.setFixedSize(prevBtn, 64, 64);
        GUITools.setFixedSize(zoomInBtn, 64, 64);
        GUITools.setFixedSize(zoomOutBtn, 64, 64);
        GUITools.setFixedSize(toTempBtn, 64, 64);
        GUITools.setFixedSize(likeBtn, 64, 64);
        GUITools.setFixedSize(delBtn, 64, 64);
        GUITools.setFixedSize(toAlbumBtn, 64, 64);
        GUITools.setFixedSize(toTagBtn, 64, 64); 
        GUITools.setFixedSize(zoomOrigBtn, 64, 64); 
        GUITools.setFixedSize(zoomFitToWinBtn, 64, 64);
        
        panel.getChildren().addAll(
                zoomInBtn, zoomOrigBtn, zoomFitToWinBtn, zoomOutBtn, GUITools.getSeparator(8), 
                prevBtn, nextBtn, GUITools.getSeparator(8), 
                likeBtn, delBtn, toAlbumBtn, toTagBtn, GUITools.getSeparator(8), 
                toTempBtn, GUITools.getSeparator());
        dw.getToolbox().getChildren().add(panel);
        
        sp.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        sp.getStyleClass().add("ImageListViewScreen_root_pane");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        
        GUITools.setMaxSize(sp, 9999, 9999);
        
        final BorderPane bp = new BorderPane();
        GUITools.setMaxSize(bp, 9999, 9999);
        bp.setCenter(imgC); 
        
        sp.setContent(bp);
        
        imgC.setSmooth(true);
        imgC.setCache(false);
        imgC.setPreserveRatio(true);
        imgC.setCursor(Cursor.OPEN_HAND);

        imgC.setOnMouseReleased((MouseEvent t) -> {
            last_x = 0;
            last_y = 0;
        });
        
        sp.setFocusTraversable(true);
        sp.setOnKeyPressed((KeyEvent key) -> {
            if (key.getCode() == KeyCode.LEFT) _prev();
            if (key.getCode() == KeyCode.RIGHT) _next();
        });
        
        panel.setFocusTraversable(true);
        panel.setOnKeyPressed((KeyEvent key) -> {
            if (key.getCode() == KeyCode.LEFT) _prev();
            if (key.getCode() == KeyCode.RIGHT) _next();
        });
        
        imgC.setOnMouseDragged((MouseEvent t) -> {
            if ((height < imgC.getFitHeight()) || (width < imgC.getFitWidth())) {
                if ((last_x > 0) && (last_y > 0)) {
                    sp.setHvalue(sp.getHvalue() + ((last_x - t.getSceneX()) / (width / 1.2d)));
                    sp.setVvalue(sp.getVvalue() + ((last_y - t.getSceneY()) / (height / 1.2d)));
                }
                
                last_x = t.getSceneX();
                last_y = t.getSceneY();
            }
        });
        
        dw.getMainContainer().getChildren().add(sp);
        
        toAlbumBtn.setOnMouseClicked((MouseEvent event) -> {
            final ArrayList<Long> tmp1 = new ArrayList<>();
            tmp1.add(IID);
            new AlbumSelectDialog().Show(tmp1);
        });      
        
        likeBtn.setOnMouseClicked((MouseEvent event) -> {
            isLiked = !isLiked;
            DBWrapper.setImageLiked(IID, isLiked);
            likeBtn.setGraphic((isLiked) ? like : nolike);
            event.consume();
        });
        
        delBtn.setOnMouseClicked((MouseEvent event) -> {
            isDeleted = !isDeleted;
            DBWrapper.setImageDeleted(IID, isDeleted);
            delBtn.setGraphic((isDeleted) ? del : nodel);
            event.consume();
        });
        
        nextBtn.setOnMouseClicked((MouseEvent event) -> {
            _next();
            event.consume();
        });
        
        prevBtn.setOnMouseClicked((MouseEvent event) -> {
            _prev(); 
            event.consume();
        });
        
        zoomOrigBtn.setOnMouseClicked((MouseEvent event) -> {
            setImgOrig(IID);
            event.consume();
        });
        
        zoomInBtn.setOnMouseClicked((MouseEvent event) -> {
            if (scale < 10d) scale = scale + 0.3d; 
            setImg(IID);
            event.consume();
        });
        
        zoomOutBtn.setOnMouseClicked((MouseEvent event) -> {
            if (scale > 0.4) scale = scale - 0.3d; 
            setImg(IID);
            event.consume();
        });
        
        zoomFitToWinBtn.setOnMouseClicked((MouseEvent event) -> {
            scale = 0.95d;
            setImg(IID);
            event.consume();
        });
        
        toTempBtn.setOnMouseClicked((MouseEvent event) -> {
            DBWrapper.downloadImageToTempDir(IID); 
            event.consume();
        });
        
        sp.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            width = newValue.doubleValue();
            if ((height > 0) && (width > 0)) setImg(IID);
        });
        
        sp.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            height = newValue.doubleValue();
            if ((height > 0) && (width > 0)) setImg(IID);
        });
    }
    
    private void setImgOrig(long iid) {
        Image im = DBWrapper.getImage(iid);
        img_w = im.getWidth();
        img_h = im.getHeight();
        imgC.setImage(im); 
        imgC.setFitHeight(img_h);
        imgC.setFitWidth(img_w);
        scale = img_w / width;
    }
    
    private void setImg(long iid) {
        Image im = DBWrapper.getImage(iid);
        img_w = im.getWidth();
        img_h = im.getHeight();
        if ((img_w < width) && (img_h < height)) {
            imgC.setFitHeight(img_h);
            imgC.setFitWidth(img_w);
            scale = img_w / width;
        } else {
            imgC.setFitHeight(height * scale);
            imgC.setFitWidth(width * scale);
        }
        imgC.setImage(im);
    }
    
    private void _show(long iid) {
        IID = iid;
        AID = -1;

        isLiked = DBWrapper.isImageLiked(IID);
        likeBtn.setGraphic((isLiked) ? like : nolike);
        
        isDeleted = DBWrapper.isImageDeleted(IID);
        delBtn.setGraphic((isDeleted) ? del : nodel);
    }
    
    public void show(long iid) {
        _show(iid);
        dw.show();
    }
}
