package utils;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    public static Set<String> createSet(String ... s) {
        return Arrays.asList(s).stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static void bindPaneSizes(Pane p1, Pane p2) {
        p1.maxHeightProperty().bind(p2.maxHeightProperty());
        p1.minHeightProperty().bind(p2.minHeightProperty());
        p1.prefHeightProperty().bind(p2.prefHeightProperty());
        p1.maxWidthProperty().bind(p2.maxWidthProperty());
        p1.minWidthProperty().bind(p2.minWidthProperty());
        p1.prefWidthProperty().bind(p2.prefWidthProperty());
    }

    public static void setBlur(Pane p) {
        final ColorAdjust adj = new ColorAdjust(0, -0.9, -0.5, 0);
        final GaussianBlur blur = new GaussianBlur(22); // 55 is just to show edge effect more clearly.
        adj.setInput(blur);
        p.setEffect(adj);
    }

    public static void removeBlur(Pane p) {
        p.setEffect(null);
    }
}
