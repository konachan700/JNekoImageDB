package dialogs;

import dataaccess.Lang;
import imgfsgui.ToolsImageViewer;
import imgfsgui.ToolsPanelTop;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import jnekoimagesdb.GUITools;

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

                }

                @Override
                public void NextKey() {

                }
            });

    private final ToolsPanelTop
            panel = new ToolsPanelTop((index) -> {
                switch (index) {
                    case BUTTON_PREV:
                        
                        break;
                    case BUTTON_NEXT:
                        
                        break;
                    case BUTTON_ZOOM_IN: 
                        
                        break;
                    case BUTTON_ZOOM_OUT:
                        
                        break;
                    case BUTTON_ORIG:
                        
                        break;
                    case BUTTON_FITTOWIN:
                        
                        break;
                }
            });
    
    public DialogFSImageView() {
        super(1024, 768, false);
        this.getToolbox().getChildren().add(panel);
        panel.addButton(new Image(new File("./icons/arrow-right.png").toURI().toString()), BUTTON_PREV);
        panel.addButton(new Image(new File("./icons/arrow-left.png").toURI().toString()), BUTTON_NEXT);
        panel.addFixedSeparator();
        panel.addButton(new Image(new File("./icons/zoom-in.png").toURI().toString()), BUTTON_ZOOM_IN);
        panel.addButton(new Image(new File("./icons/zoom-out.png").toURI().toString()), BUTTON_ZOOM_OUT);
        panel.addButton(new Image(new File("./icons/zoom-original.png").toURI().toString()), BUTTON_ORIG);
        panel.addButton(new Image(new File("./icons/zoom-fit-best.png").toURI().toString()), BUTTON_FITTOWIN);
        
        this.getMainContainer().getChildren().add(imgViewer);
    }
    
    public void setFiles(ArrayList<Path> p) {
        filesList.addAll(p);
    }
    
    public void setFileIndex(int index) {
        fileIndex = index;
    }
    
    @Override
    public void show() {
        super.show();
        
    }
    
    private void showImage(Path imgPath) {
        
    }
}
