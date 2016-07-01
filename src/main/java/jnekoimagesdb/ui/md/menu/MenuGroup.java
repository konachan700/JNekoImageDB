package jnekoimagesdb.ui.md.menu;

import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;

public class MenuGroup extends VBox {
    private final ArrayList<MenuActionListener> serviceAL = new ArrayList<>();
    private final ArrayList<MenuItem> items = new ArrayList<>();
    private final MenuActionListener serviceALX = (action) -> {
        deselectAll();
        serviceAL.forEach(x -> {
            x.OnClick(action); 
        });
    };
    
    public MenuGroup() {
        super();
        this.getStyleClass().addAll("menu_separator", "menu_max_width");
    }
    
    public MenuGroup(String groupName, String colorStyle, String iconStyle, MenuItem ... node) {
        super();
        
        this.getStyleClass().addAll("menu_group_container", "menu_max_width", colorStyle);
        this.setAlignment(Pos.CENTER_LEFT);
                
        final Label header = new Label(groupName);
        this.getChildren().addAll(header);
        header.getStyleClass().addAll("menu_group_header", "menu_max_width", "menu_element_height", colorStyle);
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().add(iconStyle);
        header.setGraphic(iconNode); 
        
        for (MenuItem mi : node) {
            mi.registerServiceAL(serviceALX);
            items.add(mi);
            this.getChildren().add(mi);
        }
    }
    
    public void deselectAll() {
        items.forEach(c -> { 
            c.setSelected(false);
        });
    }
    
    protected final void registerServiceAL(MenuActionListener s) {
        serviceAL.add(s);
    }
}
