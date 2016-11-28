package jnekoimagesdb.ui.md.albums;

import jnekoimagesdb.ui.md.dialogs.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jnekoimagesdb.ui.md.controls.LabeledBox;

public class AlbumsAddNewDialog extends SimpleDialog {
    private static AlbumsAddNewDialog mb = null;
    
    private final VBox rootPane = new VBox();
    private final Label text = new Label();
    private final Button 
            btnYes = new Button("Создать"),
            btnNo = new Button("Отмена");
    
    private final TextField
            albumName = new TextField("New album");
    
    private final TextArea
            albumText = new TextArea();
    
    private YesNoBoxResult result = YesNoBoxResult.NO;
    
    private AlbumsAddNewDialog() {
        super(500, 300, true);
        this.setContent(rootPane);
        this.setResizable(false);
        this.setOnCloseRequest(c -> {
            c.consume();
        });
        
        rootPane.setAlignment(Pos.CENTER);
        rootPane.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_messagebox");
        
        text.setAlignment(Pos.CENTER);
        text.setWrapText(true);
        text.getStyleClass().addAll("main_window_max_width", "main_window_messagebox_text");
        
        final VBox fieldsConatiner = new VBox();
        fieldsConatiner.setAlignment(Pos.CENTER);
        fieldsConatiner.getStyleClass().addAll("main_window_max_width", "main_window_null_pane", "main_window_yesnobox_btn_space");
        fieldsConatiner.getChildren().addAll(
                new LabeledBox("Название альбома", albumName),
                albumText
        );
        
        albumText.getStyleClass().addAll("main_window_max_width", "new_album_textarera_height", "new_album_textarera");
        albumName.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "new_album_textfield");

        albumText.setPromptText("Описание альбома");
        text.setText("Введите название и описание альбома.");
        
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
        rootPane.getChildren().addAll(text, fieldsConatiner, buttonConatiner);
    }

    public String getAlbumName() {
        return albumName.getText().trim();
    }
    
    public String getAlbumText() {
        return albumText.getText().trim();
    }
    
    public YesNoBoxResult getResult() {
        return result;
    }

    public YesNoBoxResult show(String msg) {
        centerOnScreen();
        showAndWait();
        return getResult();
    }
    
    public static AlbumsAddNewDialog getInstance() {
        if (mb == null) mb = new AlbumsAddNewDialog();
        return mb;
    }
}
