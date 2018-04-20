package jnekouilib.windows;

import java.io.File;

import jnekouilib.appmenu.AppMenu;
import jnekouilib.appmenu.AppMenuGroup;
import jnekouilib.fragment.Fragment;
import jnekouilib.fragment.FragmentHost;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;

public class UIDialog extends Stage {
    private final FragmentHost
            rootFragment = new FragmentHost();

    private final AppMenu
            appMenu = new AppMenu();
    
    private final Scene 
            scene;
    
    private final VBox
            rootPane = new VBox(),
            contentPane = new VBox(), 
            menuBox = new VBox(),
            logoBox = new VBox();
    
    private final HBox
            headerBox = new HBox(),
            rootBox = new HBox();
    
    public UIDialog(int width, int height, boolean isHeaderPresent, boolean isMenuPresent, String title) {
        super();
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        
        scene = new Scene(rootPane, width, height);
        scene.getStylesheets().add("/styles/window.css");

        rootPane.getStyleClass().addAll("maxHeight", "maxWidth", "dialog_root");
        headerBox.getStyleClass().addAll("dialog_headerBox", "maxWidth");
        rootBox.getStyleClass().addAll("maxHeight", "maxWidth");
        appMenu.getStyleClass().addAll("windowMenu");
        
        rootFragment.setPanelHost(headerBox); 
        
        if (isMenuPresent)
            rootBox.getChildren().addAll(menuBox, contentPane);
        else 
            rootBox.getChildren().addAll(contentPane);
        
        if (isHeaderPresent)
            contentPane.getChildren().addAll(headerBox, rootFragment);
        else
            contentPane.getChildren().addAll(rootFragment);
        
        menuBox.getChildren().addAll(logoBox, appMenu);
        rootPane.getChildren().addAll(rootBox);

        super.setMinWidth(width);
        super.setMinHeight(height);
        super.setTitle(title);
        super.setScene(scene);
    }
    
    public UIDialog(int width, int height, String title) {
        this(width, height, true, true, title);
    }
    
    public void addLogo(Node logo) {
        logoBox.getChildren().clear();
        logoBox.getChildren().add(logo);
    }
    
    public void addLogoFromResources(String logo) {
        logoBox.getChildren().clear();
        logoBox.getChildren().add(new ImageView(new Image(getClass().getResourceAsStream(logo))));
    }

    public void addLogoFromFile(File logo) {
        logoBox.getChildren().clear();
        logoBox.getChildren().add(new ImageView(new Image(logo.toURI().toString())));
    }
    
    public void addMenu(AppMenuGroup ... mg) {
        getAppMenu().addMenuGroups(mg); 
    }
    
    public void back() {
        rootFragment.back();
    }
    
    public void showFragment(Fragment f, boolean isClear) {
        rootFragment.showFragment(f, isClear); 
    }

    public FragmentHost getRootFragment() {
        return rootFragment;
    }

    public Pane getPanelHost() {
        return headerBox;
    }

    public AppMenu getAppMenu() {
        return appMenu;
    }
    
    public void addCustomStyle(String style) {
        scene.getStylesheets().add(style);
    }
    
    public void removeCustomStyle(String style) {
        scene.getStylesheets().remove(style);
    }
}
