package ui.elements;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import jiconfont.IconCode;
import jiconfont.javafx.IconNode;
import ui.annotation.style.HasStyledElements;

@HasStyledElements
public abstract class PanelButton extends Button {
    public abstract void onClick(ActionEvent e);

    public PanelButton(String text) {
        super(text);
        super.setWrapText(false);
        super.getStyleClass().clear();
        //super.getStyleClass().addAll("panel_button");
        super.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        setOnAction(e -> onClick(e));
    }

    public PanelButton(String text, IconCode iconCode) {
        super(text);
        final IconNode iconNode = new IconNode(iconCode);
        iconNode.getStyleClass().addAll("panel_button_icon");
        iconNode.fillProperty().bind(this.textFillProperty());
        super.setGraphic(iconNode);
        super.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        setOnAction(e -> onClick(e));
    }
}
