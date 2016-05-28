package jnekoimagesdb.ui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.ui.controls.PagedImageList;
import jnekoimagesdb.ui.controls.dialogs.DialogDBInitSelect;
import jnekoimagesdb.ui.controls.tabs.TabSettings;
import jnekoimagesdb.ui.md.menu.Menu;
import jnekoimagesdb.ui.md.menu.MenuGroup;
import jnekoimagesdb.ui.md.menu.MenuItem;

public class JNekoImageDB extends Application {
    public final static String
            CSS_FILE = new File("./style/style-gmd-main-window.css").toURI().toString();

    private final VBox 
            basesp          = new VBox();
            
    private final HBox 
            toolbox         = new HBox(),
            paginator_1     = new HBox();
    
    private final TabSettings
            tabSettings = new TabSettings();

    private String 
            databaseName    = "default";

    private final StartSplashScreen 
            splash = new StartSplashScreen();

    private void clearAll() {
        basesp.getChildren().clear();
        toolbox.getChildren().clear();
        paginator_1.getChildren().clear();
    }
    
    private void showAllImages(long aID) {
        XImg.getTabAllImages().clearTags();
        basesp.getChildren().add(XImg.getTabAllImages());
        toolbox.getChildren().add(XImg.getTabAllImages().getPanel());
        paginator_1.getChildren().add(XImg.getTabAllImages().getPaginator());
        XImg.getTabAllImages().setAlbumID(aID);
        XImg.getTabAllImages().regenerate();
        XImg.getTabAllImages().refresh();
    }
    
    private void showTagsCloud() {
        basesp.getChildren().add(XImg.getTabAllTags());
        toolbox.getChildren().add(XImg.getTabAllTags().getTopPanel());
        paginator_1.getChildren().add(XImg.getTabAllTags().getPaginator());
        XImg.getTabAllTags().refresh();
    }
    
    private void showSettings() {
        basesp.getChildren().add(tabSettings);
        tabSettings.refresh();
    }

    private void showAlbCats() {
        basesp.getChildren().add(XImg.getTabAlbumImageList());
        XImg.getTabAlbumImageList().refresh();
    }
    
    private void showLog() {
        basesp.getChildren().add(XImg.getTALog());
        XImg.getTALog().setScrollTop(Double.MAX_VALUE); 
    }
    
    @Override
    public void start(Stage primaryStage) {
        splash.show();
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        
        
        final DialogDBInitSelect ds = new DialogDBInitSelect();
        ds.showModal();
        if ((ds.getRetCode() == DialogDBInitSelect.DBSelectReturnCode.newDB) ||
                (ds.getRetCode() == DialogDBInitSelect.DBSelectReturnCode.existDB)) {
            databaseName = ds.getDBName();
            if (databaseName == null) {
                Platform.exit(); 
                return;
            }
        } else {
            Platform.exit(); 
            return;
        }
        
        try {
            XImg.init(databaseName);
        } catch (Exception ex) {
            Logger.getLogger(JNekoImageDB.class.getName()).log(Level.SEVERE, null, ex);
            Platform.exit(); 
            return;
        }

        final Image logoImage = new Image(new File("./icons/logo6.png").toURI().toString());
        final ImageView imgLogoV = new ImageView(logoImage);

        XImg.getTabAlbumImageList().setPanels(toolbox, paginator_1);
        XImg.getTabAlbumImageList().initDB();

        XImg.getTALog().setMaxSize(9999, 9999);
        XImg.getTALog().setPrefSize(9999, 9999);
        XImg.getTALog().setWrapText(true);
        
//        XImg.getTabAllTags().setActionListener((tags, notTags) -> {
//            clearAll();
//            XImg.getTabAllImages().setTagLists(tags, notTags);
//            basesp.getChildren().add(XImg.getTabAllImages());
//            toolbox.getChildren().add(XImg.getTabAllImages().getPanel());
//            paginator_1.getChildren().add(XImg.getTabAllImages().getPaginator());
//            XImg.getTabAllImages().setAlbumID(PagedImageList.IMAGES_ALL);
//            XImg.getTabAllImages().regenerate();
//            XImg.getTabAllImages().refresh();
//        });
        
        GUITools.setStyle(XImg.getTALog(), "JNekoImageDB", "taLOG");
        XImg.getTALog().textProperty().addListener((ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            XImg.getTALog().setScrollTop(Double.MAX_VALUE);
        });
        
        
        final Menu mn = new Menu(
                new MenuGroup(
                        "Картинки", "menu_group_container_red", "header_icon_images",
                        new MenuItem("Альбомы", (c) -> {
                            clearAll();
                            showAlbCats();
                        }),
                        new MenuItem("Последние загруженные", (c) -> {
                            clearAll();
                            showAllImages(PagedImageList.IMAGES_ALL);
                        }),
                        new MenuItem("Не входящие ни в один альбом", (c) -> {
                            clearAll();
                            showAllImages(PagedImageList.IMAGES_NOT_IN_ALBUM);
                        }),
                        new MenuItem("Не имеющие тегов", (c) -> {
                            clearAll();
                            showAllImages(PagedImageList.IMAGES_NOTAGGED);
                        })
                ),
                new MenuGroup(),
                new MenuGroup(
                        "Настройки", "menu_group_container_green", "header_icon_settings",
                        new MenuItem("Основные", (c) -> {
                            clearAll();
                            showSettings();
                        }),
                        new MenuItem("Редактор тегов", (c) -> {
                            clearAll();
                            showTagsCloud();
                        }),
                        new MenuItem("Логи", (c) -> {
                            clearAll();
                            showLog();
                        })
                )
        );
        
        final HBox rootPane = new HBox();
        rootPane.getStylesheets().add(CSS_FILE);
        rootPane.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane", "main_window_separator_1");
        
        final VBox menuBox = new VBox();
        menuBox.getStyleClass().addAll("main_window_menu_block_width", "main_window_max_height", "main_window_null_pane", "main_window_menu_block");
        menuBox.getChildren().addAll(mn);
        
        final VBox appBox = new VBox();
        appBox.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane", "main_window_separator_1");
        basesp.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_root_container");
        toolbox.getStyleClass().addAll("main_window_max_width", "main_window_toolbox_height", "main_window_toolbox_container");
        paginator_1.getStyleClass().addAll("main_window_max_width", "main_window_paginator_height", "main_window_paginator_container");
        
        appBox.getChildren().addAll(toolbox, basesp, paginator_1);
        rootPane.getChildren().addAll(appBox, menuBox);
        
        final Scene scene = new Scene(rootPane, 1100, 750);

        primaryStage.getIcons().add(new Image(new File("./style/icons/icon128.png").toURI().toString()));
        primaryStage.getIcons().add(new Image(new File("./style/icons/icon64.png").toURI().toString()));
        primaryStage.getIcons().add(new Image(new File("./style/icons/icon32.png").toURI().toString()));
        
        primaryStage.setOnHiding((WindowEvent event) -> {
            XImg.dispose();
            Platform.exit(); 
        });
        
        primaryStage.setMinWidth(840);
        primaryStage.setMinHeight(480);
        primaryStage.setTitle(Lang.JNekoImageDB_title);
        primaryStage.setScene(scene);
        
        splash.hide();
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        launch(args);
    }
}
