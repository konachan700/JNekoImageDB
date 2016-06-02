package jnekoimagesdb.ui.md.images;

import java.io.File;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.ui.controls.dialogs.DialogAlbumSelect;
import jnekoimagesdb.ui.controls.PagedImageList;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;

public class ImagesList extends VBox  {
    public final static String
            CSS_FILE = new File("./style/style-gmd-tab-all-images.css").toURI().toString();
    
    public static enum FilterType {
        all, nottags, notinalbums
    }
    
    private ImagesListActionListener
            backActionListener;
    
    private boolean 
            isNotInit = true;
    
    private final PagedImageList
            pil = XImg.getPagedImageList();
    
    private final TopPanel
            panelTop;
    
    private final DialogAlbumSelect
            dis = new DialogAlbumSelect();
    
    private final TopPanelMenuButton 
            menuBtn = new TopPanelMenuButton();
    
    private final TopPanelButton
            backToAlbums;
    
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images");
    
    public void setActionListener(ImagesListActionListener al) {
        backActionListener = al;
    }
    
    public ImagesList() {
        super();
        
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("tai_null_pane", "tai_max_width", "tai_max_height");
        this.getChildren().add(pil);
        
        backToAlbums = new TopPanelButton("panel_icon_tags_one_level_up", c -> {
                    if (backActionListener != null) backActionListener.OnBackToAlbumsClick(pil.getAlbumID()); 
                });
        backToAlbums.setVisible(false);
        
        panelTop = new TopPanel(); 
        panelTop.addNode(infoBox);
        panelTop.addNode(backToAlbums);
        panelTop.addNode(
                new TopPanelButton("panel_icon_upload_to_temp", c -> {
                    pil.uploadSelected();
                })
        );

        menuBtn.addMenuItem("Сбросить выделение", (c) -> {
            pil.selectNone();
        });
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Добавить выделенное в альбомы...", (c) -> {
            if (pil.getSelectedHashes().size() > 0) {
                dis.refresh();
                dis.showModal();
                if (dis.isRetCodeOK()) {
                    final ArrayList<DSAlbum> sl = dis.getSelected();
                    pil.addToAlbums(sl);
                    pil.selectNone();
                    dis.clearSelected();
                }
            }
        });
        menuBtn.addMenuItem("Добавить теги для выделенного...", (c) -> {
            
        });
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Импорт изображений с диска...", (c) -> {
            XImg.getUploadBox().setAlbumID(pil.getAlbumID());
            XImg.getUploadBox().showModal();
        });
        menuBtn.addMenuItem("Сохранить выделенное на диск...", (c) -> {
            XImg.openDir().showDialog();
        });
        
        panelTop.addNode(menuBtn);
    }
    
    public void setAlbumID(long id) {
        pil.setAlbumID(id);
        if (!this.getChildren().contains(pil)) this.getChildren().add(pil);
    }
    
    public void refresh() {
        backToAlbums.setVisible(pil.getAlbumID() > 0);
        pil.refresh();
        infoBox.setTitle(pil.getGroupTitle());
        infoBox.setText("Всего картинок: "+pil.getCurrentCount());
    }
    
    public void clearTags() {
        pil.clearTags();
    }
    
    public void setTagLists(List<DSTag> tags, List<DSTag> tagsNot) {
        pil.setTagLists(tags, tagsNot);
    }
    
    public void regenerate() {
        if (isNotInit) {
            dis.dbInit();
            isNotInit = false;
        }
    }
    
    public Parent getPaginator() {
        return pil.getPaginator();
    }
    
    public Parent getPanel() {
        return panelTop;
    }
}
