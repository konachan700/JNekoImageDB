package img.gui.elements;

import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

public class SEVBox extends VBox {
    public SEVBox() {
        super(0);
        init(9999, 9999, "SVBox");
    }
    
    public SEVBox(String style) {
        super(0);
        init(9999, 9999, style);
    }

    public SEVBox(int sz) {
        super(sz);
        init(9999, 9999, "SVBox");
    }

    public SEVBox(int sz, int x, int y) {
        super(sz);
        init(x, y, "SVBox");
    }

    public SEVBox(int sz, int x, int y, String style) {
        super(sz);
        init(x, y, style);
    }

    private void init(int x, int y, String style) {
        GUITools.setStyle(this, "GUIElements", style);
        GUITools.setMaxSize(this, x, y);
    }
}
