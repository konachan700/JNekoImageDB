package jnekouilib.appmenu;

import java.util.ArrayList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AppMenuGroup extends VBox {
    private final ArrayList<AppMenuActionListener> serviceAL = new ArrayList<>();
    private final ArrayList<AppMenuItem> items = new ArrayList<>();
    private final AppMenuActionListener serviceALX = (action) -> {
        deselectAll();
        serviceAL.forEach(x -> {
            x.OnClick(action); 
        });
    };
    
    public AppMenuGroup() {
        super();
        this.getStyleClass().addAll("menuSeparator");
    }
    
    public AppMenuGroup(String groupName, String colorStyle, String iconStyle, AppMenuItem ... node) {
        super();
        
        this.getStyleClass().addAll("menuHeaderBox");
        this.setAlignment(Pos.TOP_LEFT);
                
        final Label header = new Label(groupName);
        super.getChildren().addAll(header);
        header.getStyleClass().addAll("menuHeader", colorStyle);
        
        for (AppMenuItem mi : node) {
            mi.registerServiceAL(serviceALX);
            items.add(mi);
            super.getChildren().add(mi);
        }
    }
    
    public void deselectAll() {
        items.forEach(c -> { 
            c.setSelected(false);
        });
    }
    
    protected final void registerServiceAL(AppMenuActionListener s) {
        serviceAL.add(s);
    }
}
