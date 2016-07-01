package jnekoimagesdb.ui.md.toppanel;

import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TopPanelSearch extends HBox {
    private final TopPanelSearchActionListener actListener;

    @SuppressWarnings("LeakingThisInConstructor")
    public TopPanelSearch(String defaultText, TopPanelSearchActionListener al) {
        actListener = al;

        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().addAll("panel_max_width", "panel_max_height", "panel_null_pane");

        final TextField txt = new TextField();
        txt.setAccessibleText(defaultText);
        txt.setAlignment(Pos.CENTER_LEFT);
        txt.getStyleClass().addAll("panel_max_width", "panel_max_height", "panel_search_text_box");
        txt.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            actListener.OnSearch(newValue); 
        });

        final TopPanelButton tpb = new TopPanelButton("panel_search_icon", "Поиск", c -> {
            actListener.OnSearch(txt.getText().trim());
        });
        this.getChildren().addAll(txt, tpb);
    }
}
