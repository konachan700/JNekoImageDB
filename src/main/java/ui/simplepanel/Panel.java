package ui.simplepanel;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.Objects;

public class Panel extends HBox {
    public Panel(Node ... nodes) {
        super(4);
        getStyleClass().addAll("panel_pane", "max_width", "height_48px");
        Arrays.asList(nodes).stream()
                .filter(e -> Objects.nonNull(e))
                .forEach(e -> getChildren().add(e));
    }

    public static Node getSpacer() {
        final VBox vBox = new VBox();
        vBox.getStyleClass().addAll("null_pane", "max_width");
        return vBox;
    }

    public static Node getFixedSpacer() {
        final VBox vBox = new VBox();
        vBox.getStyleClass().addAll("null_pane", "width_48px");
        return vBox;
    }
}
