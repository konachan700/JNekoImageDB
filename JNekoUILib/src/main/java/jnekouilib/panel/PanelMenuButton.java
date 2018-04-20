package jnekouilib.panel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
//import jiconfont.javafx.IconNode;

public class PanelMenuButton extends PanelButton{
    private final ContextMenu 
            contextMenu = new ContextMenu();
    
    private PanelMenuButtonVisible
            mVis = null;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public PanelMenuButton() {
        super("topPanelButtonMenuIcon", "Menu", null);
        contextMenu.getStyleClass().addAll("popupMenuRoot");
        this.setOnMouseClicked((c) -> {
            if (mVis != null) 
                contextMenu.getItems().forEach(menu -> {
                    menu.setVisible(mVis.isVisible(menu)); 
                });
            contextMenu.show(this, c.getScreenX()-c.getX(), c.getScreenY()-c.getY()+this.getHeight());
        });
    }
    
    public MenuItem addMenuItem(String title, EventHandler<ActionEvent> al) {
        final MenuItem mi = new MenuItem();
        mi.setText(title);
        mi.setOnAction(al);
        mi.getStyleClass().addAll("popupMenuItem");
        contextMenu.getItems().add(mi);
        return mi;
    }
    
    public MenuItem addMenuItemBold(String title, EventHandler<ActionEvent> al) {
        final MenuItem mi = new MenuItem();
        mi.setText(title);
        mi.setOnAction(al);
        mi.getStyleClass().addAll("popupMenuItemBold");
        contextMenu.getItems().add(mi);
        return mi;
    }
    
//    public MenuItem addMenuItemBold(String imageStyle, String title, EventHandler<ActionEvent> al) {
//        final MenuItem mi = addMenuItemBold(title, al);
//        final IconNode iconNode = new IconNode();
//        iconNode.getStyleClass().addAll("menuitem_icon_color", imageStyle);
//        mi.setGraphic(iconNode);
//        return mi;
//    }
    
    public void remove(MenuItem mi) {
        contextMenu.getItems().remove(mi);
    }

    public void addSeparator() {
        contextMenu.getItems().add(new SeparatorMenuItem());
    }
    
    public void setVisibleAL(PanelMenuButtonVisible al) {
        mVis = al;
    }
}
