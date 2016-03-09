package jnekoimagesdb.ui.controls.elements;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import jnekoimagesdb.ui.GUITools;

public class SElementPair extends SFHBox {
    public SElementPair(Region p1, Region p2, int sz, int y, int p1XFixed) {
        super(sz, 128, 9999, y, y, "null_pairpane");
        this.getChildren().addAll(p1, p2);
        GUITools.setFixedSize(p1, p1XFixed, y);
    }
    
    public SElementPair(Node p1, Node p2, int sz, int y, int p1XFixed) {
        super(sz, 128, 9999, y, y, "null_pairpane");
        this.getChildren().addAll(p1, p2);
    }
    
    public SElementPair(Node p1, int sz, int y, int p1XFixed, Node ... p2) {
        super(sz, 128, 9999, y, y, "null_pairpane");
        this.getChildren().addAll(p1);
        this.getChildren().addAll(p2);
    }
    
    public SElementPair setAlign(Pos p) {
        this.setAlignment(p);
        return this;
    }
}
