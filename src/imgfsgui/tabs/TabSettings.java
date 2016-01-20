package imgfsgui.tabs;

import imgfsgui.PreviewTypesList;
import imgfsgui.elements.SEVBox;

public class TabSettings extends SEVBox   {
    
    private final PreviewTypesList
            prevTypes = new PreviewTypesList();
    
    public TabSettings() {
        super(8);
        
        prevTypes.setMinSize(256, 200);
        prevTypes.setPrefSize(9999, 200);
        prevTypes.setMaxSize(9999, 200);
        this.getChildren().add(prevTypes);
    }
    
    public void refresh() {
        prevTypes.refresh();
    }
    
}
