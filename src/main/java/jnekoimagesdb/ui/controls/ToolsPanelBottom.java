package jnekoimagesdb.ui.controls;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jnekoimagesdb.ui.GUITools;

public class ToolsPanelBottom extends HBox {
    public static final int
            PANEL_HEIGHT = 24,
            TEXTFIELD_HEIGHT = 18,
            SEPARATOR_SIZE = 8;
    
    public interface ToolsPanelBottomActionListener {
        public void OnClick(int buttonID);
    }
    
    private static class ToolsPanelBottomTextField extends TextField {
        private volatile int xID = -1;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public ToolsPanelBottomTextField() {
            super("");
            GUITools.setStyle(this, "TopPanel", "textfield1");
            GUITools.setMaxSize(this, 9999, TEXTFIELD_HEIGHT);
        }
        
        public int getID() {
            return xID;
        }
    }
    
    private static class ToolsPanelBottomButton extends Button {
        private volatile int xID = -1;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public ToolsPanelBottomButton(Image icon, int id, ToolsPanelBottomActionListener al) {
            super("", new ImageView(icon));
            xID = id;
            GUITools.setStyle(this, "TopPanel", "button");
            GUITools.setFixedSize(this, PANEL_HEIGHT, PANEL_HEIGHT);
            this.setAlignment(Pos.CENTER);
            this.setOnMouseClicked((c) -> {
                if (al != null) al.OnClick(xID); 
                c.consume();
            });
        }
        
        public int getID() {
            return xID;
        }
    }
    
    private ToolsPanelBottomActionListener
            actListener = null;
    
    private final Map<String, ToolsPanelBottomTextField> 
            textFields = new HashMap<>();
    
    public void setAL(ToolsPanelBottomActionListener al) {
        actListener = al;
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    public ToolsPanelBottom() {
        super(4);
        GUITools.setStyle(this, "ToolsPanelBottom", "root_panel");
        this.setMaxHeight(PANEL_HEIGHT);
        this.setMinHeight(PANEL_HEIGHT);
        this.setPrefHeight(PANEL_HEIGHT);
        this.setAlignment(Pos.CENTER_LEFT);
    }
    
    public void clear() {
        this.getChildren().clear();
    }
    
    public void addItem(Node item) {
        this.getChildren().add(item);
    }
    
    public void removeItem(Node item) {
        this.getChildren().remove(item);
    }
    
    public void addTextField(String name) {
        final ToolsPanelBottomTextField tf = new ToolsPanelBottomTextField();
        textFields.put(name, tf);
        this.getChildren().add(tf); 
    }
    
    public TextField getTextField(String name) {
        final TextField tf = (TextField) textFields.get(name);
        return tf;
    }
    
    public void addButton(int id, Image icon) {
        final ToolsPanelBottomButton tpb = new ToolsPanelBottomButton(icon, id, actListener);
        this.getChildren().add(tpb);
    }
    
    public void addFixedSeparator() {
        final VBox sep1 = new VBox();
        GUITools.setStyle(sep1, "TopPanel", "separator");
        GUITools.setFixedSize(sep1, SEPARATOR_SIZE, PANEL_HEIGHT);
        this.getChildren().add(sep1);
    }
    
    public void addSeparator() {
        final VBox sep1 = new VBox();
        GUITools.setStyle(sep1, "TopPanel", "separator");
        GUITools.setMaxSize(sep1, 9999, PANEL_HEIGHT);
        this.getChildren().add(sep1);
    }
}
