package jnekoimagesdb.ui.md.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MessageBox extends SimpleDialog {
    private static final MessageBox mb = new MessageBox();
    
    private final VBox rootPane = new VBox();
    private final Label text = new Label();
    private final Button btn = new Button("OK");
    
    private MessageBox() {
        super(500, 200, true);
        this.setContent(rootPane);
        this.setResizable(false);
        this.setOnCloseRequest(c -> {
            c.consume();
        });
        
        rootPane.setAlignment(Pos.CENTER);
        rootPane.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_messagebox");
        
        text.setAlignment(Pos.CENTER);
        text.setWrapText(true);
        text.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_messagebox_text");
        
        btn.getStyleClass().addAll("main_window_messagebox_button");
        btn.setOnAction(c -> {
            this.hide();
        });
        
        rootPane.getChildren().addAll(text, btn);
    }
    
    public final void setText(String t) {
        text.setText(t);
    }

    public static void show(String msg) {
        mb.setText(msg);
        mb.centerOnScreen();
        mb.showAndWait();
    }
}
