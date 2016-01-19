package imgfsgui;

import imgfsgui.elements.SEVBox;
import imgfsgui.elements.SFHBox;
import imgfsgui.elements.SScrollPane;
import javafx.scene.control.ScrollPane;

public class PreviewTypesList extends SEVBox {
    
    
    private final SFHBox
            addNewItem = new SFHBox(4, 120, 9999, 32, 32);
    
    private final SScrollPane
            itemsScroll = new SScrollPane();
    
    private final SEVBox
            itemContainer = new SEVBox(4);
    
    public PreviewTypesList() {
        super();
        itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        itemsScroll.setFitToHeight(true);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setContent(itemContainer);
        
    }
    
    
}
