package jnekoimagesdb.ui.controls.tabs;

import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.ui.controls.dialogs.DialogAlbumSelect;
import jnekoimagesdb.ui.controls.PagedImageList;
import jnekoimagesdb.ui.controls.ToolsPanelTop;
import static jnekoimagesdb.ui.controls.dialogs.XImageUpload.IMG64_SELECT_NONE;
import static jnekoimagesdb.ui.controls.elements.GUIElements.BTN_SELNONE;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import java.util.ArrayList;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import jnekoimagesdb.ui.GUITools;

public class TabAllImages extends SEVBox  {
    public static final Image 
            IMG48_ADD_TAGS       = GUITools.loadIcon("add-tags-48"), 
            IMG48_ADD_NEW        = GUITools.loadIcon("todb-48"),
            IMG48_TO_ALBUM       = GUITools.loadIcon("add-to-album-48"),
            IMG48_TO_TEMP        = GUITools.loadIcon("addtotemp-48"); 
    
    public static final int
            BTN_ADD_TAGS    = 10,
            BTN_TO_ALBUM    = 11,
            BTN_TO_TEMP     = 12,
            BTN_ADD_NEW     = 13;
        
    public static enum FilterType {
        all, nottags, notinalbums
    }
    
    private boolean 
            isNotInit = true;
    
    private final PagedImageList
            pil = XImg.getPagedImageList();
    
    private final ToolsPanelTop 
            panelTop;
    
    private final DialogAlbumSelect
            dis = new DialogAlbumSelect();
    
    public TabAllImages() {
        super(0, 9999, 9999);
        this.getChildren().add(pil);
        
        panelTop = new ToolsPanelTop((index) -> {
            switch (index) {
                case BTN_SELNONE:
                    pil.selectNone();
                    break;
//                case TabAddImagesToDB.BTN_DEL:
//                    
//                    break;
                case BTN_TO_ALBUM:
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
                    break;
                case BTN_ADD_TAGS:
                    
                    break;
                case BTN_TO_TEMP:
                    pil.uploadSelected();
                    break;
                case BTN_ADD_NEW:
                    XImg.getUploadBox().setAlbumID(pil.getAlbumID());
                    XImg.getUploadBox().showModal();
                    break;
            }
        });
        panelTop.addButton(IMG64_SELECT_NONE, BTN_SELNONE);
        panelTop.addFixedSeparator();
        panelTop.addButton(TabAllImages.IMG48_TO_ALBUM, TabAllImages.BTN_TO_ALBUM);
        panelTop.addButton(TabAllImages.IMG48_ADD_TAGS, TabAllImages.BTN_ADD_TAGS);
        panelTop.addFixedSeparator();
        panelTop.addButton(IMG48_ADD_NEW, BTN_ADD_NEW);
        panelTop.addSeparator();
        panelTop.addButton(TabAllImages.IMG48_TO_TEMP, TabAllImages.BTN_TO_TEMP);
    }
    
    public void setAlbumID(long id) {
        pil.setAlbumID(id);
        if (!this.getChildren().contains(pil)) this.getChildren().add(pil);
    }
    
    public void refresh() {
        pil.refresh();
    }
    
    public void regenerate() {
        if (isNotInit) {
            dis.dbInit();
            isNotInit = false;
        }
        //pil.regenerateView(0);
    }
    
    public Parent getPaginator() {
        return pil.getPaginator();
    }
    
    public Parent getPanel() {
        return panelTop;
    }
}
