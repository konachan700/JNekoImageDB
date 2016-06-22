package jnekoimagesdb.ui.md.controls;

import java.io.File;
import javafx.scene.layout.VBox;

public class HSeparator extends VBox {
    public final static String
            CSS_FILE = new File("./style/style-gmd-main-window.css").toURI().toString();
        
    public HSeparator() {
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("main_window_max_width", "main_window_hseparator_height", "main_window_vseparator");
    }
}
