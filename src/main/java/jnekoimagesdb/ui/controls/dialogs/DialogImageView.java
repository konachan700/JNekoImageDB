package jnekoimagesdb.ui.controls.dialogs;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.md.images.PagedImageList;
import jnekoimagesdb.ui.controls.PanelButtonCodes;
import jnekoimagesdb.ui.md.controls.ToolsImageViewer;
import jnekoimagesdb.ui.controls.ToolsPanelTop;

public class DialogImageView extends DialogWindow {
//    private int 
//            fileIndex = 0,
//            imagesCount = 0;
//    
//    private long 
//            currAlbumID = 0;
//    
//    private final PagedImageList
//            pil;
//    
//    private final ToolsImageViewer 
//            imgViewer = new ToolsImageViewer() {
//
//        @Override
//        public void PrevKey() {
//        }
//
//        @Override
//        public void NextKey() {
//        }
//    };
//
//    private final ToolsPanelTop
//            panel = new ToolsPanelTop((index) -> {
//                switch (index) {
//                    case buttonPrevItem:
//                        prev();
//                        break;
//                    case buttonNextItem:
//                        next();
//                        break;
//                    case buttonZoomIn: 
//                        imgViewer.zoomIn();
//                        break;
//                    case buttonZoomOut:
//                        imgViewer.zoomOut();
//                        break;
//                    case buttonOriginalSize:
//                        imgViewer.zoomOrig();
//                        break;
//                    case buttonFitToWindowSize:
//                        imgViewer.zoomFitToWin();
//                        break;
//                }
//            });
//    
//    private void next() {
//        if (fileIndex >= imagesCount) return;
//        fileIndex++;
//        _setImg();
//    }
//    
//    private void prev() {
//        if (fileIndex <= 0) return;
//        fileIndex--;
//        _setImg();
//    }
//    
//    public DialogImageView(PagedImageList p) {
//        super(1336, 768, false); // todo: сделать запоминание размера
//        pil = p;
//        
//        this.getToolbox().getChildren().add(panel);
//        panel.addButton(GUITools.loadIcon("arrow-left-48"), PanelButtonCodes.buttonPrevItem);
//        panel.addButton(GUITools.loadIcon("arrow-right-48"), PanelButtonCodes.buttonNextItem);
//        panel.addFixedSeparator();
//        panel.addButton(GUITools.loadIcon("zoom-in-48"), PanelButtonCodes.buttonZoomIn);
//        panel.addButton(GUITools.loadIcon("zoom-out-48"), PanelButtonCodes.buttonZoomOut);
//        panel.addButton(GUITools.loadIcon("zoom-original-48"), PanelButtonCodes.buttonOriginalSize);
//        panel.addButton(GUITools.loadIcon("zoom-fit-best-48"), PanelButtonCodes.buttonFitToWindowSize);
//        
//        this.getMainContainer().getChildren().add(imgViewer);
//    }
//    
//    public void setAlbumID(DSImageIDListCache.ImgType imgt, long aid) {
//        currAlbumID = aid;
//        pil.setImageType(imgt, aid);
//        imagesCount = (int) pil.getTotalImagesCount();
//    }
//    
//    public void setImageIndex(int index) {
//        fileIndex = index;
//    }
//    
//    private void _setImg() {
////        List<DSImage> dsi = pil.getImgListA(fileIndex, 1);
////        if ((dsi != null) && (!dsi.isEmpty())) {
////            final Image img;
////            try {
////                img = XImgDatastore.getImage(dsi.get(0).getMD5());
////                imgViewer.setImage(img);
////            } catch (IOException ex) {
////                Logger.getLogger(DialogImageView.class.getName()).log(Level.SEVERE, null, ex);
////            }
////        }
//    }
//    
//    @Override
//    public void show() {
//        _setImg();
//        super.show();
//    }
}
