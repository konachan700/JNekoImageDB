package jnekoimagesdb.ui.md.filelist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.ui.GUITools;

public class PagedFileListElement extends Pane {
    private final ImageView 
        imageContainer = new ImageView(),
        selectedIcon = new ImageView(GUITools.loadIcon("selected-32"));
    
    private final VBox 
        imageVBox = new VBox(0);

    private final Label
        imageName = new Label();

    private int
            itemSizeH = PagedFileList.FIXED_SIZE, 
            itemSizeV = PagedFileList.FIXED_SIZE;
    
    private boolean 
            fixedSize = false;

    private Path
            currentPath = null;

    private boolean
            isDir = false;

    private final PagedFileListElementActionListener
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
    
    public final void setFixedSize(int x, int y) {
        fixedSize = true;
        itemSizeV = x;
        itemSizeH = y;
    }

    public final void setNullImage() {
        imageContainer.setImage(GUITools.loadIcon("dummy-128"));
        imageContainer.setVisible(false);
        this.getChildren().clear();
        this.getStyleClass().clear();
//        this.setStyle("-fx-background-color: #f00;");//test only
    }

    public final void setDir() {
        final IconNode iconFolder = new IconNode();
        iconFolder.getStyleClass().addAll("pil_folder_item_icon");
        
        this.getChildren().clear();
        
        this.getChildren().addAll(iconFolder, selectedIcon, imageName);
        iconFolder.relocate(0, 0);
        selectedIcon.relocate(10, 10);
        imageName.relocate(0, itemSizeV - 23);
        
        this.getStyleClass().clear();
        this.getStyleClass().addAll("pil_item_root_pane");
    }
    
    public final void setImage(Image img) {
      //  final double centerX = (imageContainer.getViewport().getWidth() - img.getWidth()) / 2;
      //  imageContainer.setX((centerX <= 0) ? 0 : centerX);
        imageContainer.setImage(img);
        imageContainer.setVisible(true);
        if (this.getChildren().isEmpty()) addAll();
        
        this.getStyleClass().clear();
        this.getStyleClass().addAll("pil_item_root_pane");
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
        if (!fixedSize) {
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
            itemSizeV = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
            itemSizeH = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
        }
       

        this.setMaxSize(itemSizeH, itemSizeV);
        this.setPrefSize(itemSizeH, itemSizeV);
        super.setMinSize(itemSizeH, itemSizeV);
        
//        imageContainer.setFitHeight(itemSizeV);
//        imageContainer.setFitWidth(itemSizeH);
        
        imageVBox.setMaxSize(itemSizeH, itemSizeV);
        imageVBox.setPrefSize(itemSizeH, itemSizeV);
        
        imageName.setMaxSize(itemSizeH, 16);
        imageName.setPrefSize(itemSizeH, 16);
        imageName.relocate(0, itemSizeV - 23);
    }

    public PagedFileListElement(PagedFileListElementActionListener al, boolean itemFixedSize) {
        super();

        fixedSize = itemFixedSize;
        actionListener = al;
        
        this.getStyleClass().addAll("pil_item_root_pane");

        super.setMaxSize(itemSizeH, itemSizeV);
        super.setPrefSize(itemSizeH, itemSizeV);
        super.setMinSize(itemSizeH, itemSizeV);

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
        imageContainer.setImage(GUITools.loadIcon("dummy-128"));

        imageVBox.getChildren().add(imageContainer);
        imageVBox.setAlignment(Pos.CENTER);
        imageVBox.getStyleClass().addAll("pil_item_null_pane");

        imageName.getStyleClass().addAll("pil_item_name_label");
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
        
    @Override
    public boolean equals(Object o) {
        if ((o == null) || (currentPath == null)) return false;
        if (o instanceof Path) {
            try {
                return Files.isSameFile(((Path) o), currentPath);
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.currentPath);
        return hash;
    }
}
