package img.gui.elements;

import javafx.scene.control.TextArea;
import jnekoimagesdb.GUITools;

public class STextArea extends TextArea {
    public STextArea(int xMin, int xMax, int yMin, int yMax, String style) {
        super();
        this.getStylesheets().clear();
        this.getStylesheets().add(GUITools.CSS_FILE);
        this.getStyleClass().clear();
        this.getStyleClass().add("GUIElements_SScrollPane");
        this.getStyleClass().add("GUIElements_" + style);
        this.setMaxSize(xMax, yMax);
        this.setMinSize(xMin, yMin);
        this.setPrefSize(xMax, yMax);
    }
}
