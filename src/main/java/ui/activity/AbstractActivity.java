package ui.activity;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import utils.Loggable;

public abstract class AbstractActivity extends VBox implements Loggable {

    public abstract void onShow();

    public AbstractActivity() {
        getStyleClass().addAll("null_pane", "max_width", "max_height");
    }

    public void addAll(Node... nodes) {
        getChildren().addAll(nodes);
    }
}
