package jnekoimagesdb.ui.md.controls;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LabeledBox extends HBox {
    private final Label 
            label = new Label();
    
    public LabeledBox(String text, Node element) {
        super();
        label.setAlignment(Pos.CENTER_RIGHT); 
        label.getStyleClass().addAll("main_window_labeled_box_text");
        label.setText(text);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().addAll("main_window_max_width", "main_window_null_pane", "main_window_labeled_box_pane");
        this.getChildren().addAll(label, element);
    }
}
