package img.gui.elements;

import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

public class SFVBox extends VBox {
    public SFVBox(int sz, int xMin, int xMax, int yMin, int yMax) {
        super(sz);
        init(xMin, xMax, yMin, yMax, "SVBox");
    }

    public SFVBox(int sz, int xMin, int xMax, int yMin, int yMax, String style) {
        super(sz);
        init(xMin, xMax, yMin, yMax, style);
    }

    private void init(int xMin, int xMax, int yMin, int yMax, String style) {
        GUITools.setStyle(this, "GUIElements", style);
        this.setMaxSize(xMax, yMax);
        this.setMinSize(xMin, yMin);
        this.setPrefSize(xMax, yMax);
    }
}
