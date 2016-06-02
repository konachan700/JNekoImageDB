package jnekoimagesdb.ui.md.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class WaitBox extends SimpleDialog {
    private static final WaitBox mb = new WaitBox();
    
    private final VBox rootPane = new VBox();
    private final Label text = new Label();
    
    private WaitBox() {
        super(400, 170, true);
        this.setContent(rootPane);
        this.setResizable(false);
        this.setOnCloseRequest(c -> {
            c.consume();
        });
        
        rootPane.setAlignment(Pos.CENTER);
        rootPane.getStylesheets().add(CSS_FILE);
        rootPane.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_messagebox");
        
        text.setAlignment(Pos.CENTER);
        text.setWrapText(true);
        text.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_messagebox_text");

        rootPane.getChildren().addAll(text);
    }
    
    public final void setText(String t) {
        text.setText(t);
    }

    public static void show(String msg) {
        mb.setText(msg);
        mb.centerOnScreen();
        mb.showAndWait();
    }
    
    public static void hideMe() {
        mb.hide();
    }
}
