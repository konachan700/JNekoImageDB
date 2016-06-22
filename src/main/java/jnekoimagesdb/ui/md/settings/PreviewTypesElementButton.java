package jnekoimagesdb.ui.md.settings;

import jnekoimagesdb.ui.md.tags.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import jiconfont.javafx.IconNode;

public class PreviewTypesElementButton extends Button {
    private final TagElementButtonActionListener tagBtnAL;
    private final IconNode iconNode = new IconNode();
    
    public PreviewTypesElementButton(String iconStyle, String toolTopText, TagElementButtonActionListener al) {
        super();
        tagBtnAL = al;
        this.getStyleClass().addAll("previews_element_button_width", "previews_element_button_height", "albums_null_pane");
        this.setAlignment(Pos.CENTER);
        iconNode.getStyleClass().addAll(iconStyle, "previews_element_button_icon_color");
        this.setGraphic(iconNode); 
        final Tooltip tt = new Tooltip();
        tt.setText(toolTopText);
        this.setTooltip(tt);
        this.setOnMouseClicked(c -> {
            if (tagBtnAL != null) tagBtnAL.OnClick(c);
        });
    }
}
