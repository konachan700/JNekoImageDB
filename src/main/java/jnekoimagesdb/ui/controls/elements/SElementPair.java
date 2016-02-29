package jnekoimagesdb.ui.controls.elements;

import javafx.scene.layout.Region;
import jnekoimagesdb.ui.GUITools;

public class SElementPair extends SFHBox {
    public SElementPair(Region p1, Region p2, int sz, int y, int p1XFixed) {
        super(sz, 128, 9999, y, y, "null_pairpane");
        this.getChildren().addAll(p1, p2);
        GUITools.setFixedSize(p1, p1XFixed, y);
    }
}
