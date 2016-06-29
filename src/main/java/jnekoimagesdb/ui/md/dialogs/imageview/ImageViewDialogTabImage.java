package jnekoimagesdb.ui.md.dialogs.imageview;

import javafx.scene.Node;
import javafx.scene.image.Image;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.ui.md.controls.ToolsImageViewer;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;

public class ImageViewDialogTabImage extends ToolsImageViewer {
    private final TopPanel
            panelTopImageTab = new TopPanel();
    
    private DSImageIDListCache
            imagesCache = null;
    
    private int 
            currentIID = 0;
    
    public ImageViewDialogTabImage() {
        super();
        panelTopImageTab.addNodes(
                new TopPanelButton("panel_icon_prev", "Предыдущее изображение", c -> {
                    PrevKey();
                }),
                new TopPanelButton("panel_icon_next", "Следующее изображение", c -> {
                    NextKey();
                }),
                new TopPanelButton("panel_icon_zoom_in", "Увеличить", c -> {
                    super.zoomIn();
                }),
                new TopPanelButton("panel_icon_zoom_out", "Уменьшить", c -> {
                    super.zoomOut();
                }),
                new TopPanelButton("panel_icon_zoom_fit", "В размер экрана", c -> {
                    super.zoomFitToWin();
                }),
                new TopPanelButton("panel_icon_zoom_orig", "Оригинальный размер", c -> {
                    super.zoomOrig();
                })
        );
    }
    
    private boolean showImageA(int iid) {
        final Image img;
        if (imagesCache != null) 
            img = imagesCache.getImage(iid);
        else
            img = DSImageIDListCache.getAll().getImage(iid);
        if (img != null) {
            super.setImage(img);
            return true;
        } else 
            return false;
    }
    
    public final DSImage getDSImage() {
        return imagesCache.getDSImage(currentIID);
    }

    public Node getTopPanel() {
        return panelTopImageTab;
    }
    
    public void setCache(DSImageIDListCache c) {
        imagesCache = c;
    }
    
    public void setIID(int iid) {
        currentIID = iid;
    }

    @Override
    public final void PrevKey() {
        if (showImageA(currentIID+1)) currentIID++;
    }

    @Override
    public final void NextKey() {
        if (showImageA(currentIID-1)) currentIID--;
    }
}
