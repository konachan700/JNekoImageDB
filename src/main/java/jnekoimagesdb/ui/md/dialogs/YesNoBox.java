package jnekoimagesdb.ui.md.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class YesNoBox extends SimpleDialog {
    private static final YesNoBox mb = new YesNoBox();
    
    private final VBox rootPane = new VBox();
    private final Label text = new Label();
    private final Button 
            btnYes = new Button("Yes"),
            btnNo = new Button("No");
    
    private YesNoBoxResult result = YesNoBoxResult.NO;
    
    private YesNoBox() {
        super(500, 200, true);
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
        
        final HBox buttonConatiner = new HBox();
        buttonConatiner.setAlignment(Pos.CENTER);
        buttonConatiner.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane", "main_window_yesnobox_btn_space");
        
        btnYes.getStyleClass().addAll("main_window_messagebox_button");
        btnYes.setOnAction(c -> {
            result = YesNoBoxResult.YES;
            this.hide();
        });
        
        btnNo.getStyleClass().addAll("main_window_messagebox_button");
        btnNo.setOnAction(c -> {
            result = YesNoBoxResult.NO;
            this.hide();
        });
        
        buttonConatiner.getChildren().addAll(btnYes, btnNo);
        rootPane.getChildren().addAll(text, buttonConatiner);
    }
    
    public final void setButtonsText(String y, String n) {
        btnYes.setText(y);
        btnNo.setText(n);
    }
    
    public final void setText(String t) {
        text.setText(t);
    }
    
    public YesNoBoxResult getResult() {
        return result;
    }

    public static YesNoBoxResult show(String msg) {
        mb.setText(msg);
        mb.centerOnScreen();
        mb.showAndWait();
        return mb.getResult();
    }
    
    public static YesNoBoxResult show(String msg, String yesText, String noText) {
        mb.setButtonsText(yesText, noText);
        mb.setText(msg);
        mb.centerOnScreen();
        mb.showAndWait();
        return mb.getResult();
    }
}
