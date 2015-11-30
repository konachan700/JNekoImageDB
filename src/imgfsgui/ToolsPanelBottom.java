package imgfsgui;

import dataaccess.Lang;
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
        
        public ToolsPanelBottomTextField() {
            super("");
            this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
            this.getStyleClass().add("TopPanel_textfield1");
            this.setMaxSize(9999, TEXTFIELD_HEIGHT);
            this.setMinSize(TEXTFIELD_HEIGHT, TEXTFIELD_HEIGHT);
            this.setPrefSize(9999, TEXTFIELD_HEIGHT);
        }
        
        public int getID() {
            return xID;
        }
    }
    
    private static class ToolsPanelBottomButton extends Button {
        private volatile int xID = -1;
        
        public ToolsPanelBottomButton(Image icon, int id, ToolsPanelBottomActionListener al) {
            super("", new ImageView(icon));
            xID = id;
            this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
            this.getStyleClass().add("TopPanel_button");
            this.setMaxSize(PANEL_HEIGHT, PANEL_HEIGHT);
            this.setMinSize(PANEL_HEIGHT, PANEL_HEIGHT);
            this.setPrefSize(PANEL_HEIGHT, PANEL_HEIGHT);
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
    
    public ToolsPanelBottom() {
        super(4);
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("ToolsPanelBottom");
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
        sep1.getStyleClass().add("TopPanel_separator");
        sep1.setMaxSize(SEPARATOR_SIZE, PANEL_HEIGHT);
        sep1.setMinSize(SEPARATOR_SIZE, PANEL_HEIGHT);
        sep1.setPrefSize(SEPARATOR_SIZE, PANEL_HEIGHT);
        this.getChildren().add(sep1);
    }
    
    public void addSeparator() {
        final VBox sep1 = new VBox();
        sep1.getStyleClass().add("TopPanel_separator");
        sep1.setMaxSize(9999, PANEL_HEIGHT);
        sep1.setMinSize(PANEL_HEIGHT, PANEL_HEIGHT);
        sep1.setPrefSize(9999, PANEL_HEIGHT);
        this.getChildren().add(sep1);
    }
}
