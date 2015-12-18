package imgfsgui;

import imgfs.ImgFS;
import imgfs.ImgFSPreviewGen;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import jnekoimagesdb.GUITools;

public class PagedImageList extends FlowPane {
    
    

    
    private int 
            isItResize      = 0,
            myWidth         = 0,
            myHeight        = 0, 
            elementSize     = 128,
            elementsInRow   = 0,
            elementsInCol   = 0, 
            totalElCount    = 0,
            currentPage     = 0;
    
    private final ArrayList<ImgFSPreviewGen.PreviewElement>
            elements = new ArrayList<>();

    private final Timeline resizeTimer = new Timeline(new KeyFrame(Duration.millis(88), ae -> {
        switch (isItResize) {
            case 1:
                
                
                
                
                break;
            default:
                break;
        }
    }));
    
    private Set<Long>
            tagsSet = new HashSet<>();
    
    private Long 
            albumID = 0L;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public PagedImageList() {
        GUITools.setStyle(this, "PagedImageList", "root_pane");
        
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            this.getChildren().clear();
            myWidth = newValue.intValue();
            elementsInRow = myWidth / elementSize;
            totalElCount = elementsInRow * elementsInCol;
            isItResize = 1;
        });
        
        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            this.getChildren().clear();
            myHeight = newValue.intValue();
            elementsInCol = myHeight / elementSize;
            totalElCount = elementsInRow * elementsInCol;
            isItResize = 1;
        });
        
        resizeTimer.setCycleCount(Animation.INDEFINITE);
        resizeTimer.play();
    }
    
    
    private void getList(int offset, int count) {
        
        //ImgFS.getDB("preview").
        
        
        //ImgFS.getDB("preview").iterator()
        
    }
    
    
}
