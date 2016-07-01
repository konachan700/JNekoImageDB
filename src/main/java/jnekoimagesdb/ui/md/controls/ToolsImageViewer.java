package jnekoimagesdb.ui.md.controls;

import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public abstract class ToolsImageViewer extends ScrollPane {
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

    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    public ToolsImageViewer() {
        super();

        this.getStyleClass().addAll("iv_root_pane", "max_width", "max_height");
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setFitToHeight(true);
        this.setFitToWidth(true);

        
        final BorderPane bp = new BorderPane();
        this.getStyleClass().addAll("null_pane", "max_width", "max_height");
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
            if (key.getCode() == KeyCode.LEFT)  PrevKey();
            if (key.getCode() == KeyCode.RIGHT) NextKey();
            if (key.getCode() == KeyCode.SPACE) NextKey();
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
    
    public abstract void PrevKey();
    public abstract void NextKey();
}
