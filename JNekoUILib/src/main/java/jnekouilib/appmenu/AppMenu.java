package jnekouilib.appmenu;

import java.util.ArrayList;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class AppMenu extends VBox {
    private final ArrayList<AppMenuGroup> items = new ArrayList<>();
    private final AppMenuActionListener serviceALX = (action) -> {
        items.forEach(c -> {
            c.deselectAll();
        });
    };
    
    public AppMenu() {
        super();
        this.setAlignment(Pos.TOP_LEFT);
    }
    
    public void addMenuGroups(AppMenuGroup ... mg) {
        super.getChildren().clear();        
        for (AppMenuGroup m : mg) {
            m.registerServiceAL(serviceALX);
            items.add(m);
            super.getChildren().add(m);
        }
    }
    
    public void deselectAll() {
        items.forEach(c -> { 
            c.deselectAll();
        });
    }
}
