package jnekoimagesdb.ui.md.toppanel;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import jiconfont.javafx.IconNode;

public class TopPanelButton extends Button {
    private final TopPanelButtonActionListener al;
    private final IconNode iconNode = new IconNode();
    
    public TopPanelButton(String iconStyle, String tooltip, TopPanelButtonActionListener a) {
        al = a;
        this.getStyleClass().addAll("panel_button_width", "panel_max_height", "panel_null_pane");
        this.setAlignment(Pos.CENTER);
        iconNode.getStyleClass().addAll(iconStyle, "panel_icon_color");
        this.setGraphic(iconNode); 
        this.setOnMouseClicked(c -> {
            if (al != null) al.OnClick(c);
        });
        this.setTooltip(new Tooltip(tooltip));
    } 
}
