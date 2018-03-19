package ui.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Dimension;
import java.awt.Toolkit;

public class YesNoDialog extends BaseDialogWindow {
    public static final int ACTION_YES = 1;
    public static final int ACTION_NO = 2;

    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private final Button ok = new Button("Yes");
    private final Button cancel = new Button("No");
    private final TextArea textArea = new TextArea();

    private int action = ACTION_NO;

    public YesNoDialog(String text, boolean yesNo) {
        super("Alert", (int) screenSize.getWidth() / 4, (int) screenSize.getHeight() / 4, true);
        cancel.setVisible(yesNo);

        textArea.getStyleClass().addAll("StringFieldElementTextArea", "max_width", "max_height");
        textArea.setWrapText(true);
        textArea.setText(text);

        ok.getStyleClass().addAll("button", "max_width");
        cancel.getStyleClass().addAll("button", "max_width");

        ok.setOnAction(e -> {
            action = ACTION_YES;
            this.hide();
        });

        cancel.setOnAction(e -> {
            action = ACTION_NO;
            this.hide();
        });

        final HBox buttons = new HBox();
        buttons.getStyleClass().addAll("null_pane", "max_width");
        buttons.getChildren().addAll(cancel, ok);

        setContent(textArea, buttons);
    }

    public int getAction() {
        return action;
    }
}
