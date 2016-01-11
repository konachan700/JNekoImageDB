package imgfsgui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

/*
    В этом классе делаем обертки над стандартными элементами, но уже со стилями и размерами, а в части элементов еще и с дополнительными полями.
    Это позволяет избежать многократно повторяющегося кода при создании окон и элементов.
    Также класс унифицирует работу с событиями элементов.
*/

public class GUIElements {
    public static final int
            EVENT_CODE_CLICK = 1,
            EVENT_CODE_CHANGE = 2;
    
    public static interface GUIActionListener {
        public void OnItemEvent(int evCode, int ID);
    }
    
    public static class SScrollPane extends ScrollPane {
        @SuppressWarnings("LeakingThisInConstructor")
        public SScrollPane() {
            super();
            GUITools.setStyle(this, "GUIElements", "SScrollPane");
            GUITools.setMaxSize(this, 9999, 9999);
        }
    }
    
    public static class SFLabel extends Label {
        @SuppressWarnings("LeakingThisInConstructor")
        public SFLabel(String text, int xMin, int xMax, int yMin, int yMax, String style, String className) {
            super(text);
            GUITools.setStyle(this, className, style);
            this.setMaxSize(xMax, yMax);
            this.setMinSize(xMin, yMin);
            this.setPrefSize(xMax, yMax);
        }
    }
    
    public static class SEVBox extends VBox {
        public SEVBox() {
            super(0);
            init(9999, 9999, "SVBox");
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
    
    public static class SFVBox extends VBox {
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
    
    public static class SFHBox extends HBox {
        public SFHBox(int sz, int xMin, int xMax, int yMin, int yMax) {
            super(sz);
            init(xMin, xMax, yMin, yMax, "SHBox");
        }
        
        public SFHBox(int sz, int xMin, int xMax, int yMin, int yMax, String style) {
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
    
    public static class STabTextButton extends Button {
        private volatile int xID = -1;
        
        public STabTextButton(String text, int id, int sizeX, int sizeY, GUIActionListener al) {
            super(text);
            init(id, sizeX, sizeY, al, "STabTextButton");
        }
        
        public STabTextButton(String text, int id, int sizeX, int sizeY, GUIActionListener al, String styleName) {
            super(text);
            init(id, sizeX, sizeY, al, styleName);
        }
        
        private void init(int id, int sizeX, int sizeY, GUIActionListener al, String styleName) {
            xID = id;
            GUITools.setStyle(this, "GUIElements", styleName);
            GUITools.setFixedSize(this, sizeX, sizeY);
            this.setAlignment(Pos.CENTER);
            this.setOnMouseClicked((c) -> {
                al.OnItemEvent(EVENT_CODE_CLICK, xID); 
            });
        }
        
        public int getID() {
            return xID;
        }
    }
    
    public static class STextArea extends TextArea {
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
