package ui.elements.generator;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.proto.UIPaneElement;
import ui.proto.UIUpdateCallback;

@HasStyledElements
public abstract class TextLineInputBox<T> extends VBox implements UIPaneElement<T> {
	@CssStyle({"ui_element_label"})
	private final Label label = new Label();

	@CssStyle({"ui_element_text_field_normal"})
	private final TextField textField = new TextField();

	private T value;
	private UIUpdateCallback<T> uiUpdateCallback;

	public abstract void onValueChanged(String text);

	public TextLineInputBox() {
		super();
		StyleParser.parseStyles(this);
		this.getStyleClass().addAll("null_pane");
		this.getChildren().addAll(getLabel(), getTextField());
		//getTextField().setOnKeyReleased(e -> onValueChanged(e, getText()));
		getTextField().textProperty().addListener((e, o, n) -> {
			onValueChanged(n);
		});
	}

	public Label getLabel() {
		return label;
	}

	public void setError(boolean state) {
		getTextField().getStyleClass().clear();
		getTextField().getStyleClass().add(state ? "ui_element_text_field_error" : "ui_element_text_field_normal");
	}

	public String getText() {
		return getTextField().getText().trim();
	}

	public void setText(String text) {
		getTextField().setText(text);
	}

	@Override
	public void setCaption(String value) {
		this.getLabel().setText(value);
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(T value) {
		this.value = value;
		getTextField().setText(value.toString());
	}

	@Override
	public Pane getUIElement() {
		return this;
	}

	@Override
	public void setUpdateCallback(UIUpdateCallback updateCallback) {
		this.setUiUpdateCallback(getUiUpdateCallback());
	}

	public UIUpdateCallback<T> getUiUpdateCallback() {
		return uiUpdateCallback;
	}

	public void setUiUpdateCallback(UIUpdateCallback<T> uiUpdateCallback) {
		this.uiUpdateCallback = uiUpdateCallback;
	}

	public TextField getTextField() {
		return textField;
	}
}
