package jnekoimagesdb;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class GUITools {
    

    
    public static void setFixedSize(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
        n.setMinSize(w, h);
    }
    
    public static void setMaxSize(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
    }
    
    public static VBox getSeparator() {
        VBox sep1 = new VBox();
        setMaxSize(sep1, 9999, 16);
        return sep1;
    }
    
    public static VBox getSeparator(double sz) {
        VBox sep1 = new VBox();
        setFixedSize(sep1, sz, sz);
        return sep1;
    }
}
