package jnekoimagesdb.ui.md.filelist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import jiconfont.javafx.IconNode;
import jnekoimagesdb.core.img.XImg;

public class PagedFileListElementDirFile extends Pane {
    private final IconNode
            iconFile = new IconNode(),
            iconDir = new IconNode(),
            iconSel = new IconNode();

    private final Label
            imageName = new Label(),
            imageHolder = new Label(),
            imageSel = new Label();

    private int
            itemSizeH = 128, 
            itemSizeV = 128;

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

    public final void setNullImage() {
        currentPath = null;
        imageSel.setVisible(false);
        imageName.setText("");
        imageHolder.setVisible(false);
        this.getStyleClass().clear();
    }

    public final void setDir() {
        imageHolder.setVisible(true);
        imageHolder.setGraphic(iconDir);
        this.getStyleClass().addAll("pil_item_root_pane");    
    }
    
    public final void setFile() {
        imageHolder.setVisible(true);
        imageHolder.setGraphic(iconFile);
        this.getStyleClass().addAll("pil_item_root_pane");    
    }
    
    public final void setName(String fname) {
        imageName.setText((fname.length() > 21) ? 
            (fname.substring(0, 8) + "..." + fname.substring(fname.length()-8 , fname.length())) : fname);
    }

    public final void setSelected(boolean s) {
        if (!isDir) imageSel.setVisible(s);
    }

    public final boolean getSelected() {
        return imageSel.isVisible();
    }

    public final void setSize() {
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;

        itemSizeV = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
        itemSizeH = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();

        this.setMinSize(itemSizeH, itemSizeV);
        this.setMaxSize(itemSizeH, itemSizeV);
        this.setPrefSize(itemSizeH, itemSizeV);

        imageName.setMaxSize(itemSizeH, 16);
        imageName.setPrefSize(itemSizeH, 16);
        imageName.relocate(0, itemSizeV - 23);
        
        imageHolder.setMinSize(itemSizeH, itemSizeV);
        imageHolder.setMaxSize(itemSizeH, itemSizeV);
        imageHolder.setPrefSize(itemSizeH, itemSizeV);
        imageHolder.relocate(0, 0);
        
        imageSel.relocate(15, 25);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public PagedFileListElementDirFile(PagedFileListElementActionListener al) {
        super();
        actionListener = al;
        
        this.getStyleClass().addAll("pil_item_root_pane");
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

        imageName.setAlignment(Pos.CENTER);
        imageName.getStyleClass().addAll("pil_item_name_label");
        
        imageSel.setAlignment(Pos.CENTER);
        imageSel.getStyleClass().addAll("pil_item_null_pane", "pil_item_selected_icon_holder");
        iconSel.getStyleClass().addAll("pil_item_selected_icon");
        imageSel.setGraphic(iconSel);
        
        imageHolder.setAlignment(Pos.CENTER);
        imageHolder.getStyleClass().addAll("pil_item_null_pane");
        
        iconFile.getStyleClass().addAll("pil_folder_item_icon", "pil_folder_item_icon_file");
        iconDir.getStyleClass().addAll("pil_folder_item_icon", "pil_folder_item_icon_dir");

        imageSel.setVisible(false);

        this.getChildren().addAll(imageHolder, imageSel, imageName);
        
        setSize();
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
