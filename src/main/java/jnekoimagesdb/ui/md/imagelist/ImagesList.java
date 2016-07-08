package jnekoimagesdb.ui.md.imagelist;

import java.nio.file.Path;
import jnekoimagesdb.domain.DSAlbum;
import java.util.List;
import java.util.Set;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.md.albums.AlbumsSelectDialog;
import jnekoimagesdb.ui.md.dialogs.MessageBox;
import jnekoimagesdb.ui.md.dialogs.fs.OpenDirectoryDialog;
import jnekoimagesdb.ui.md.dialogs.fs.OpenSaveFileDialog;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;

public class ImagesList extends VBox  {
    public static enum FilterType {
        all, nottags, notinalbums
    }
    
    private static ImagesList
            sImgList = null;
    
    private ImagesListActionListener
            backActionListener;
    
//    private boolean 
//            isNotInit = true;

    private final TopPanel
            panelTop;
    
    private final TopPanelMenuButton 
            menuBtn = new TopPanelMenuButton();
    
    private final TopPanelButton
            backToAlbums;
    
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images");
    
    public void setActionListener(ImagesListActionListener al) {
        backActionListener = al;
    }
    
    private ImagesList() {
        super();
        
        this.getStyleClass().addAll("tai_null_pane", "tai_max_width", "tai_max_height");
        this.getChildren().add(PagedImageList.get());
        
        backToAlbums = new TopPanelButton("panel_icon_tags_one_level_up", "На один уровень вверх", c -> {
                    if (backActionListener != null) backActionListener.OnBackToAlbumsClick(PagedImageList.get().getAlbumID()); 
                });
        backToAlbums.setVisible(false);
        
        panelTop = new TopPanel(); 
        panelTop.addNode(infoBox);
        panelTop.addNode(backToAlbums);
        panelTop.addNode(
                new TopPanelButton("panel_icon_upload_to_temp", "Сохранить в папку обмена", c -> {
                    PagedImageList.get().uploadSelected();
                })
        );

        menuBtn.addMenuItemBold("menuitem_star_icon", "Последние добавленные картинки", (c) -> {
            this.setImageType(DSImageIDListCache.ImgType.All);
            refresh();
        });
        menuBtn.addMenuItemBold("menuitem_star_icon", "Все картинки без тегов", (c) -> {
            this.setImageType(DSImageIDListCache.ImgType.Notagged);
            refresh();
        });
        
        menuBtn.addMenuItemBold("menuitem_star_icon", "Все картинки не в альбомах", (c) -> {
            this.setImageType(DSImageIDListCache.ImgType.NotInAnyAlbum);
            refresh();
        });
        
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Сбросить выделение", (c) -> {
            PagedImageList.get().selectNone();
        });
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Добавить выделенное в альбомы...", (c) -> {
            if (PagedImageList.get().getSelectedHashes().size() > 0) {
                AlbumsSelectDialog.getDialog().showAndWait();
                final Set<DSAlbum> sl = AlbumsSelectDialog.getDialog().getSelectedAlbums();
                if (sl.isEmpty()) return;
                PagedImageList.get().addToAlbums(sl);
                PagedImageList.get().selectNone();
                AlbumsSelectDialog.getDialog().getSelectedAlbums().clear();
            } else
                MessageBox.show("Ни одной картинки не выделено!");
        });
        menuBtn.addMenuItem("Добавить теги для выделенного...", (c) -> {
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        });
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Импорт изображений с диска...", (c) -> {
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//            XImg.getUploadBox().setAlbumID(PagedImageList.get().getAlbumID());
//            XImg.getUploadBox().showModal();
        });
        menuBtn.addMenuItem("Сохранить выделенное на диск...", (c) -> {
            OpenSaveFileDialog.showOpenDialog();
            if (OpenSaveFileDialog.isResultPresent()) {
                final Path p = OpenSaveFileDialog.getPath();
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                
            }
        });
        
        panelTop.addNode(menuBtn);
    }
    
    public final void setImageType(DSImageIDListCache.ImgType imgt) {
        PagedImageList.get().setImageType(imgt, 0);
        if (!this.getChildren().contains(PagedImageList.get())) this.getChildren().add(PagedImageList.get());
    }
    
    public final void setImageType(DSImageIDListCache.ImgType imgt, long albumID) {
        PagedImageList.get().setImageType(imgt, albumID);
        if (!this.getChildren().contains(PagedImageList.get())) this.getChildren().add(PagedImageList.get());
    }
    
    public final void refresh() {
        backToAlbums.setVisible(PagedImageList.get().getAlbumID() > 0);
        PagedImageList.get().refresh();
        infoBox.setTitle(PagedImageList.get().getGroupTitle());
        infoBox.setText("Всего картинок: "+PagedImageList.get().getTotalImagesCount());
    }
    
    public void clearTags() {
        PagedImageList.get().clearTags();
    }
    
    public void setTagLists(List<DSTag> tags, List<DSTag> tagsNot) {
        PagedImageList.get().setTagLists(tags, tagsNot);
    }
    
//    public void regenerate() {
//        if (isNotInit) {
//            isNotInit = false;
//        }
//    }
//    
    public Parent getPaginator() {
        return PagedImageList.get().getPaginator();
    }
    
    public Parent getPanel() {
        return panelTop;
    }
    
    public static ImagesList get() {
        if (sImgList == null) sImgList = new ImagesList();
        return sImgList;
    }
}
