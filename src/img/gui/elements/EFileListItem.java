package img.gui.elements;

import img.XImg;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

public class EFileListItem extends Pane {
        private final ImageView 
            imageContainer = new ImageView(),
            selectedIcon = new ImageView(GUIElements.ITEM_SELECTED);
        
        private final VBox 
            imageVBox = new VBox(0);
                
        private final Label
            imageName = new Label();
        
        private int
                itemSizeH = 128, 
                itemSizeV = 128;
        
        private Path
                currentPath = null;
        
        private boolean
                isDir = false;
        
        private final EFileListItemActionListener
                actionListener;
        
        public boolean isDirectory() {
            return isDir;
        }
        
        public final void setPath(Path p) {
            currentPath = p;
            if (p != null) isDir = Files.isDirectory(p); else isDir = false;
        }
        
        public final Path getPath() {
            return currentPath;
        }
        
        public final void setNullImage() {
            imageContainer.setImage(GUIElements.ITEM_NOTHING);
            imageContainer.setVisible(false);
            this.getChildren().clear();
            this.getStyleClass().clear();
        }
        
        public final void setImage(Image img) {
            imageContainer.setImage(img);
            imageContainer.setVisible(true);
            if (this.getChildren().isEmpty()) addAll();
            GUITools.setStyle(this, "FileListItem", "root_pane");
        }

        public final void setName(String fname) {
            imageName.setText((fname.length() > 21) ? 
                (fname.substring(0, 8) + "..." + fname.substring(fname.length()-8 , fname.length())) : fname);
        }
        
        public final void setSelected(boolean s) {
            if (imageContainer.isVisible() && (!isDir)) selectedIcon.setVisible(s);
        }
        
        public final boolean getSelected() {
            return selectedIcon.isVisible();
        }
        
        public final void setSize() {
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
            
            itemSizeV = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
            itemSizeH = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
            
            GUITools.setMaxSize(this, itemSizeH, itemSizeV);
            imageContainer.setFitHeight(itemSizeV);
            imageContainer.setFitWidth(itemSizeH);
            GUITools.setMaxSize(imageVBox, itemSizeH, itemSizeV);
            imageName.setMaxSize(itemSizeH, 16);
            imageName.setPrefSize(itemSizeH, 16);
            imageName.relocate(0, itemSizeV - 23);
        }
        
        @SuppressWarnings("LeakingThisInConstructor")
        public EFileListItem(EFileListItemActionListener al) {
            super();
            
            actionListener = al;
            
            GUITools.setStyle(this, "FileListItem", "root_pane");
            GUITools.setMaxSize(this, itemSizeH, itemSizeV);
            this.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 1) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        setSelected(!getSelected());
                        if (currentPath != null) actionListener.OnSelect(getSelected(), currentPath);
                    }
                } else {
                    if (currentPath != null) actionListener.OnOpen(currentPath);
                }
                event.consume();
            });

            imageContainer.setPreserveRatio(true);
            imageContainer.setSmooth(true);
            imageContainer.setCache(false);
//            imageContainer.setFitHeight(itemSizeV);
//            imageContainer.setFitWidth(itemSizeH);
            imageContainer.setImage(GUIElements.ITEM_NOTHING);
                        
            GUITools.setStyle(imageVBox, "FileListItem", "imageVBox");
//            GUITools.setMaxSize(imageVBox, itemSizeH, itemSizeV);
            imageVBox.getChildren().add(imageContainer);
            imageVBox.setAlignment(Pos.CENTER);
            
            GUITools.setStyle(imageName, "FileListItem", "imageName");
//            imageName.setMaxSize(itemSizeH, 16);
//            imageName.setPrefSize(itemSizeH, 16);
            imageName.setAlignment(Pos.CENTER);

            selectedIcon.setVisible(false);
            
            setSize();
            addAll();
        }
        
        private void addAll() {
            this.getChildren().addAll(imageVBox, selectedIcon, imageName);
            imageVBox.relocate(0, 0);
            selectedIcon.relocate(10, 10);
            imageName.relocate(0, itemSizeV - 23);
        }
}
