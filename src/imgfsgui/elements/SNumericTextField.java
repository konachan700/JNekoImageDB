package imgfsgui.elements;

import static imgfsgui.elements.GUIElements.EVENT_CODE_CHANGE;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import jnekoimagesdb.GUITools;

public class SNumericTextField extends TextField {
    private volatile int xID = -1;

    public SNumericTextField(int id, int height, GUIActionListener al, String styleName) {
        super("0");
        init(id, 0, height, al, styleName);
    }

    public SNumericTextField(int id, int width, int height, GUIActionListener al) {
        super("0");
        init(id, width, height, al, "textfield");
    }

    private void init(int id, int w, int h, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        if (w <= 0) GUITools.setMaxSize(this, 9999, h); GUITools.setFixedSize(this, w, h);
        this.setAlignment(Pos.CENTER);
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            this.getStyleClass().remove("GUIElements_textfield_valid");
            this.getStyleClass().remove("GUIElements_textfield_error");
            try {
                final int test = Integer.parseInt(newValue.trim(), 10);
                if (test >= 0) {
                    this.getStyleClass().add("GUIElements_textfield_valid");
                    al.OnItemEvent(EVENT_CODE_CHANGE, xID);
                } 
            } catch (NumberFormatException e) { 
                this.getStyleClass().add("GUIElements_textfield_error");
                this.setText(oldValue);
            }
        });
    }

    public long getIntValue() {
        try {
            return Integer.parseInt(this.getText().trim(), 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public long getLongValue() {
        try {
            return Long.parseLong(this.getText().trim(), 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getID() {
        return xID;
    }
}