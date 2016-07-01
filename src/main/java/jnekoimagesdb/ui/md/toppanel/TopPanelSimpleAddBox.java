package jnekoimagesdb.ui.md.toppanel;

import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

public class TopPanelSimpleAddBox extends HBox {
    private final TopPanelSimpleAddBoxActionListener actListener;

    @SuppressWarnings("LeakingThisInConstructor")
    public TopPanelSimpleAddBox(String defaultText, TopPanelSimpleAddBoxActionListener al) {
        actListener = al;
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().addAll("panel_max_width", "panel_max_height", "panel_null_pane");

        final TextField txt = new TextField();
        txt.setPromptText(defaultText);
        txt.setAlignment(Pos.CENTER_LEFT);
        txt.getStyleClass().addAll("panel_max_width", "panel_max_height", "panel_search_text_box");
        txt.setOnKeyPressed((KeyEvent key) -> {
            if (key.getCode() == KeyCode.ENTER) {
                actListener.onAddNew(txt.getText().trim());
                txt.setText("");
            }
        });
        final TopPanelButton tpb1 = new TopPanelButton("panel_icon_add_new_1", "Добавить", c -> {
            actListener.onAddNew(txt.getText().trim());
            txt.setText("");
        });
        final TopPanelButton tpb2 = new TopPanelButton("panel_icon_save_1", "Сохранить", c -> {
            actListener.onSave();
        });
        this.getChildren().addAll(txt, tpb1, tpb2);
    }
}
