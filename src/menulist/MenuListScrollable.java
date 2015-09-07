
package menulist;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MenuListScrollable extends ScrollPane {
    private final Map<String, MenuGroupItem> groups = new LinkedHashMap<>();
    private MenuGroupItemActionListener AL = null;
    
    private final VBox 
            rootVBox = new VBox(7);
    
    public MenuListScrollable() {
        super();
        this.getStylesheets().add(getClass().getResource("ProgressBar.css").toExternalForm());
        this.getStyleClass().add("MenuList");
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.ALWAYS); 
        this.setContent(rootVBox); 
        this.setPadding(new Insets(0, 3, 0, 0));
        
    }

    public void addGroup(String ID, String title, ImageView icon, String color) {
        final MenuGroupItem mi = new MenuGroupItem();
        mi.setGroupTitle(title); 
        mi.setGID(ID);
        mi.setBgColor(color);
        mi.setActionListener(AL);
        if (icon != null) mi.setGroupIcon(icon);
        mi.Commit();
        groups.put(ID, mi);
        rootVBox.getChildren().add(mi);
    }
    
    public MenuGroupItem getGroup(String ID) {
        return groups.get(ID);
    }
    
    public void addItem(String GID, String ID, String text) {
        if (groups.get(GID) == null) return;
        groups.get(GID).addLabel(ID, text);
        groups.get(GID).Commit();
    }
    
    public void setActionListener(MenuGroupItemActionListener l) {
        AL = l;
    }
}
