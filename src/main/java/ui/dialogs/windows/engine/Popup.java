package ui.dialogs.windows.engine;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class Popup extends VBox {
	public interface ActionListener {
		void OnClick(MouseEvent e, Popup pane);
	}

	private final Label title = new Label();
	private final TextArea textArea = new TextArea();
	private final long createdTime;

	public Popup(String title, String text, ActionListener al) {
		getStyleClass().addAll("popup_message_pane");
		this.title.getStyleClass().addAll("popup_title");
		this.title.setText(title);
		textArea.setText(text);
		textArea.setWrapText(true);
		textArea.setEditable(false);
		textArea.setPrefRowCount(4);
		textArea.getStyleClass().addAll("text-area-transparent");
		this.getChildren().addAll(this.title, textArea);

		if (al != null) {
			this.title.setOnMouseClicked(e -> al.OnClick(e, this));
			textArea.setOnMouseClicked(e -> al.OnClick(e, this));
			setOnMouseClicked(e -> al.OnClick(e, this));
		}

		createdTime = System.currentTimeMillis();
	}

	public long getCreatedTime() {
		return createdTime;
	}
}
