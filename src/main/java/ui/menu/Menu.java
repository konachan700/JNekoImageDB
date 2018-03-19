package ui.menu;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;

public class Menu extends VBox {
    private final static Image logoImage = new Image("/style/images/logo7.png");
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
        //getStylesheets().add(getClass().getResource("/style/css/menu.css").toExternalForm());
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
