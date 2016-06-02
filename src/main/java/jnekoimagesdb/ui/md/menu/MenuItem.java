package jnekoimagesdb.ui.md.menu;

import java.io.File;
import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import jiconfont.javafx.IconNode;

public class MenuItem extends Label {
    public final static String
            CSS_FILE = new File("./style/style-gmd-menu.css").toURI().toString();
        
    private final MenuActionListener mil;
    private boolean selected = false;
    private final ArrayList<MenuActionListener> serviceAL = new ArrayList<>();

    public MenuItem(String text, MenuActionListener m) {
        super(text);
        mil = m;
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("menu_item", "menu_max_width", "menu_element_height");
        this.setAlignment(Pos.CENTER_LEFT);
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().add("menu_item_icon");
        this.setGraphic(iconNode); 
        this.setOnMouseClicked(c -> {
            if (!selected) {
                mil.OnClick(c);
                serviceAL.forEach(x -> {
                    x.OnClick(c); 
                });
                setSelected(true);
            }
        });
    }
    
    public MenuItem defaultSelected() {
        setSelected(true);
        return this;
    }
    
    public final void setSelected(boolean s) {
        selected = s;
        if (s) 
            this.getStyleClass().add("menu_item_selected");
        else
            this.getStyleClass().removeAll("menu_item_selected");
    }
    
    public final boolean isSelected() {
        return selected;
    }
    
    protected final void registerServiceAL(MenuActionListener s) {
        serviceAL.add(s);
    }
}
