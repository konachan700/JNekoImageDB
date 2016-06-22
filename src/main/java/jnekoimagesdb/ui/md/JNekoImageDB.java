package jnekoimagesdb.ui.md;

import jnekoimagesdb.ui.md.dialogs.start.StartSplashScreen;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.Lang;
import jnekoimagesdb.ui.md.dialogs.start.StartDialog;
import jnekoimagesdb.ui.md.images.ImagesList;
import jnekoimagesdb.ui.md.menu.Menu;
import jnekoimagesdb.ui.md.menu.MenuGroup;
import jnekoimagesdb.ui.md.menu.MenuItem;
import jnekoimagesdb.ui.md.settings.PreviewTypes;
import jnekoimagesdb.ui.md.settings.Settings;

public class JNekoImageDB extends Application {
    public final static String
            CSS_FILE = new File("./style/style-gmd-main-window.css").toURI().toString();

    private final VBox 
            basesp          = new VBox();
            
    private final HBox 
            toolbox         = new HBox(),
            paginator_1     = new HBox();

    private String 
            databaseName    = "default";

    private final StartSplashScreen 
            splash = new StartSplashScreen();

    private void clearAll() {
        basesp.getChildren().clear();
        toolbox.getChildren().clear();
        paginator_1.getChildren().clear();
    }
    
    private void showAllImages(DSImageIDListCache.ImgType iID, long aID) {
        ImagesList.get().clearTags();
        basesp.getChildren().add(ImagesList.get());
        toolbox.getChildren().add(ImagesList.get().getPanel());
        paginator_1.getChildren().add(ImagesList.get().getPaginator());
        if (aID > 0) 
            ImagesList.get().setImageType(iID, aID); 
        else 
            ImagesList.get().setImageType(iID);
        ImagesList.get().regenerate();
        ImagesList.get().refresh();
    }
    
//    private void showTagsCloud() {
//        basesp.getChildren().add(XImg.getTabAllTags());
//        toolbox.getChildren().add(XImg.getTabAllTags().getTopPanel());
//        paginator_1.getChildren().add(XImg.getTabAllTags().getPaginator());
//        XImg.getTabAllTags().refresh();
//    }
    
//    private void showSettings() {
//        basesp.getChildren().add(Settings.getSettingBox());
//        toolbox.getChildren().add(Settings.getSettingBox().getPanel());
//    }

    private void showAlbCats() {
        basesp.getChildren().add(XImg.getTabAlbumImageList());
        toolbox.getChildren().add(XImg.getTabAlbumImageList().getPanel());
        paginator_1.getChildren().add(XImg.getTabAlbumImageList().getPaginator());
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
        
        StartDialog.showDialog();
        if (StartDialog.getDBName().isEmpty()) {
            Platform.exit(); 
            splash.hide();
            return;
        } else {
            databaseName = StartDialog.getDBName();
        }

        try {
            XImg.init(databaseName);
        } catch (Exception ex) {
            Logger.getLogger(JNekoImageDB.class.getName()).log(Level.SEVERE, null, ex);
            Platform.exit(); 
            return;
        }

        XImg.getTabAlbumImageList().setActionListener(c -> {
            clearAll();
            showAllImages(DSImageIDListCache.ImgType.InAlbum, c.getAlbumID());
        });
        
        ImagesList.get().setActionListener(c -> {
            clearAll();
            showAlbCats();
        });
        
        XImg.getTALog().setMaxSize(9999, 9999);
        XImg.getTALog().setPrefSize(9999, 9999);
        XImg.getTALog().setWrapText(true);

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
                        new MenuItem("Картинки", (c) -> {
                            clearAll();
                            showAllImages(DSImageIDListCache.ImgType.All, 0);
                        })//,
//                        new MenuItem("Не входящие ни в один альбом", (c) -> {
//                            clearAll();
//                            showAllImages(DSImageIDListCache.ImgType.NotInAnyAlbum, 0);
//                        }),
//                        new MenuItem("Не имеющие тегов", (c) -> {
//                            clearAll();
//                            showAllImages(DSImageIDListCache.ImgType.Notagged, 0);
//                        })
                ),
                new MenuGroup(
                        "Сервис", "menu_group_container_green", "header_icon_settings",
                        new MenuItem("Основные настройки", (c) -> {
                            clearAll();
                            basesp.getChildren().add(Settings.getSettingBox());
                            toolbox.getChildren().add(Settings.getSettingBox().getPanel());
                        }),
                        new MenuItem("Редактор тегов", (c) -> {
                            clearAll();
                            basesp.getChildren().add(XImg.getTabAllTags());
                            toolbox.getChildren().add(XImg.getTabAllTags().getTopPanel());
                            paginator_1.getChildren().add(XImg.getTabAllTags().getPaginator());
                            XImg.getTabAllTags().refresh();
                        }),
                        new MenuItem("Управление превью", (c) -> {
                            clearAll();
                            basesp.getChildren().add(PreviewTypes.get());
                            toolbox.getChildren().add(PreviewTypes.get().getPanel());
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
        appBox.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_appbox_pane", "main_window_separator_1");
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
