package jnekouilib.panel;

import java.util.Collection;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Panel extends HBox {
    public Panel() {
        super();
        this.getStyleClass().addAll("maxWidth", "topPanel");
        this.setAlignment(Pos.CENTER_LEFT);
    }
    
    public void addSeparator() {
        final VBox sep1 = new VBox();
        sep1.getStyleClass().addAll("maxWidth");
        this.getChildren().add(sep1);
    }
    
    public void addFixedSeparator() {
        final VBox sep1 = new VBox();
        sep1.getStyleClass().addAll("topPanelSeparator");
        this.getChildren().add(sep1);
    }
    
    public void addFixedSeparator(int position) {
        final VBox sep1 = new VBox();
        sep1.getStyleClass().addAll("topPanelSeparator");
        this.getChildren().add(position, sep1);
    }
    
    public void addNode(Node element, int position) {
        this.getChildren().add(position, element);
    }
    
    public void addNode(Node element) {
        this.getChildren().add(element);
    }
    
    public void addNodes(Node ... element) {
        this.getChildren().addAll(element);
    }
    
    public void removeNode(Node element) {
        this.getChildren().remove(element);
    }
    
    public void clear() {
        this.getChildren().clear();
    }
    
    public void setAll(Collection col) {
        this.getChildren().setAll(col);
    }
}
