package imgfstabs;

import imgfsgui.GUIElements;
import imgfsgui.PagedImageList;
import javafx.scene.Parent;

public class TabAllImages extends GUIElements.SEVBox  {
    public static enum FilterType {
        all, nottags, notinalbums
    }
    
    private boolean 
            isNotInit = true;
    
    private final PagedImageList 
            pil = new PagedImageList();
    
    public TabAllImages() {
        super(0, 9999, 9999);
        this.getChildren().add(pil);
    }
    
    public void regenerate() {
        if (isNotInit) {
            pil.initDB();
            isNotInit = false;
        }
        //pil.regenerateView(0);
    }
    
    public Parent getPaginator() {
        return pil.getPaginator();
    }
}
