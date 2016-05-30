package jnekoimagesdb.ui.md.toppanel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class TopPanelMenuButton extends TopPanelButton{
    private final ContextMenu 
            contextMenu = new ContextMenu();
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TopPanelMenuButton() {
        super("panel_menu_icon", null);
        this.setOnMouseClicked((c) -> {
            contextMenu.show(this, c.getScreenX()-c.getX(), c.getScreenY()-c.getY()+this.getHeight());
        });
    }
    
    public MenuItem addMenuItem(String title, EventHandler<ActionEvent> al) {
        final MenuItem mi = new MenuItem();
        mi.setText(title);
        mi.setOnAction(al);
        contextMenu.getItems().add(mi);
        return mi;
    }
    
    public void remove(MenuItem mi) {
        contextMenu.getItems().remove(mi);
    }

    public void addSeparator() {
        contextMenu.getItems().add(new SeparatorMenuItem());
    }
}
