package img.gui.tabs;

import img.gui.PreviewTypesList;
import img.gui.elements.SEVBox;
import img.gui.elements.SFLabel;
import img.gui.elements.SScrollPane;
import javafx.scene.control.ScrollPane;

public class TabSettings extends SEVBox   {
    
    private final PreviewTypesList
            prevTypes = new PreviewTypesList();
    
    private final SScrollPane
            itemsScroll = new SScrollPane();
    
    private final SEVBox
            scrollableContainer = new SEVBox(0);
    
    public TabSettings() {
        super(0);
        
        prevTypes.setMinSize(256, 300);
        prevTypes.setPrefSize(9999, 300);
        prevTypes.setMaxSize(9999, 300);
        
        itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        itemsScroll.setFitToHeight(true);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setContent(scrollableContainer);
        scrollableContainer.getChildren().addAll(
                prevTypes
        );
        
        this.getChildren().addAll(
                new SFLabel("Настройки программы", 64, 9999, 32, 32, "label_header", "TabSettings"), 
                itemsScroll
        );
    }
    
    public void refresh() {
        prevTypes.refresh();
    }
    
}
