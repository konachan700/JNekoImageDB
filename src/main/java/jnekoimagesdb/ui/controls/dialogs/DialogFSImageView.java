package jnekoimagesdb.ui.controls.dialogs;

import java.nio.file.Path;
import java.util.ArrayList;
import javafx.scene.image.Image;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.PanelButtonCodes;
import jnekoimagesdb.ui.controls.ToolsImageViewer;
import jnekoimagesdb.ui.controls.ToolsPanelTop;

public class DialogFSImageView extends DialogWindow {
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
                    case buttonPrevItem:
                        prev();
                        break;
                    case buttonNextItem:
                        next();
                        break;
                    case buttonZoomIn: 
                        imgViewer.zoomIn();
                        break;
                    case buttonZoomOut:
                        imgViewer.zoomOut();
                        break;
                    case buttonOriginalSize:
                        imgViewer.zoomOrig();
                        break;
                    case buttonFitToWindowSize:
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
        panel.addButton(GUITools.loadIcon("arrow-left-48"), PanelButtonCodes.buttonPrevItem);
        panel.addButton(GUITools.loadIcon("arrow-right-48"), PanelButtonCodes.buttonNextItem);
        panel.addFixedSeparator();
        panel.addButton(GUITools.loadIcon("zoom-in-48"), PanelButtonCodes.buttonZoomIn);
        panel.addButton(GUITools.loadIcon("zoom-out-48"), PanelButtonCodes.buttonZoomOut);
        panel.addButton(GUITools.loadIcon("zoom-original-48"), PanelButtonCodes.buttonOriginalSize);
        panel.addButton(GUITools.loadIcon("zoom-fit-best-48"), PanelButtonCodes.buttonFitToWindowSize);
        
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
