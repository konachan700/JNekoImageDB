package jnekoimagesdb.ui.controls.dialogs;

import java.nio.file.Path;
import java.util.ArrayList;
import javafx.scene.image.Image;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.ToolsImageViewer;
import jnekoimagesdb.ui.controls.ToolsPanelTop;

public class DialogFSImageView extends DialogWindow {
    public static final int 
            BUTTON_SIZE         = 64,
            BUTTON_PREV         = 1,
            BUTTON_NEXT         = 2,
            BUTTON_ZOOM_IN      = 3,
            BUTTON_ZOOM_OUT     = 4,
            BUTTON_ORIG         = 5, 
            BUTTON_FITTOWIN     = 6;
    
    private final ArrayList<Path>
            filesList = new ArrayList<>();
    
    private int 
            fileIndex = 0;
    
    private final ToolsImageViewer
            imgViewer = new ToolsImageViewer(new ToolsImageViewer.ToolsImageViewerActionListener() {
                @Override
                public void PrevKey() {
                    prev();
                }

                @Override
                public void NextKey() {
                    next();
                }
            });

    private final ToolsPanelTop
            panel = new ToolsPanelTop((index) -> {
                switch (index) {
                    case BUTTON_PREV:
                        prev();
                        break;
                    case BUTTON_NEXT:
                        next();
                        break;
                    case BUTTON_ZOOM_IN: 
                        imgViewer.zoomIn();
                        break;
                    case BUTTON_ZOOM_OUT:
                        imgViewer.zoomOut();
                        break;
                    case BUTTON_ORIG:
                        imgViewer.zoomOrig();
                        break;
                    case BUTTON_FITTOWIN:
                        imgViewer.zoomFitToWin();
                        break;
                }
            });
    
    private void next() {
        if (filesList.size() <= 1) return;
        if (fileIndex >= filesList.size()) return;
        
        fileIndex++;
        showImage(filesList.get(fileIndex));
    }
    
    private void prev() {
        if (filesList.size() <= 1) return;
        if (fileIndex <= 0) return;
        
        fileIndex--;
        showImage(filesList.get(fileIndex));
    }
    
    public DialogFSImageView() {
        super(1336, 768, false); // todo: сделать запоминание размера
        
        this.getToolbox().getChildren().add(panel);
        panel.addButton(GUITools.loadIcon("arrow-left-48"), BUTTON_PREV);
        panel.addButton(GUITools.loadIcon("arrow-right-48"), BUTTON_NEXT);
        panel.addFixedSeparator();
        panel.addButton(GUITools.loadIcon("zoom-in-48"), BUTTON_ZOOM_IN);
        panel.addButton(GUITools.loadIcon("zoom-out-48"), BUTTON_ZOOM_OUT);
        panel.addButton(GUITools.loadIcon("zoom-original-48"), BUTTON_ORIG);
        panel.addButton(GUITools.loadIcon("zoom-fit-best-48"), BUTTON_FITTOWIN);
        
        this.getMainContainer().getChildren().add(imgViewer);
    }
    
    public void setFiles(ArrayList<Path> p) {
        filesList.clear();
        filesList.addAll(p);
    }
    
    public void setFileIndex(Path p) {
        fileIndex = 0;
        for (int i=0; i<filesList.size(); i++) {
            if (filesList.get(i).compareTo(p) == 0) fileIndex = i;
        }
    }
    
    public void setFileIndex(int index) {
        fileIndex = index;
    }
    
    @Override
    public void show() {
        showImage(filesList.get(fileIndex));
        super.show();
    }
    
    private void showImage(Path imgPath) {
        final Image img = new Image(imgPath.toFile().toURI().toString());
        imgViewer.setImage(img);
    }
}
