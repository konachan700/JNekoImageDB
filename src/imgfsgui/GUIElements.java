package imgfsgui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jnekoimagesdb.GUITools;

public class GUIElements {
    public static final int
            EVENT_CODE_CLICK = 1,
            EVENT_CODE_CHANGE = 2;
    
    public static interface GUIActionListener {
        public void OnItemEvent(int evCode, int ID);
    }
    
    public static class SButton extends Button {
        private volatile int xID = -1;
        
        public SButton(Image icon, int id, int size, GUIActionListener al) {
            super("", new ImageView(icon));
            init(id, size, al, "button");
        }
        
        public SButton(Image icon, int id, int size, GUIActionListener al, String styleName) {
            super("", new ImageView(icon));
            init(id, size, al, styleName);
        }
        
        private void init(int id, int size, GUIActionListener al, String styleName) {
            xID = id;
            GUITools.setStyle(this, "GUIElements", styleName);
            GUITools.setFixedSize(this, size, size);
            this.setAlignment(Pos.CENTER);
            this.setOnMouseClicked((c) -> {
                al.OnItemEvent(EVENT_CODE_CLICK, xID); 
            });
        }
        
        public int getID() {
            return xID;
        }
    }
    
    public static class STextField extends TextField {
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
    
    public static class SNumericTextField extends TextField {
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
}
