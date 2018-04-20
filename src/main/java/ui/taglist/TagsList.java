package ui.taglist;

import java.util.HashMap;
import java.util.Map;

import annotation.CssStyle;
import annotation.HasStyledElements;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.StyleParser;
import ui.elements.PanelButton;

@HasStyledElements
public class TagsList extends VBox {
	@CssStyle({"tags_scroll_pane"})
	private final ScrollPane scrollPane = new ScrollPane();

	@CssStyle({"tags_null_pane"})
	private final FlowPane flowPane = new FlowPane();

	@CssStyle({"tag_text_field"})
	private final TextField textField = new TextField();

	@CssStyle({"tag_add_container"})
	private final HBox hBox = new HBox();

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton button = new PanelButton("Add tag") {
		@Override public void onClick(ActionEvent e) {
			addTag(textField.getText());
			textField.setText("");
		}
	};

	private final Map<String, TagElement> map = new HashMap<>();

	public TagsList() {
		StyleParser.parseStyles(this);
		flowPane.setVgap(4);
		flowPane.setHgap(6);

		scrollPane.setContent(flowPane);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		hBox.getChildren().addAll(textField, button);
		getChildren().addAll(hBox, scrollPane);

		textField.setPromptText("Type a tag...");
		textField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				addTag(textField.getText());
				textField.setText("");
			}
		});
	}

	public void addTag(String tag) {
		if (tag == null || tag.isEmpty() || tag.trim().isEmpty() || map.containsKey(tag)) return;

		final TagElement tagElement = new TagElement(tag);
		tagElement.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				map.remove(tag);
				flowPane.getChildren().remove(tagElement);
			}
		});
		map.put(tag, tagElement);
		flowPane.getChildren().add(tagElement);
	}
}
