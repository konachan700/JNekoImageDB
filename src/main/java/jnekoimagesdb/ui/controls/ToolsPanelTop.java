package jnekoimagesdb.ui.controls;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.STextField;

public class ToolsPanelTop extends HBox {
    public static final int 
            BUTTON_SIZE = 64,
            SEPARATOR_SIZE = 16;
    
    public static interface TopPanelButtonActionListener {
        public void OnClick(PanelButtonCodes buttonID);
    }
    
    public static interface TopPanelSearchActionListener {
        public void OnSearch(String search);
    }
    
    public static class SPanelSearchBlock extends HBox {
        private static final Image
                searchIcon = GUITools.loadIcon("search-48");
        
        private final TopPanelSearchActionListener actListener;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public SPanelSearchBlock(TopPanelSearchActionListener al) {
            actListener = al;
            
            this.setAlignment(Pos.CENTER_LEFT);
            GUITools.setStyle(this, "GUIElements", "search_root");
            GUITools.setMaxSize(this, 9999, BUTTON_SIZE);
                        
            final STextField txt = new STextField(BUTTON_SIZE, "search_txt").setHelpText("Введите тег для поиска...");
            txt.setAlignment(Pos.CENTER_LEFT);
            GUITools.setMaxSize(txt, 9999, BUTTON_SIZE);
            txt.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
                actListener.OnSearch(newValue); 
            });
            
            final TopPanelButton tpb = new TopPanelButton(searchIcon, PanelButtonCodes.buttonUnknown, c -> {
                actListener.OnSearch(txt.getText().trim());
            });
            this.getChildren().addAll(txt, GUITools.getSeparator(4), tpb);
        }
    }
    
    public static class SPanelPopupMenuButton extends Button {
        private static final Image
                menuIcon = GUITools.loadIcon("menu-32");

        private final ContextMenu 
                contextMenu = new ContextMenu();

        @SuppressWarnings("LeakingThisInConstructor")
        public SPanelPopupMenuButton() {
            super("", new ImageView(menuIcon));
            GUITools.setStyle(this, "TopPanel", "button");
            GUITools.setFixedSize(this, BUTTON_SIZE, BUTTON_SIZE);
            this.setAlignment(Pos.CENTER);
            this.setAlignment(Pos.CENTER);
            this.setOnMouseClicked((c) -> {
                contextMenu.show(this, c.getScreenX()-c.getX(), c.getScreenY()-c.getY()+this.getHeight());
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
    
    private static class TopPanelButton extends Button {
        private PanelButtonCodes xID = PanelButtonCodes.buttonUnknown;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public TopPanelButton(Image icon, PanelButtonCodes id, TopPanelButtonActionListener al) {
            super("", new ImageView(icon));
            xID = id;
            GUITools.setStyle(this, "TopPanel", "button");
            GUITools.setFixedSize(this, BUTTON_SIZE, BUTTON_SIZE);
            this.setAlignment(Pos.CENTER);
            this.setOnMouseClicked((c) -> {
                al.OnClick(xID); 
            });
        }
        
        public PanelButtonCodes getID() {
            return xID;
        }
    }
    
    public final TopPanelButtonActionListener
            actListener;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public ToolsPanelTop(TopPanelButtonActionListener al) {
        super(4);
        actListener = al;
        GUITools.setStyle(this, "TopPanel", "root_pane");
        this.setMaxHeight(BUTTON_SIZE);
        this.setMinHeight(BUTTON_SIZE);
        this.setPrefHeight(BUTTON_SIZE);
    }
    
    public void addFixedSeparator() {
        final VBox sep1 = new VBox();
        GUITools.setStyle(sep1, "TopPanel", "separator");
        GUITools.setFixedSize(sep1, SEPARATOR_SIZE, BUTTON_SIZE);
        this.getChildren().add(sep1);
    }
    
    public void addSeparator() {
        final VBox sep1 = new VBox();
        GUITools.setStyle(sep1, "TopPanel", "separator");
        GUITools.setMaxSize(sep1, 9999, BUTTON_SIZE);
        this.getChildren().add(sep1);
    }
    
    public void addButton(Image icon, PanelButtonCodes id) {
        final TopPanelButton tpb = new TopPanelButton(icon, id, actListener);
        this.getChildren().add(tpb);
    }
    
    public void addButton(Image icon, PanelButtonCodes id, String tooltip) {
        final TopPanelButton tpb = new TopPanelButton(icon, id, actListener);
        tpb.setTooltip(GUITools.createTT(tooltip));
        this.getChildren().add(tpb);
    }
    
    public void addMenuButton(SPanelPopupMenuButton btn) {
        this.getChildren().add(btn);
    }
    
    public void addSearch(SPanelSearchBlock sb) {
        this.getChildren().add(sb);
    }
    
    public void clearAll() {
        this.getChildren().clear();
    }
}
