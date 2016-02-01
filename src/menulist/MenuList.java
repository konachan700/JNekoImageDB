package menulist;

import jnekoimagesdb.Lang;
import javafx.scene.layout.VBox;

public class MenuList extends VBox {
    private final VBox
            scrollable = new VBox();

    final MenuListScrollable 
            ms = new MenuListScrollable();
    
    public MenuList() {
        ms.setPrefHeight(Integer.MAX_VALUE);
        ms.setPrefWidth(Integer.MAX_VALUE);
        
        scrollable.getChildren().add(ms);
        this.getChildren().add(scrollable);
        
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("MenuList_pane");
    }
    
    public final MenuListScrollable getMenu() {
        return ms;
    }
}
