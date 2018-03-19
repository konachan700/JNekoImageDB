package ui.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import ui.simplepanel.Panel;
import ui.simplepanel.PanelButton;

import java.awt.Dimension;
import java.awt.Toolkit;

public class WaitDialog extends BaseDialogWindow {
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String PLEASE_WAIT = "Please, wait...";
    public static final String CANCEL = "Cancel";
    public static final String COMPLETED = "Completed";

    private boolean started = false;

    private final TextArea textArea = new TextArea();
    private final PanelButton panelButton = new PanelButton(CANCEL, GoogleMaterialDesignIcons.CLOSE, e -> {
        this.hide();
    });
    private final Panel panel = new Panel(Panel.getSpacer(), panelButton);

    public WaitDialog() {
        super(PLEASE_WAIT, (int) screenSize.getWidth() / 3, (int) screenSize.getHeight() / 4, true);

        textArea.getStyleClass().addAll("StringFieldElementTextArea", "max_width", "max_height");
        textArea.setWrapText(true);
        textArea.setText("");

        setContent(textArea, panel);
    }

    public void clearText() {
        textArea.clear();
    }

    public void setText(String text) {
        textArea.appendText(text + "\n");
    }

    public void startProgress() {
        started = true;
        super.setTitle(PLEASE_WAIT);
        super.setCaption(PLEASE_WAIT);
        panelButton.setText(CANCEL);
        this.showAndWait();
    }

    public void stopProgress() {
        started = false;
        super.setTitle(COMPLETED);
        super.setCaption(COMPLETED);
        panelButton.setText("Close");
        //this.hide();
    }

    @Override
    public void setCaption(String title) {
        if (!started) return;
        super.setCaption(title);
    }
}
