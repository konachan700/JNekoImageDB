package imgfsgui;

import dataaccess.Lang;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import jnekoimagesdb.GUITools;

public class ToolsImageViewer extends ScrollPane {
    public static interface ToolsImageViewerActionListener {
        public void PrevKey();
        public void NextKey();
    }
    
    private volatile double 
            scale = 0.95d,
            height = 0,
            width = 0,
            last_x = 0,
            last_y = 0,
            img_h = 0,
            img_w = 0;
    
    private final ImageView
            imgContainer = new ImageView();
    
    private final ToolsImageViewerActionListener
            actListener;
    
    private Image
            currImg = null;
    
    public final void zoomIn() {
        if (scale < 10d) scale = scale + 0.3d; 
        setImg(currImg);
    }
    
    public final void zoomOut() {
        if (scale > 0.4) scale = scale - 0.3d; 
        setImg(currImg);
    }
    
    public final void zoomOrig() {
        setImgOrig(currImg);
    }
    
    public final void zoomFitToWin() {
        scale = 0.95d;
        setImg(currImg);
    }
    
    public final void setImage(Image im) {
        currImg = im;
        zoomFitToWin();
    }

    public ToolsImageViewer(ToolsImageViewerActionListener al) {
        super();
        actListener = al;
        
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("ToolsImageViewer_root_pane");
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        GUITools.setMaxSize(this, 9999, 9999);
        
        final BorderPane bp = new BorderPane();
        GUITools.setMaxSize(bp, 9999, 9999);
        bp.setCenter(imgContainer); 
        
        this.setContent(bp);
        
        imgContainer.setSmooth(true);
        imgContainer.setCache(false);
        imgContainer.setPreserveRatio(true);
        imgContainer.setCursor(Cursor.OPEN_HAND);
        
        imgContainer.setOnMouseReleased((MouseEvent t) -> {
            last_x = 0;
            last_y = 0;
        });
        
        this.setFocusTraversable(true);
        this.setOnKeyPressed((KeyEvent key) -> {
            if (key.getCode() == KeyCode.LEFT)  actListener.PrevKey();
            if (key.getCode() == KeyCode.RIGHT) actListener.NextKey();
            if (key.getCode() == KeyCode.SPACE) actListener.NextKey();
            if (key.getCode() == KeyCode.PLUS) zoomIn();
            if (key.getCode() == KeyCode.MINUS) zoomOut();
        });

        imgContainer.setOnMouseDragged((MouseEvent t) -> {
            if ((height < imgContainer.getFitHeight()) || (width < imgContainer.getFitWidth())) {
                if ((last_x > 0) && (last_y > 0)) {
                    this.setHvalue(this.getHvalue() + ((last_x - t.getSceneX()) / (width / 1.2d)));
                    this.setVvalue(this.getVvalue() + ((last_y - t.getSceneY()) / (height / 1.2d)));
                }
                
                last_x = t.getSceneX();
                last_y = t.getSceneY();
            }
        });
        
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            width = newValue.doubleValue();
            if ((height > 0) && (width > 0)) setImg(currImg);
        });
        
        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            height = newValue.doubleValue();
            if ((height > 0) && (width > 0)) setImg(currImg);
        });
    }
    
    private void setImgOrig(Image im) {
        img_w = im.getWidth();
        img_h = im.getHeight();
        imgContainer.setImage(im); 
        imgContainer.setFitHeight(img_h);
        imgContainer.setFitWidth(img_w);
        scale = img_w / width;
    }
    
    private void setImg(Image im) {
        img_w = im.getWidth();
        img_h = im.getHeight();
        if ((img_w < width) && (img_h < height)) {
            imgContainer.setFitHeight(img_h);
            imgContainer.setFitWidth(img_w);
            scale = img_w / width;
        } else {
            imgContainer.setFitHeight(height * scale);
            imgContainer.setFitWidth(width * scale);
        }
        imgContainer.setImage(im);
    }
}
