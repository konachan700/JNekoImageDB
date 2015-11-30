package jnekoimagesdb;

import albums.AlbumImageList;
import albums.AlbumsCategories;
import dataaccess.Crypto;
import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.DBEngine;
import dataaccess.Lang;
import dataaccess.SplittedFile;
import fsimagelist.FSImageList;
import imagelist.ImageList;
import imgfs.ImgFSCrypto;
import imgfsgui.InfiniteFileList;
import imgfsgui.TabAddImagesToDB;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import menulist.MenuGroupItem;
import menulist.MenuGroupItemActionListener;
import menulist.MenuLabel;
import menulist.MenuList;
import settings.Settings;

public class JNekoImageDB extends Application {
    public static final String TEMPORARY_DIR = "./temp/";

    private DragDelta DRD = new DragDelta();
    
    public static final StringBuilder
            LOG = new StringBuilder();
    
    private final VBox 
            mvbox           = new VBox(),
            toolbarvbox     = new VBox(),
            basevbox        = new VBox(), 
            base2vbox       = new VBox(),
            basesp          = new VBox();
            
    private final HBox 
            basehbox        = new HBox(),
            headerbox       = new HBox(), 
            toolbox         = new HBox(),
            logobox         = new HBox(),
            paginator_1     = new HBox();
    
    private final TextArea
            taLOG = new TextArea();
    
//    private final ImgFS
//            xImgFS = new ImgFS("test");
    
    private String 
            databaseName    = "default";
    
    private MenuGroupItem
            MGI = null;
    
    private final MenuList 
            ml              = new MenuList();
    
    private AlbumsCategories
            albumCats = null;
    
    private FSImageList 
            fileImgList     = null;
    
    private ImageList
            imgList         = null;
    
    private AlbumImageList
            albImgList      = null;
    
    private Settings
            settings        = null;
    
    private final Crypto
            mainCrypto      = new Crypto();
    
    private final ImgFSCrypto
            cryptoEx        = new ImgFSCrypto(() -> {
                
                
                return null;
            });
    
    private DBEngine
            SQL = null;
    
    private ImageEngine
            imgEn           = null;
    
    private final Timeline TMRLOG = new Timeline(new KeyFrame(Duration.millis(150), ae -> {
       if (taLOG.getText().length() < LOG.length()) {
           taLOG.setText(Lang.NullString);
           taLOG.appendText(LOG.toString());
           taLOG.setScrollTop(Double.MIN_VALUE);
       }
    }));
    
    private final MenuGroupItemActionListener
            menuAL = new MenuGroupItemActionListener() {
                @Override
                public void OnExpandGroup(boolean expanded, MenuGroupItem item) {
                    
                }

                @Override
                public void OnItemHover(MenuLabel l) {
                    
                }

                @Override
                public void OnItemClicked(MenuLabel l) {
                    clearAll();
                    
                    if (l.getGID().contentEquals("M02")) {
                        long lv = Long.parseLong(l.getID(), 10);
                        basesp.getChildren().add(albImgList);
                        albImgList.setAlbID(lv);
                        albImgList.getImageList().normalRefresh();
                        toolbox.getChildren().add(albImgList.getImageList().getTopPanel()); 
                        paginator_1.getChildren().add(albImgList.getImageList().getPaginator());
                        return;
                    }
                    
                    if (l.getID().contentEquals("M01-01")) showAllImages();
                    if (l.getID().contentEquals("M01-06")) showAllImages(DBWrapper.ALBUM_ID_WO_GROUPS);
                    if (l.getID().contentEquals("M01-05")) showFileDialog();
                    if (l.getID().contentEquals("M03-03")) showLog();
                    if (l.getID().contentEquals("M03-01")) showAlbCats();
                    if (l.getID().contentEquals("M03-02")) showSettings();
                    
                    if (l.getID().contentEquals("M03-04")) {
                        TabAddImagesToDB fl = new TabAddImagesToDB(cryptoEx, databaseName);
                        basesp.getChildren().add(fl.getList());
                        toolbox.getChildren().add(fl.getTopPanel());
                        paginator_1.getChildren().add(fl.getBottomPanel());
                        
//                        long t = System.currentTimeMillis();
//                        System.err.println("mem="+Runtime.getRuntime().totalMemory());
                        
//                        ArrayList<Path> al = new ArrayList<>(150000);
//                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("G:\\#TEMP02\\Images\\Danbooru.p1\\danbooru_simple_bg"))) {
//                            for (Path p : stream) {
//                                al.add(p);
//                            }
//                            stream.close();
//                        } catch (IOException ex) {
//
//                        }
//                        getFilesCount(new File("G:\\#TEMP02\\Images\\Danbooru.p1\\danbooru_simple_bg"));
//                        System.err.println(/*"p.sz="+al.size()+*/"; t="+(System.currentTimeMillis() - t));
//                        System.err.println("mem="+Runtime.getRuntime().totalMemory());
                    }
                }
            };
    
//    private int getFilesCount(File f) throws IOException {
//        final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(f.getAbsolutePath()));
//        int counter = 0;
//        for (Path p : stream) counter++;
//        stream.close();
//        return counter;
//    }
    
