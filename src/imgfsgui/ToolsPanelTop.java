package imgfsgui;

import dataaccess.Lang;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ToolsPanelTop extends HBox {
    public static final int 
            BUTTON_SIZE = 64,
            SEPARATOR_SIZE = 16;
    
    public static interface TopPanelButtonActionListener {
        public void OnClick(int buttonID);
    }
    
    private static class TopPanelButton extends Button {
        private volatile int xID = -1;
        
        public TopPanelButton(Image icon, int id, TopPanelButtonActionListener al) {
            super("", new ImageView(icon));
            xID = id;
            this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
            this.getStyleClass().add("TopPanel_button");
            this.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
            this.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
            this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
            this.setOnMouseClicked((c) -> {
                if (c.isPrimaryButtonDown()) al.OnClick(xID); 
            });
        }
        
        public int getID() {
            return xID;
        }
    }
    
    public final TopPanelButtonActionListener
            actListener;
    
    public ToolsPanelTop(TopPanelButtonActionListener al) {
        super(4);
        actListener = al;
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("TopPanel");
        this.setMaxHeight(BUTTON_SIZE);
        this.setMinHeight(BUTTON_SIZE);
        this.setPrefHeight(BUTTON_SIZE);
    }
    
    public void addFixedSeparator() {
        final VBox sep1 = new VBox();
        sep1.getStyleClass().add("TopPanel_separator");
        sep1.setMaxSize(SEPARATOR_SIZE, BUTTON_SIZE);
        sep1.setMinSize(SEPARATOR_SIZE, BUTTON_SIZE);
        sep1.setPrefSize(SEPARATOR_SIZE, BUTTON_SIZE);
        this.getChildren().add(sep1);
    }
    
    public void addSeparator() {
        final VBox sep1 = new VBox();
        sep1.getStyleClass().add("TopPanel_separator");
        sep1.setMaxSize(9999, BUTTON_SIZE);
        sep1.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        sep1.setPrefSize(9999, BUTTON_SIZE);
        this.getChildren().add(sep1);
    }
    
    public void addButton(Image icon, int id) {
        final TopPanelButton tpb = new TopPanelButton(icon, id, actListener);
        this.getChildren().add(tpb);
    }
    
    public void clearAll() {
        this.getChildren().clear();
    }
}
