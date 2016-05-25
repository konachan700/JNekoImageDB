package jnekoimagesdb.ui.controls.tabs;

import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.ui.controls.dialogs.DialogAlbumSelect;
import jnekoimagesdb.ui.controls.PagedImageList;
import jnekoimagesdb.ui.controls.ToolsPanelTop;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.PanelButtonCodes;

public class TabAllImages extends SEVBox  {
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
    
    private final ToolsPanelTop.SPanelPopupMenuButton 
            menuBtn = new ToolsPanelTop.SPanelPopupMenuButton();
    
    public TabAllImages() {
        super(0, 9999, 9999);
        this.getChildren().add(pil);
        
        panelTop = new ToolsPanelTop((index) -> {
            switch (index) {
                case buttonExportToExchangeFolder:
                    pil.uploadSelected();
                    break;
            }
        });

        panelTop.addSeparator();
        panelTop.addButton(GUITools.loadIcon("addtotemp-48"),PanelButtonCodes.buttonExportToExchangeFolder);  

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
        
        panelTop.addMenuButton(menuBtn);
    }
    
    public void setAlbumID(long id) {
        pil.setAlbumID(id);
        if (!this.getChildren().contains(pil)) this.getChildren().add(pil);
    }
    
    public void refresh() {
        pil.refresh();
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
        //pil.regenerateView(0);
    }
    
    public Parent getPaginator() {
        return pil.getPaginator();
    }
    
    public Parent getPanel() {
        return panelTop;
    }
}
