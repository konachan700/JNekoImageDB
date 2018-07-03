package ui.dialogs.windows;

import static ui.dialogs.activities.engine.ActivityHolder.getSeparator;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.dialogs.windows.engine.DefaultWindow;
import ui.elements.PanelButton;

public class PasswordWindow extends DefaultWindow {
	private final PasswordWindow THIS = this;

	@CssStyle({"ui_element_label"})
	private final Label label = new Label("Enter password and press 'Open' button.");

	@CssStyle({"ui_element_text_field_normal"})
	private final PasswordField textField = new PasswordField();

	private String password = null;

	@CssStyle({"panel_button_big_1"})
	private final PanelButton okButton = new PanelButton("Open") {
		@Override
		public void onClick(ActionEvent e) {
			setPassword(textField.getText());
			THIS.close();
		}
	};

	@CssStyle({"panel_button_footer_1"})
	private final PanelButton exitButton = new PanelButton("Exit") {
		@Override
		public void onClick(ActionEvent e) {
			setPassword(null);
			THIS.close();
		}
	};

	public PasswordWindow() {
		super("Enter password", true, false, true);

		StyleParser.parseStyles(this);

		final VBox vBox = new VBox();
		vBox.getStyleClass().addAll("fill_all_for_password");
		vBox.getChildren().addAll(label, textField);

		this.getActivityHolder().getChildren().addAll(vBox);

		getHeader().getChildren().addAll(getSeparator(), okButton);
		getFooter().getChildren().addAll(getSeparator(), exitButton);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
