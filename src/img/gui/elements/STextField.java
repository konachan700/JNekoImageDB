package img.gui.elements;

import static img.gui.elements.GUIElements.EVENT_CODE_CHANGE;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import jnekoimagesdb.GUITools;

public class STextField extends TextField {
    private volatile int xID = -1;

    public STextField(int id, int height, GUIActionListener al, String styleName) {
        super("");
        init(id, 0, height, al, styleName);
    }

    public STextField(int id, int width, int height, GUIActionListener al) {
        super("");
        init(id, width, height, al, "textfield");
    }

    private void init(int id, int w, int h, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        if (w <= 0) GUITools.setMaxSize(this, 9999, h); GUITools.setFixedSize(this, w, h);
        this.setAlignment(Pos.CENTER_LEFT);
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            al.OnItemEvent(EVENT_CODE_CHANGE, xID); 
        });
    }

    public int getID() {
        return xID;
    }
}