package settings;

import dataaccess.Lang;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class SettingsElementContainer extends HBox {
    public SettingsElementContainer(String text, Node element) {
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("SettingsElementContainer");
        this.setMaxSize(9999, 24);
        this.setPrefSize(9999, 24);
        this.setAlignment(Pos.CENTER_LEFT);
        
        final Label l = new Label(text);
        l.setMaxSize(256, 16);
        l.setMinSize(256, 16);
        l.setPrefSize(256, 16);
        l.setAlignment(Pos.CENTER_LEFT);
        l.getStyleClass().add("SEC_Label");
        
        this.getChildren().addAll(l, element);
    }
}
