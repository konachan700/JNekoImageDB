
package jnekoimagesdb.ui.controls.menulist;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import jnekoimagesdb.ui.Lang;

public class MenuListScrollable extends ScrollPane {
    private final Map<String, MenuGroupItem> groups = new LinkedHashMap<>();
    private MenuGroupItemActionListener AL = null;
    
    private final VBox 
            rootVBox = new VBox(7);
    
    public MenuListScrollable() {
        super();
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("MenuList");
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED); 
        this.setContent(rootVBox); 
    }

    public void addGroup(String ID, String title, ImageView icon, String color) {
        final MenuGroupItem mi = new MenuGroupItem();
        mi.setGroupTitle(title); 
        mi.setGID(ID);
        mi.setBgColor(color);
        mi.setActionListener(AL);
        if (icon != null) mi.setGroupIcon(icon);
        mi.commit();
        groups.put(ID, mi);
        rootVBox.getChildren().add(mi);
    }
    
    public MenuGroupItem getGroup(String ID) {
        return groups.get(ID);
    }
    
    public void addItem(String GID, String ID, String text) {
        if (groups.get(GID) == null) return;
        groups.get(GID).addLabel(ID, text);
        groups.get(GID).commit();
    }
    
    public void setActionListener(MenuGroupItemActionListener l) {
        AL = l;
    }
}
