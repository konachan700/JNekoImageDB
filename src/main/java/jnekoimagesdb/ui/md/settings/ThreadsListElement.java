package jnekoimagesdb.ui.md.settings;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import jiconfont.javafx.IconNode;

public class ThreadsListElement extends HBox {

    private final Label 
            text = new Label();
    
    public ThreadsListElement() {
        super();
        
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().addAll("albums_max_width", "previews_element_height", "previews_element_root_pane", "albums_element_root_pane_non_selected");
        
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().addAll("threads_list_element_icon");
        
        text.setAlignment(Pos.CENTER_LEFT);
        text.setWrapText(true);
        text.getStyleClass().addAll("albums_max_width", "albums_max_height", "previews_element_text");
        text.setText("");
        
        this.getChildren().addAll(
                iconNode,
                text
        );
    }

    public void SetText(String t) {
        text.setText(t);
    }
}