    private void clearAll() {
        basesp.getChildren().clear();
        toolbox.getChildren().clear();
        paginator_1.getChildren().clear();
    }
    
    private void showAllImages() {
        showAllImages(0);
    }
    
    private void showSettings() {
        basesp.getChildren().add(settings);
        
        
    }
    
    private void showAllImages(long id) {
        basesp.getChildren().add(imgList);
        imgList.setAlbimID(id);
        imgList.normalRefresh();
        toolbox.getChildren().add(imgList.getTopPanel()); 
        paginator_1.getChildren().add(imgList.getPaginator());
    }
    
    private void showFileDialog() {
        try {
            if (fileImgList.isStart()) fileImgList.setPath(new File(".").getCanonicalPath());
            paginator_1.getChildren().add(fileImgList.getPaginator());
            toolbox.getChildren().add(fileImgList.getTopPanel());  
            basesp.getChildren().add(fileImgList);
        } catch (IOException ex) {}
    }
    
    private void showAlbCats() {
        albumCats.RefreshAll();
        basesp.getChildren().add(albumCats);
        //toolbox.getChildren().add(albumCats.getToolbox());
    }
    
    private void showLog() {
        basesp.getChildren().add(taLOG);
        taLOG.setScrollTop(65535);
    }
    
    
    @Override
    public void start(Stage primaryStage) {
        new File(SplittedFile.DATABASE_FOLDER).mkdir();
        new File(JNekoImageDB.TEMPORARY_DIR).mkdir();
        
//        try {
//            xImgFS.init();
//        } catch (IOException ex) {
//            Logger.getLogger(JNekoImageDB.class.getName()).log(Level.SEVERE, null, ex);
//            return;
//        }
        
        if (!mainCrypto.genSecureRandomSalt()) {
            System.err.println(Lang.JNekoImageDB_no_salt_file);
            Platform.exit(); return;
        }
        
        if (!mainCrypto.genMasterKey()) {
            System.err.println(Lang.JNekoImageDB_no_master_key);
            Platform.exit(); return;
        }
        
        if (!mainCrypto.genMasterKeyAES()) {
            System.err.println(Lang.JNekoImageDB_no_crypt_support);
            Platform.exit(); return;
        }
        DBWrapper.setCrypto(mainCrypto);
        
        try {
            cryptoEx.init(databaseName);
        } catch (Exception ex) {
            Logger.getLogger(JNekoImageDB.class.getName()).log(Level.SEVERE, null, ex);
            Platform.exit(); 
            return;
        }
        
        

        
        
        SQL = new DBEngine();
        if (SQL.Connect(SplittedFile.DATABASE_FOLDER + "fs") == -1) {
            System.err.println(Lang.JNekoImageDB_no_DB_connection);
            Platform.exit(); return;
        }
        DBWrapper.setSQLite(SQL);
        DBWrapper.DBWrapperTmrStart();
        
        TMRLOG.setCycleCount(Animation.INDEFINITE);
        TMRLOG.play();
        
        final Image logoImage = new Image(new File("./icons/logo6.png").toURI().toString());
        final ImageView imgLogoV = new ImageView(logoImage);
        
        imgEn = new ImageEngine(mainCrypto, SQL);
        DBWrapper.setImageEngine(imgEn);
                
        fileImgList = new FSImageList(mainCrypto, imgEn, SQL);
        imgList = new ImageList(imgEn, basesp);
        albImgList = new AlbumImageList(imgEn, basesp); 
        settings = new Settings(SQL);
        
        //L("Количество изображений в БД: "+imgEn.getImgCount()+" штук.");
        
        taLOG.setMaxSize(9999, 9999);
        taLOG.setPrefSize(9999, 9999);
        taLOG.setWrapText(true);
        taLOG.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        taLOG.getStyleClass().add("JNekoImageDB_taLOG");
        taLOG.textProperty().addListener((ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            taLOG.setScrollTop(Double.MAX_VALUE);
        });
        
        StackPane root = new StackPane();
        final Scene scene;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            StackPane root3m = GUITools.getWinGUI(this, primaryStage, DRD, root, mvbox, Lang.AppStyleCSS, GUITools.CLOSE_EXIT);
            scene = new Scene(root3m, 950, 650);
            scene.setFill(Color.TRANSPARENT);
             
        } else {
            scene = new Scene(root, 1336, 778);
        }
        
        mvbox.getChildren().add(toolbarvbox);
        mvbox.getChildren().add(basevbox);
        root.getChildren().add(mvbox);

