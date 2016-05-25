package jnekoimagesdb.ui.controls.elements;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jnekoimagesdb.ui.GUITools;

public class SPopupMenuButton extends Button {
    private static final Image
            menuIcon = GUITools.loadIcon("menu-32");
    
    private final ContextMenu 
            contextMenu = new ContextMenu();


    public SPopupMenuButton(int size) {
        super("", new ImageView(menuIcon));
    }

    public SPopupMenuButton(int size, String styleName) {
        super("", new ImageView(menuIcon));
        init(size, styleName);
    }

    private void init(int size, String styleName) {
        GUITools.setStyle(this, "GUIElements", styleName);
        GUITools.setFixedSize(this, size, size);
        this.setAlignment(Pos.CENTER);
        this.setOnMouseClicked((c) -> {
            contextMenu.show(this, c.getScreenX()-c.getX(), c.getSceneY()-c.getY()+this.getHeight());
        });
    }
    
    public void addMenuItem(String title, EventHandler<ActionEvent> al) {
        final MenuItem mi = new MenuItem();
        mi.setText(title);
        mi.setOnAction(al);
        contextMenu.getItems().add(mi);
    }
    
    public void addSeparator() {
        contextMenu.getItems().add(new SeparatorMenuItem());
    }
    
    public void setIcon(Image icon) {
        this.setGraphic(new ImageView(icon));
    }
}