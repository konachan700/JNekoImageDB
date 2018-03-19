package ui.simplepanel;

import javafx.scene.control.Button;
import jiconfont.IconCode;
import jiconfont.javafx.IconNode;

public class PanelButton extends Button {
    public PanelButton(String text, final ButtonClickListener listener) {
        super(text);
        getStyleClass().addAll("panel_button", "max_height");
        setOnAction(e -> listener.OnClick(e));
    }

    public PanelButton(String text, IconCode iconCode, final ButtonClickListener listener) {
        super(text);
        final IconNode iconNode = new IconNode(iconCode);
        iconNode.getStyleClass().addAll("panel_button_icon");
        iconNode.fillProperty().bind(this.textFillProperty());
        super.setGraphic(iconNode);
        getStyleClass().addAll("panel_button");
        setOnAction(e -> listener.OnClick(e));
    }
}
