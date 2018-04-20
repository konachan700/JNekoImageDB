package jnekouilib.panel;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class PanelSearch extends HBox {
    private final PanelSearchActionListener actListener;

    @SuppressWarnings("LeakingThisInConstructor")
    public PanelSearch(String defaultText, PanelSearchActionListener al) {
        actListener = al;

        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().addAll("max_width", "max_height", "panel_null_pane");

        final TextField txt = new TextField();
        txt.setAccessibleText(defaultText);
        txt.setAlignment(Pos.CENTER_LEFT);
        txt.getStyleClass().addAll("max_height", "panel_search_text_box");
        txt.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            actListener.OnSearch(newValue); 
        });

        final PanelButton tpb = new PanelButton("panel_search_icon", "Search", c -> {
            actListener.OnSearch(txt.getText().trim());
        });
        super.getChildren().addAll(txt, tpb);
    }
}
