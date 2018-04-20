package jnekouilib.panel;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import jiconfont.javafx.IconNode;

public class PanelButton extends Button {
    private final PanelButtonActionListener al;
    private final IconNode iconNode = new IconNode();
    
    public PanelButton(String iconStyle, String tooltip, PanelButtonActionListener a) {
        al = a;
        this.getStyleClass().addAll("topPanelButton", "topPanelButton_width");
        this.setAlignment(Pos.CENTER);
        iconNode.getStyleClass().addAll(iconStyle, "topPanelButtonIcon");
        this.setGraphic(iconNode); 
        this.setOnMouseClicked(c -> {
            if (al != null) al.OnClick(c);
        });
        this.setTooltip(new Tooltip(tooltip));
    } 
    
    public PanelButton(String iconStyle, String tooltip, String dispText, PanelButtonActionListener a) {
        this(iconStyle, tooltip, a);
        super.setAlignment(Pos.CENTER);
        super.setText(dispText);
        super.getStyleClass().remove("topPanelButton_width");
        super.getStyleClass().addAll("topPanelButton_wtext");
        super.setMinWidth(Region.USE_PREF_SIZE);
    }
}
