package jnekoimagesdb.ui.controls;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jnekoimagesdb.ui.GUITools;

public class ToolsPanelTop extends HBox {
    public static final int 
            BUTTON_SIZE = 64,
            SEPARATOR_SIZE = 16;
    
    public static interface TopPanelButtonActionListener {
        public void OnClick(PanelButtonCodes buttonID);
    }
    
    private static class TopPanelButton extends Button {
        private PanelButtonCodes xID = PanelButtonCodes.buttonUnknown;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public TopPanelButton(Image icon, PanelButtonCodes id, TopPanelButtonActionListener al) {
            super("", new ImageView(icon));
            xID = id;
            GUITools.setStyle(this, "TopPanel", "button");
            GUITools.setFixedSize(this, BUTTON_SIZE, BUTTON_SIZE);
            this.setAlignment(Pos.CENTER);
            this.setOnMouseClicked((c) -> {
                al.OnClick(xID); 
            });
        }
        
        public PanelButtonCodes getID() {
            return xID;
        }
    }
    
    public final TopPanelButtonActionListener
            actListener;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public ToolsPanelTop(TopPanelButtonActionListener al) {
        super(4);
        actListener = al;
        GUITools.setStyle(this, "TopPanel", "root_pane");
        this.setMaxHeight(BUTTON_SIZE);
        this.setMinHeight(BUTTON_SIZE);
        this.setPrefHeight(BUTTON_SIZE);
    }
    
    public void addFixedSeparator() {
        final VBox sep1 = new VBox();
        GUITools.setStyle(sep1, "TopPanel", "separator");
        GUITools.setFixedSize(sep1, SEPARATOR_SIZE, BUTTON_SIZE);
        this.getChildren().add(sep1);
    }
    
    public void addSeparator() {
        final VBox sep1 = new VBox();
        GUITools.setStyle(sep1, "TopPanel", "separator");
        GUITools.setMaxSize(sep1, 9999, BUTTON_SIZE);
        this.getChildren().add(sep1);
    }
    
    public void addButton(Image icon, PanelButtonCodes id) {
        final TopPanelButton tpb = new TopPanelButton(icon, id, actListener);
        this.getChildren().add(tpb);
    }
    
    public void addButton(Image icon, PanelButtonCodes id, String tooltip) {
        final TopPanelButton tpb = new TopPanelButton(icon, id, actListener);
        tpb.setTooltip(GUITools.createTT(tooltip));
        this.getChildren().add(tpb);
    }
    
    public void clearAll() {
        this.getChildren().clear();
    }
}
