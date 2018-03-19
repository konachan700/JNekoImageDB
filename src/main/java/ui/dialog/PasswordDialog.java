package ui.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;

import java.awt.*;

public class PasswordDialog extends BaseDialogWindow {
    public static final int ACTION_OK = 1;
    public static final int ACTION_CANCEL = 2;

    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private final Label title = new Label("Password");
    private final PasswordField passwordField = new PasswordField();
    private final Button ok = new Button("OK");
    private final Button cancel = new Button("Cancel");

    private int action = ACTION_CANCEL;
    private String password = "";

    public PasswordDialog() {
        super("Enter password", (int) screenSize.getWidth() / 4, (int) screenSize.getHeight() / 4, true);

        passwordField.getStyleClass().addAll("text_box", "max_width");
        title.getStyleClass().addAll("label_for_textbox", "max_width");

        ok.getStyleClass().addAll("button", "max_width");
        cancel.getStyleClass().addAll("button", "max_width");

        ok.setOnAction(e -> {
            action = ACTION_OK;
            password = passwordField.getText();
            this.hide();
        });

        cancel.setOnAction(e -> {
            action = ACTION_CANCEL;
            password = "";
            this.hide();
        });

        final HBox buttons = new HBox();
        buttons.getStyleClass().addAll("null_pane", "max_width");
        buttons.getChildren().addAll(cancel, ok);

        final VBox passwordBox = new VBox();
        passwordBox.getStyleClass().addAll("button_box_pane", "max_width", "max_height");
        passwordBox.getChildren().addAll(title, passwordField);

        setContent(passwordBox, buttons);
    }

    public String getPassword() {
        final String tempPassword = password;
        password = "";
        return tempPassword;
    }

    public int getAction() {
        return action;
    }
}
