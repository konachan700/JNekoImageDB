package imgfsgui.elements;

import javafx.scene.control.Label;
import jnekoimagesdb.GUITools;

public class SFLabel extends Label {
    @SuppressWarnings("LeakingThisInConstructor")
    public SFLabel(String text, int xMin, int xMax, int yMin, int yMax, String style, String className) {
        super(text);
        GUITools.setStyle(this, className, style);
        this.setMaxSize(xMax, yMax);
        this.setMinSize(xMin, yMin);
        this.setPrefSize(xMax, yMax);
    }
}