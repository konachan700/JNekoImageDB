package imagelist;

import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.Lang;
import java.io.ByteArrayInputStream;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Deprecated
public class ImageListItem extends Pane {
    private Long ID = (long) 0x0;
    private ImageListItemActionListener AL = null;
    
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
    
    public ImageListItem(ImageListItemActionListener a) {
        super();
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
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("ImageListItem_root_pane");
        this.setMinSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        this.setMaxSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        this.setPrefSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        
        this.setOnMouseEntered((MouseEvent event) -> {
            isDeleted = DBWrapper.isImageDeleted(ID);
            delImage.setImage((isDeleted) ? delTrue : delFalse);
            
            isLiked = DBWrapper.isImageLiked(ID);
            likeImage.setImage((isLiked) ? likeTrue : likeFalse);
        
            buttonsVBox.setVisible(true);
        });
        
        this.setOnMouseExited((MouseEvent event) -> {
            buttonsVBox.setVisible(false);
        });
        
        this.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    isSel = !isSel;
                    selImg.setVisible(isSel);
                    if (AL != null) AL.OnClick(this);
                }
            } else {
                if (event.getButton() == MouseButton.PRIMARY) new ImageListViewScreen().show(ID);
            }
            event.consume();
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
        
        openImage.setOnMouseClicked((MouseEvent event) -> {
            new ImageListViewScreen().show(ID);
            event.consume();
        });
        
        likeImage.setOnMouseClicked((MouseEvent event) -> {
            isLiked = !isLiked;
            DBWrapper.setImageLiked(ID, isLiked);
            likeImage.setImage((isLiked) ? likeTrue : likeFalse);
            event.consume();
        });
        
        delImage.setOnMouseClicked((MouseEvent event) -> {
            isDeleted = !isDeleted;
            DBWrapper.setImageDeleted(ID, isDeleted);
            delImage.setImage((isDeleted) ? delTrue : delFalse);
            event.consume();
        });
        
        totempImage.setOnMouseClicked((MouseEvent event) -> {
            DBWrapper.downloadImageToTempDir(ID);
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
        imgBox.setAlignment(Pos.CENTER);
        buttonsVBox.relocate(0, 80);
        selImg.relocate(10, 10);
    }

    public void setImg(double sizeW, double sizeH, byte img[]) {
        this.getStyleClass().clear();
        imageZ.setSmooth(true);
        imageZ.setCache(false);
        imageZ.setPreserveRatio(true);
        imageZ.setImage(new Image(new ByteArrayInputStream(img)));
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("ImageListItem_root_pane");
    }
    
    public void setImg(double sizeW, double sizeH, Image img) {
        this.getStyleClass().clear();
        imageZ.setSmooth(true);
        imageZ.setCache(false);
        imageZ.setPreserveRatio(true);
        imageZ.setImage(img);
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("ImageListItem_root_pane");
    }

    public void setID(Long i) {
        ID = i;
    }
    
    public long getID() {
        return ID;
    }
}
