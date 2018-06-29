package ui.elements;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import jiconfont.IconCode;
import jiconfont.javafx.IconNode;

public class VerticalIconsPanel extends VBox {
	public interface ActionListener {
		void OnClick(String buttonName);
	}

	private final Map<String, Button> icons = new HashMap<>();

	public VerticalIconsPanel() {
		getStyleClass().add("vertical_panel_box");
	}

	public void add(String uniqueName, IconCode iconCode, ActionListener al) {
		final Button b = new Button("");
		b.setTooltip(new Tooltip(uniqueName));
		final IconNode iconNode = new IconNode(iconCode);
		iconNode.getStyleClass().addAll("vertical_panel_button_icon");
		iconNode.fillProperty().bind(b.textFillProperty());
		b.setGraphic(iconNode);
		b.setOnMouseClicked(e -> al.OnClick(uniqueName));
		b.getStyleClass().addAll("vertical_panel_box_button");
		this.getChildren().addAll(b);
		icons.put(uniqueName, b);
	}

	public void addFixedSeparator() {
		final VBox vBox = new VBox();
		vBox.getStyleClass().addAll("vertical_panel_separator");
		this.getChildren().addAll(vBox);
	}
}
