package jnekoimagesdb.ui.md.toppanel;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;

public class TopPanelInfobox extends HBox {
    private final Label 
            title = new Label(),
            text = new Label();
    
    public TopPanelInfobox(String iconStyle) {
        super();
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().addAll("panel_max_width", "panel_max_height", "panel_null_pane");
        
        final VBox container = new VBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().addAll("panel_max_width", "panel_max_height", "panel_null_pane");
        
        final Label icon = new Label();
        icon.getStyleClass().addAll("panel_button_width", "panel_max_height", "panel_null_pane");
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().add(iconStyle);
        icon.setGraphic(iconNode);
        
        this.getChildren().addAll(icon, container);
        
        title.setAlignment(Pos.CENTER_LEFT);
        title.getStyleClass().addAll("panel_max_width", "panel_info_title_height", "panel_null_pane");
        text.setAlignment(Pos.TOP_LEFT);
        text.getStyleClass().addAll("panel_max_width", "panel_info_text_height", "panel_null_pane");
        
        container.getChildren().addAll(title, text);
    }
    
    public void setText(String t) {
        text.setText(t);
    }
    
    public void setTitle(String t) {
        title.setText(t);
    }
}