        toolbox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        toolbox.getStyleClass().add("JNekoImageDB_toolbox");
        toolbox.setMaxWidth(9999);
        toolbox.setPrefWidth(9999);
        toolbox.setAlignment(Pos.BOTTOM_LEFT);

        headerbox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        headerbox.getStyleClass().add("JNekoImageDB_headerbox");
        headerbox.setMaxWidth(9999);
        
        logobox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        logobox.getStyleClass().add("JNekoImageDB_headerbox");
        logobox.setMaxSize(240, 64);
        logobox.setMinSize(240, 64);
        logobox.setPrefSize(240, 64);
        logobox.setAlignment(Pos.CENTER_RIGHT);
        logobox.getChildren().add(imgLogoV);
        
        headerbox.getChildren().add(toolbox);
        headerbox.getChildren().add(logobox);
        
        toolbarvbox.setPrefSize(9999, 70);
        toolbarvbox.setMaxSize(9999, 70);
        toolbarvbox.setMinSize(64, 70);
        toolbarvbox.setStyle("-fx-background-color: #000000;");
        
        toolbarvbox.getChildren().add(headerbox);

        paginator_1.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        paginator_1.getStyleClass().add("JNekoImageDB_paginator_1");
        paginator_1.setMaxSize(9999, 32);
        paginator_1.setPrefSize(9999, 32);
        paginator_1.setMinSize(32, 32);
        paginator_1.setAlignment(Pos.CENTER_RIGHT);

        basevbox.getChildren().add(basehbox);

        basesp.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        basesp.getStyleClass().add("JNekoImageDB_basesp");
        basesp.setPrefSize(9999, 9999);
        basesp.setMaxSize(9999, 9999);
        
        ml.getMenu().setActionListener(menuAL);
        
        ml.getMenu().addGroup("M01", Lang.JNekoImageDB_menu_title_main, null, "331111");
        ml.getMenu().addItem("M01", "M01-01", Lang.JNekoImageDB_menu_main_all_images);
        ml.getMenu().addItem("M01", "M01-06", Lang.JNekoImageDB_menu_main_all_images_wo_groups);
        //ml.getMenu().addItem("M01", "M01-04", "Параметрический поиск");
        ml.getMenu().addItem("M01", "M01-05", Lang.JNekoImageDB_menu_main_add_images);
        
        ml.getMenu().addGroup("M04", Lang.JNekoImageDB_menu_title_tags, null, "331133");
        ml.getMenu().addItem("M04", "M04-01", Lang.JNekoImageDB_menu_main_tagcloud);
        ml.getMenu().addItem("M04", "M04-02", Lang.JNekoImageDB_menu_main_fav_tags);
        ml.getMenu().addItem("M04", "M04-03", Lang.JNekoImageDB_menu_main_tags_parser);
        
        ml.getMenu().addGroup("M02", Lang.JNekoImageDB_menu_title_albums, null, "113311");
        //ml.getMenu().addItem("M02", "M02-01", "Избранное");
        
        ml.getMenu().addGroup("M03", Lang.JNekoImageDB_menu_title_settings, null, "111133");
        ml.getMenu().addItem("M03", "M03-01", Lang.JNekoImageDB_menu_settings_album_roots);
        ml.getMenu().addItem("M03", "M03-02", Lang.JNekoImageDB_menu_settings_main);
        ml.getMenu().addItem("M03", "M03-03", Lang.JNekoImageDB_menu_settings_logs);
        ml.getMenu().addItem("M03", "M03-04", "For test");
        
        MGI = ml.getMenu().getGroup("M02");
        DBWrapper.setMenuGroupItem2(MGI);
        
        albumCats = new AlbumsCategories(MGI);
        albumCats.RefreshAll();
        
        ml.setPrefSize(240, 9999);
        ml.setMaxSize(240, 9999);
        ml.setMinSize(240, 300);
        
        ml.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        ml.getStyleClass().add("JNekoImageDB_MenuList");
        
        base2vbox.getChildren().add(basesp);
        base2vbox.getChildren().add(paginator_1);
        
        basehbox.getChildren().add(base2vbox);
        basehbox.getChildren().add(ml);

        primaryStage.getIcons().add(new Image(new File("./icons/icon128.png").toURI().toString()));
        primaryStage.getIcons().add(new Image(new File("./icons/icon64.png").toURI().toString()));
        primaryStage.getIcons().add(new Image(new File("./icons/icon32.png").toURI().toString()));
        
        primaryStage.setMinWidth(840);
        primaryStage.setMinHeight(480);
        primaryStage.setTitle(Lang.JNekoImageDB_title);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        //L("procs="+Runtime.getRuntime().availableProcessors());
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) ResizeHelper.addResizeListener(primaryStage);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        launch(args);
    }
    
    public static final void L(String s) {
        final SimpleDateFormat DF = new SimpleDateFormat(Lang.DateTimeFormat);
        LOG.append("[").append(DF.format(new Date())).append("]\t");
        LOG.append(s).append("\n");
    }
}
