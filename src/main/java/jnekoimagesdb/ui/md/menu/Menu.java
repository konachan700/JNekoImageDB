package jnekoimagesdb.ui.md.menu;

import java.io.File;
import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class Menu extends VBox {
    private final static String
            CSS_FILE = new File("./style/style-gmd-menu.css").toURI().toString();
    
    private final static Image logoImage = new Image(new File("./style/icons/logo7.png").toURI().toString());
    private final ImageView imgLogoNode = new ImageView(logoImage);
    
    private final ArrayList<MenuGroup> items = new ArrayList<>();
    private final MenuActionListener serviceALX = (action) -> {
        items.forEach(c -> {
            c.deselectAll();
        });
    };
    
    public Menu(MenuGroup ... mg) {
        super();
        
        this.setAlignment(Pos.TOP_LEFT);
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("menu_null_container", "menu_max_width", "menu_max_height");
        
        final Label logo = new Label();
        logo.setAlignment(Pos.TOP_RIGHT);
        logo.getStyleClass().addAll("menu_app_logo", "menu_max_width");
        logo.setGraphic(imgLogoNode);
        this.getChildren().add(logo);
        
        for (MenuGroup m : mg) {
            m.registerServiceAL(serviceALX);
            items.add(m);
            this.getChildren().add(m);
        }
    }
    
    public void deselectAll() {
        items.forEach(c -> { 
            c.deselectAll();
        });
    }
}
