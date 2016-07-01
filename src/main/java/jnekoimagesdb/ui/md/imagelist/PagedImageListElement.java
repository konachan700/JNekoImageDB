package jnekoimagesdb.ui.md.imagelist;

import java.io.IOException;
import java.util.Arrays;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.core.img.XImgPreviewGen;
import jnekoimagesdb.core.threads.UPools;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.ui.GUITools;

public class PagedImageListElement extends Pane {
    private final ImageView 
            imageContainer = new ImageView();

    private final IconNode
            selectedIcon; 

    private DSImage
            img = null;

    public int 
            myNumber = 0;

    private final VBox 
            imageVBox = new VBox(0);

    private final PagedImageListElementActionListener
            actionListener;

    public byte[] getMD5() {
        return img.getMD5();
    }

    public void setMD5(byte[] b) {
        img.setMD5(b);
    }

    public DSImage get() {
        return img;
    }

    public final void setNullImage() {
        img = null;
        imageContainer.setImage(GUITools.loadIcon("dummy-128"));
        imageContainer.setVisible(false);
        this.getChildren().clear();
        this.getStyleClass().clear();
    }

    public final void setImage(Image img) {
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        imageContainer.setFitHeight(XImg.getPSizes().getPrimaryPreviewSize().getHeight());
        imageContainer.setFitWidth(XImg.getPSizes().getPrimaryPreviewSize().getWidth());

        imageContainer.setImage(img);
        imageContainer.setVisible(true);

        if (this.getChildren().isEmpty()) addAll();
        this.getStyleClass().clear();
        this.getStyleClass().addAll("pil_item_root_pane");
    }

    public final void setImage(DSImage dsi) {
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        imageContainer.setFitHeight(XImg.getPSizes().getPrimaryPreviewSize().getHeight());
        imageContainer.setFitWidth(XImg.getPSizes().getPrimaryPreviewSize().getWidth());

        img = dsi; 
        XImgPreviewGen.PreviewElement peDB;
        try {
                peDB = XImgDatastore.readPreviewEntry(dsi.getMD5());
                final Image im = peDB.getImage(XImg.getCrypt(), XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
                if (im != null) {
                    imageContainer.setImage(im);
                    imageContainer.setVisible(true);
                } else 
                    throw new IOException();
        } catch (IOException | ClassNotFoundException ex) {
            actionListener.OnError(img); 
            imageContainer.setImage(GUITools.loadIcon("loading-128"));
            imageContainer.setVisible(true);
            UPools.getGroup(UPools.PREVIEW_POOL).resume();
        }

        if (this.getChildren().isEmpty()) addAll();
        this.getStyleClass().addAll("pil_item_root_pane");
    }

    public final void setSelected(boolean s) {
        if (imageContainer.isVisible()) selectedIcon.setVisible(s);
    }

    public final boolean getSelected() {
        return selectedIcon.isVisible();
    }

    public void setSizes(long x, long y) {
        imageVBox.setMaxSize(x, y);
        imageVBox.setPrefSize(x, y);
        this.setMaxSize(x, y);
        this.setPrefSize(x, y);
        imageContainer.setFitHeight(y);
        imageContainer.setFitWidth(x);
    }

    public PagedImageListElement(int itemSizeX, int itemSizeY, PagedImageListElementActionListener al) {
        super();
        actionListener = al;

        selectedIcon = new IconNode();
        selectedIcon.getStyleClass().add("pil_selected_item_icon");

        this.getStyleClass().addAll("pil_item_root_pane");
        this.setMaxSize(itemSizeX, itemSizeY);
        this.setPrefSize(itemSizeX, itemSizeY);

        this.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    setSelected(!getSelected());
                    actionListener.OnSelect(getSelected(), img);
                }
            } else {
                actionListener.OnOpen(img, this);
            }
            event.consume();
        });

        imageContainer.setPreserveRatio(true);
        imageContainer.setSmooth(true);
        imageContainer.setCache(false);
        imageContainer.setImage(GUITools.loadIcon("dummy-128"));

        imageVBox.setAlignment(Pos.CENTER);    
        imageVBox.getStyleClass().addAll("pil_item_null_pane");
        imageVBox.getChildren().add(imageContainer);

        selectedIcon.setVisible(false);

        addAll();
    }

    private void addAll() {
        this.getChildren().addAll(imageVBox, selectedIcon);
        imageVBox.relocate(0, 0);
        selectedIcon.relocate(10, 10);
    }

    public boolean equals(DSImage o) {
        if ((o == null) || (img == null)) return false;
        return Arrays.equals(((DSImage) o).getMD5(), img.getMD5());
    }
}
