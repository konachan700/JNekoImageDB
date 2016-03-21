package jnekoimagesdb.ui.controls.elements;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import jnekoimagesdb.ui.GUITools;

public class SNumericTextField extends TextField {
    private ElementsIDCodes xID = ElementsIDCodes.textUnknown;
    public int
            max = 0xFFFF,
            min = 0;
    
    private long
            value = 0;
    
    
    public SNumericTextField(ElementsIDCodes id, int height, GUIActionListener al, String styleName) {
        super("0");
        init(id, 0, height, al, styleName);
    }

    public SNumericTextField(ElementsIDCodes id, int width, int height, GUIActionListener al) {
        super("0");
        init(id, width, height, al, "textfield");
    }

    private void init(ElementsIDCodes id, int w, int h, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        if (w <= 0) GUITools.setMaxSize(this, 9999, h); GUITools.setFixedSize(this, w, h);
        this.setAlignment(Pos.CENTER);
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            this.getStyleClass().remove("GUIElements_textfield_valid");
            this.getStyleClass().remove("GUIElements_textfield_error");
            try {
                final long test = Long.parseLong(newValue.trim(), 10);
                if ((test >= min) && (test <= max)) {
                    this.getStyleClass().add("GUIElements_textfield_valid");
                    value = test;
                    if (al != null) al.OnItemEvent(ElementsEventCodes.eventContentChange, xID);
                } else {
                    this.getStyleClass().add("GUIElements_textfield_error");
                }
            } catch (NumberFormatException e) { 
                this.getStyleClass().add("GUIElements_textfield_error");
                //this.setText(oldValue);
            }
        });
    }

    public int getIntValue() {
        return (int) value;
    }

    public long getLongValue() {
        return value;
    }

    public ElementsIDCodes getID() {
        return xID;
    }
}