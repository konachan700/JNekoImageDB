package fsimagelist;

import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.Lang;
import java.io.File;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class FSImageListItem extends Pane {
    private final FSImageListItem THIS = this;

    private final Image
            empty = new Image(new File("./icons/empty.png").toURI().toString()),
            selected = new Image(new File("./icons/selected.png").toURI().toString()),
            transparent = new Image(new File("./icons/transparent.png").toURI().toString());
    
    private final ImageView 
            imageZ = new ImageView(), 
            selImg = new ImageView(selected);
    
    private final VBox 
            buttonsVBox = new VBox(8),
            imgBox = new VBox(0);
    
    private final Label
            imageName = new Label();
    
    private long 
            ID = 0;

    final int 
            PADDING = 3;
    
    final DropShadow 
            ds = new DropShadow();
    
    private File 
            myFile = null;
    
    private FSImageListActionListener 
            FLAL = null;
    
    private boolean
            isSelected = false;
    
    @SuppressWarnings("Convert2Lambda")
    private final EventHandler<MouseEvent> onMouseEvent = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (imageName.getText().length() <= 1) return;
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                if (event.getClickCount() == 2) {
                    if (FLAL != null) 
//                        if (myFile.isDirectory()) {
//                            FLAL.OnDblFolderClick(THIS, myFile);
//                        } else {
                            FLAL.OnDblImageClick(THIS, myFile);
//                        }
                    return;
                }
                
                if (event.getClickCount() == 1) {
                    if (!myFile.isDirectory()) {
                        isSelected = !isSelected;
                        selImg.setVisible(isSelected);
                        ds.setColor(Color.color(((isSelected) ? 0.70f : 0.99f), 0.99f, ((isSelected) ? 0.70f : 0.99f)));
                        if (FLAL != null) FLAL.OnClick(THIS);
                    } else {
                        FLAL.OnDblFolderClick(THIS, myFile);
                    }
                }
            }
        }
    };
    
    public final boolean IsSelected() {
        return isSelected;
    }
    
    public final void setSelected(boolean sel) {
        isSelected = sel;
        selImg.setVisible(isSelected); 
        ds.setColor(Color.color(((isSelected) ? 0.70f : 0.99f), 0.99f, ((isSelected) ? 0.70f : 0.99f)));
    }
    
    public void setRed(boolean b) {
        if (b) ds.setColor(Color.color(0.99f, 0.44f, 0.44f)); else ds.setColor(Color.color(0.99f, 0.99f, 0.99f));
    }
    
    public final void setInitInfo(File f) {
        myFile = f;
        final String fname = f.getName().trim();
        
        this.getStylesheets().clear();
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().clear();
        this.getStyleClass().add("FSImageListItem_root_pane");
        this.applyCss();
        
        selImg.setVisible(false);
        isSelected = false;
        //if (DBWrapper.isMD5InMetadata(f.getAbsolutePath())) ds.setColor(Color.color(0.99f, 0.44f, 0.44f)); else 
        ds.setColor(Color.color(0.99f, 0.99f, 0.99f));
        
        final Image im = new Image("file:///"+f.getAbsolutePath().replace("\\", "/"));
        imageZ.setImage(im);

        imageName.setText((fname.length() > 21) ? 
                (fname.substring(0, 8) + "..." + fname.substring(fname.length()-8 , fname.length())) : fname); 

        __a001();
    }
    
    private void __a001() {
        this.setPrefSize(128, 128);
        this.getChildren().clear();
        this.getChildren().add(imgBox);
        this.getChildren().add(imageName);
        this.getChildren().add(buttonsVBox);
        
        imgBox.relocate(3, 3);
        imageName.relocate(0, 116);
        buttonsVBox.relocate(0, 0);
    }
    
    public final void setActionListener(FSImageListActionListener a) {
        FLAL = a;
    }
    
    public final void setInitInfo(Image im, File f, boolean isFolder) {
        myFile = f;
        final String fname = f.getName().trim();
        this.getStylesheets().clear();
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().clear();
        this.getStyleClass().add((isFolder) ? "FSImageListItem_root_pane_folder" : "FSImageListItem_root_pane");
        this.applyCss();
        
        selImg.setVisible(false);
        isSelected = false;
        //if (DBWrapper.isMD5InMetadata(f.getAbsolutePath())) ds.setColor(Color.color(0.99f, 0.44f, 0.44f)); else 
        ds.setColor(Color.color(0.99f, 0.99f, 0.99f));
        
        imageZ.setImage(im);
        if (fname.length() >= 1)
            imageName.setText((fname.length() > 21) ? 
                    (fname.substring(0, 8) + "..." + fname.substring(fname.length()-8 , fname.length())) : fname);
        else 
            imageName.setText(f.getAbsolutePath());

        __a001();
    }
    
    public void clearIt() {
        imageName.setText(Lang.NullString);
        imageZ.setImage(empty);
    }
    
    public void hideIt() {
        imageName.setText(Lang.NullString);
        imageZ.setImage(transparent);
        this.getStyleClass().clear();
        this.getStyleClass().add("FSImageListItem_root_pane_folder");
    }
    
    public FSImageListItem() {
        super();
        _createComponents();
        imageZ.setImage(empty);
    }
    
    public FSImageListItem(Image im, File f, boolean isFolder) {
        super();
        _createComponents();
        setInitInfo(im, f, isFolder);
    }
    
    public FSImageListItem(File f) {
        super();
        _createComponents();
        setInitInfo(f);
    }
    
    private void _createComponents() {       
        this.setMinSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        this.setMaxSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        this.setPrefSize(ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2), ImageEngine.SMALL_PREVIEW_SIZE+(PADDING*2));
        
        imageZ.setFitHeight(ImageEngine.SMALL_PREVIEW_SIZE);
        imageZ.setFitWidth(ImageEngine.SMALL_PREVIEW_SIZE);
        imageZ.setPreserveRatio(true);
        imageZ.setSmooth(true);
        imageZ.setCache(true);
        
        imgBox.setMaxSize(128, 128);
        imgBox.setMinSize(128, 128);
        imgBox.setPrefSize(128, 128);
        imgBox.setAlignment(Pos.TOP_CENTER);
        imgBox.getChildren().add(imageZ);
        
        //imageName.getStyleClass().add("FSImageListItem_FSImageListLabel");
        imageName.setMaxSize(128, 16);
        imageName.setPrefSize(128, 16);
        imageName.setAlignment(Pos.CENTER);
        
        selImg.setFitHeight(32);
        selImg.setFitWidth(32);
        selImg.setPreserveRatio(true);

        ds.setOffsetY(0f);
        ds.setRadius(7f);
        ds.setSpread(0.8f);
        ds.setColor(Color.color(0.99f, 0.99f, 0.99f));
        imageName.setEffect(ds);
        
        selImg.setOpacity(0.75d);
        this.setOnMouseClicked(onMouseEvent);

        buttonsVBox.setMaxSize(128, 128);
        buttonsVBox.setPrefSize(128, 128);
        buttonsVBox.setAlignment(Pos.BOTTOM_CENTER);
        buttonsVBox.setPadding(new Insets(0,0,16,0));
        buttonsVBox.getChildren().add(selImg);
    }
    
    public void setID(long i) {
        ID = i;
    }
    
    public long getID() {
        return ID;
    }
    
    public File getFile() {
        return myFile;
    }
}
