package jnekoimagesdb.ui.controls.elements;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import jnekoimagesdb.ui.GUITools;

public class STextField extends TextField {
    private volatile ElementsIDCodes xID = ElementsIDCodes.textUnknown;

    public STextField(ElementsIDCodes id, int height, GUIActionListener al, String styleName) {
        super("");
        init(id, -1, height, al, styleName);
    }

    public STextField(ElementsIDCodes id, int width, int height, GUIActionListener al) {
        super("");
        init(id, width, height, al, "textfield");
    }
    
    public STextField(int height) {
        super("");
        init(ElementsIDCodes.textUnknown, -1, height, null, "textfield");
    }
    
    public STextField setHelpText(String s) {
        this.setPromptText(s); 
        return this;
    }

    private void init(ElementsIDCodes id, int w, int h, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        if (w <= 0) GUITools.setMaxSize(this, 9999, h); GUITools.setFixedSize(this, w, h);
        this.setAlignment(Pos.CENTER_LEFT);
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            if (al != null) al.OnItemEvent(ElementsEventCodes.eventContentChange, xID); 
        });
    }

    public ElementsIDCodes getID() {
        return xID;
    }
}