package imagelist;

import dataaccess.ImageEngine;
import fsimagelist.FSImageListItem;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ImageListItem extends Pane {
    //private long ID = 0;
    private Long ID = (long) 0x0;
    private final ImageListItem THIS = this;
    private ImageListItemActionListener AL = null;
    
    private ImageEngine 
            IM = null;

    private final Image
            empty = new Image(new File("./icons/empty.png").toURI().toString()),
            likeTrue = new Image(new File("./icons/like24.png").toURI().toString()),
            likeFalse = new Image(new File("./icons/nolike24.png").toURI().toString()),
            delTrue = new Image(new File("./icons/delete24a.png").toURI().toString()),
            delFalse = new Image(new File("./icons/delete24.png").toURI().toString()),
            selected = new Image(new File("./icons/selected.png").toURI().toString());
    
    private final ImageView 
            imageZ = new ImageView(), 
            likeImage = new ImageView(likeFalse),
            openImage = new ImageView(new Image(new File("./icons/open24.png").toURI().toString())),
            totempImage = new ImageView(new Image(new File("./icons/totemp24.png").toURI().toString())),
            delImage = new ImageView(delFalse),
            selImg = new ImageView(selected);
    
    private final VBox 
            buttonsVBox = new VBox(8),
            imgBox = new VBox(0);
    
    final int 
            PADDING = 3;
    
    private boolean
            isSel = false,
            isLiked = false, 
            isDeleted = false;
    
    public ImageListItem(ImageEngine im, ImageListItemActionListener a) {
        super();
        IM = im;
        AL = a;
        init();
    }
    
    public boolean isSelected() {
        return isSel;
    }
    
    public void setSelected(boolean s) {
        isSel = s;
        selImg.setVisible(isSel);
    }
    
    public void clearIt() {
        isSel = false;
        selImg.setVisible(isSel);
        this.getStyleClass().clear();
        imageZ.setImage(empty);
    }

    private void init() {
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("ImageListX");
        this.setMinSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        this.setMaxSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        this.setPrefSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        
        this.setOnMouseEntered((MouseEvent event) -> {
            isDeleted = IM.getWr().isImageDeleted(ID);
            delImage.setImage((isDeleted) ? delTrue : delFalse);
            
            isLiked = IM.getWr().isImageLiked(ID);
            likeImage.setImage((isLiked) ? likeTrue : likeFalse);
        
            buttonsVBox.setVisible(true);
        });
        
        this.setOnMouseExited((MouseEvent event) -> {
            buttonsVBox.setVisible(false);
        });
        
        this.setOnMouseClicked((MouseEvent event) -> {
            isSel = !isSel;
            selImg.setVisible(isSel);
            if (AL != null) AL.OnClick(this);
        }); 

        imageZ.setFitHeight(ImageEngine.SMALL_PREVIEW_SIZE);
        imageZ.setFitWidth(ImageEngine.SMALL_PREVIEW_SIZE);
        imageZ.setPreserveRatio(true);
        imageZ.setSmooth(true);
        imageZ.setCache(false);
        
        imgBox.setMaxSize(128, 128);
        imgBox.setMinSize(128, 128);
        imgBox.setPrefSize(128, 128);
        imgBox.setAlignment(Pos.TOP_CENTER);
        imgBox.getChildren().add(imageZ);
        
        buttonsVBox.setMaxSize(128, 128);
        buttonsVBox.setPrefSize(128, 128);
        buttonsVBox.setAlignment(Pos.TOP_CENTER);
        buttonsVBox.setPadding(new Insets(24,0,0,4));
        buttonsVBox.setVisible(false);
        selImg.setVisible(false);
        likeImage.setOpacity(0.75d);
        openImage.setOpacity(0.75d);
        totempImage.setOpacity(0.75d);
        selImg.setOpacity(0.75d);
        delImage.setOpacity(0.75d);
        
        likeImage.setOnMouseClicked((MouseEvent event) -> {
            isLiked = !isLiked;
            IM.getWr().setImageLiked(ID, isLiked);
            likeImage.setImage((isLiked) ? likeTrue : likeFalse);
            event.consume();
        });
        
        delImage.setOnMouseClicked((MouseEvent event) -> {
            isDeleted = !isDeleted;
            IM.getWr().setImageDeleted(ID, isDeleted);
            delImage.setImage((isDeleted) ? delTrue : delFalse);
            event.consume();
        });
        
        HBox iconsI = new HBox(4);
        iconsI.setAlignment(Pos.CENTER);
        iconsI.getChildren().addAll(totempImage, openImage, delImage, likeImage);
        buttonsVBox.getChildren().addAll(iconsI);
        
        this.setPrefSize(128, 128);
        this.getChildren().add(imgBox);
        this.getChildren().add(buttonsVBox);
        this.getChildren().add(selImg);
        
        imgBox.relocate(3, 3);
        buttonsVBox.relocate(0, 80);
        selImg.relocate(10, 10);
    }
    
    public void setNonSquaredSmallImage(ImageEngine fs, long iid) {
        BufferedImage img = fs.getImages(iid).getNSSmallPrerview();
        setImg(img.getWidth(null), img.getHeight(null), img);
    }
    
    public void setSmallImage(ImageEngine fs, long iid) {
        BufferedImage img = fs.getImages(iid).getSmallPrerview();
        setImg(ImageEngine.SMALL_PREVIEW_SIZE, ImageEngine.SMALL_PREVIEW_SIZE, img);
    }

    public void setImg(double sizeW, double sizeH, BufferedImage img) {
        if (img == null) return;
        final WritableImage img_r = SwingFXUtils.toFXImage(img, null);       
        imageZ.setFitHeight(sizeW);
        imageZ.setFitWidth(sizeH);
        imageZ.setSmooth(true);
        imageZ.setCache(true);
        imageZ.setPreserveRatio(true);
        imageZ.setImage(img_r);
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("ImageListX");
    }

    public void setID(Long i) {
        ID = i; //new Long(i);
    }
    
    public long getID() {
        return ID;
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
