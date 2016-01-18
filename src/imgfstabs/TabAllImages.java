package imgfstabs;

import datasources.DSAlbum;
import dialogs.DialogAlbumSelect;
import imgfsgui.GUIElements;
import imgfsgui.PagedImageList;
import imgfsgui.ToolsPanelTop;
import java.util.ArrayList;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import jnekoimagesdb.GUITools;

public class TabAllImages extends GUIElements.SEVBox  {
    public static final Image 
            IMG48_ADD_TAGS       = GUITools.loadIcon("add-tags-48"),
            IMG48_TO_ALBUM       = GUITools.loadIcon("add-to-album-48"),
            IMG48_TO_TEMP        = GUITools.loadIcon("addtotemp-48"); 
    
    public static final int
            BTN_ADD_TAGS    = 10,
            BTN_TO_ALBUM    = 11,
            BTN_TO_TEMP     = 12;
        
    public static enum FilterType {
        all, nottags, notinalbums
    }
    
    private boolean 
            isNotInit = true;
    
    private final PagedImageList 
            pil = new PagedImageList();
    
    private final ToolsPanelTop 
            panelTop;
    
    private final DialogAlbumSelect
            dis = new DialogAlbumSelect();
    
    public TabAllImages() {
        super(0, 9999, 9999);
        this.getChildren().add(pil);
        
        panelTop = new ToolsPanelTop((index) -> {
            switch (index) {
                case TabAddImagesToDB.BTN_SELNONE:
                    pil.selectNone();
                    break;
                case TabAddImagesToDB.BTN_DEL:
                    
                    break;
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
                    
                    break;
            }
        });
        panelTop.addButton(TabAddImagesToDB.IMG64_SELECT_NONE, TabAddImagesToDB.BTN_SELNONE);
        panelTop.addFixedSeparator();
        panelTop.addButton(TabAddImagesToDB.IMG64_DELETE, TabAddImagesToDB.BTN_DEL);
        panelTop.addFixedSeparator();
        panelTop.addButton(TabAllImages.IMG48_TO_ALBUM, TabAllImages.BTN_TO_ALBUM);
        panelTop.addButton(TabAllImages.IMG48_ADD_TAGS, TabAllImages.BTN_ADD_TAGS);
        panelTop.addSeparator();
        panelTop.addButton(TabAllImages.IMG48_TO_TEMP, TabAllImages.BTN_TO_TEMP);
    }
    
    public void setAlbumID(long id) {
        pil.setAlbumID(id);
    }
    
    public void refresh() {
        pil.refresh();
    }
    
    public void regenerate() {
        if (isNotInit) {
            pil.initDB();
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
