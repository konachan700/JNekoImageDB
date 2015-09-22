package jnekoimagesdb;

import albums.AlbumImageList;
import albums.AlbumsCategories;
import dataaccess.Crypto;
import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.DBEngine;
import dataaccess.SplittedFile;
import fsimagelist.FSImageList;
import imagelist.ImageList;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
//    private class DragDelta { 
//        double x, y; 
//    }
    
//    private final DragDelta 
//            DRD = new DragDelta();
    
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
    
    private DBEngine
            SQL = null;
    
    private ImageEngine
            imgEn           = null;
    
    private final Timeline TMRLOG = new Timeline(new KeyFrame(Duration.millis(150), ae -> {
       if (taLOG.getText().length() < LOG.length()) {
           taLOG.setText("");
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
                        albImgList.getImageList().setAlbimID(lv);
                        albImgList.getImageList().normalRefresh();
                        toolbox.getChildren().add(albImgList.getImageList().getTopPanel()); 
                        paginator_1.getChildren().add(albImgList.getImageList().getPaginator());
                        return;
                    }
                    
                    if (l.getID().contentEquals("M01-01")) showAllImages();
                    if (l.getID().contentEquals("M01-05")) showFileDialog();
                    if (l.getID().contentEquals("M03-03")) showLog();
                    if (l.getID().contentEquals("M03-01")) showAlbCats();
                    if (l.getID().contentEquals("M03-02")) showSettings();
                    
                    
                }
            };
    
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
        } catch (IOException ex) { }
    }
    
    private void showAlbCats() {
        albumCats.RefreshAll();
        basesp.getChildren().add(albumCats);
        toolbox.getChildren().add(albumCats.getToolbox());
    }
    
    private void showLog() {
        basesp.getChildren().add(taLOG);
        taLOG.setScrollTop(65535);
    }
    
    
    @Override
    public void start(Stage primaryStage) {
        new File(SplittedFile.DATABASE_FOLDER).mkdir();
        
        if (!mainCrypto.genSecureRandomSalt()) {
            System.err.println("Error #1: Cannot open some crypt files.");
            Platform.exit(); return;//while(true) {}
        }
        
        if (!mainCrypto.genMasterKey()) {
            System.err.println("Error #2: Cannot open some crypt files.");
            Platform.exit(); return;//while(true) {}
        }
        
        if (!mainCrypto.genMasterKeyAES()) {
            System.err.println("Error #3: JVM not support some crypt function.");
            Platform.exit(); return;//while(true) {}
        }
        DBWrapper.setCrypto(mainCrypto);
        
        SQL = new DBEngine();
        if (SQL.Connect(SplittedFile.DATABASE_FOLDER + "fs") == -1) {
            System.err.println("Error #4: Cannot connect to DB.");
            Platform.exit(); return;
        }
        DBWrapper.setSQLite(SQL);
        DBWrapper.DBWrapperTmrStart();
        
        TMRLOG.setCycleCount(Animation.INDEFINITE);
        TMRLOG.play();
        
        final Image logoImage = new Image(JNekoImageDB.class.getResourceAsStream("logo6.png"));
        final ImageView imgLogoV = new ImageView(logoImage);
        
        imgEn = new ImageEngine(mainCrypto, SQL);
        DBWrapper.setImageEngine(imgEn);
                
        fileImgList = new FSImageList(mainCrypto, imgEn, SQL);
        imgList = new ImageList(imgEn, basesp);
        albImgList = new AlbumImageList(imgEn, SQL, basesp); 
        settings = new Settings(SQL);
        
        L("Количество изображений в БД: "+imgEn.getImgCount()+" штук.");
        
        taLOG.setMaxSize(9999, 9999);
        taLOG.setPrefSize(9999, 9999);
        taLOG.setWrapText(true);
        taLOG.getStylesheets().add(getClass().getResource("ProgressBar.css").toExternalForm());
        taLOG.getStyleClass().add("logbox");
        taLOG.textProperty().addListener((ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            taLOG.setScrollTop(Double.MAX_VALUE);
        });
        
        StackPane root = new StackPane();
        final Scene scene;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
//            final DropShadow ds = new DropShadow();
//            ds.setOffsetY(0f);
//            ds.setRadius(14f);
//            ds.setSpread(0.5f);
//            ds.setColor(Color.color(0.0f, 0.0f, 0.0f));
//            
//            final Label imageName = new Label("JNeko Image Database");
//            imageName.getStyleClass().add("appnamez");
//            imageName.setMaxSize(9999, 16);
//            imageName.setPrefSize(9999, 16);
//            imageName.setAlignment(Pos.CENTER_LEFT);
//            imageName.setEffect(ds);
//            
//            
//            HBox header_z = new HBox(6);
//            header_z.setMaxSize(9999, 32);
//            header_z.setPrefSize(9999, 32);
//            header_z.setMinSize(32, 32);
//            header_z.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
//            header_z.getStyleClass().add("header_z");
//            header_z.setAlignment(Pos.CENTER);
//            
//            header_z.setOnMousePressed((MouseEvent mouseEvent) -> {
//                DRD.x = primaryStage.getX() - mouseEvent.getScreenX();
//                DRD.y = primaryStage.getY() - mouseEvent.getScreenY();
//            });
//            header_z.setOnMouseDragged((MouseEvent mouseEvent) -> {
//                primaryStage.setX(mouseEvent.getScreenX() + DRD.x);
//                primaryStage.setY(mouseEvent.getScreenY() + DRD.y);
//            });
//            
//            final Button close = new Button("", new ImageView(new Image(getClass().getResourceAsStream("close.png"))));
//            _s2(close, 16, 16);
//            close.setOnMouseClicked((MouseEvent event) -> {
//                Platform.exit();
//            });
//            
//            final Button expand = new Button("", new ImageView(new Image(getClass().getResourceAsStream("up.png"))));
//            _s2(expand, 16, 16);
//            expand.setOnMouseClicked((MouseEvent event) -> {
//                primaryStage.setMaximized(!primaryStage.isMaximized());
//            });
//            
//            final Button unexpand = new Button("", new ImageView(new Image(getClass().getResourceAsStream("dwn.png"))));
//            _s2(unexpand, 16, 16);
//            unexpand.setOnMouseClicked((MouseEvent event) -> {
//                primaryStage.setIconified(true);
//            });
//            
//            final Button iconx = new Button("", new ImageView(new Image(getClass().getResourceAsStream("icon.png"))));
//            _s2(iconx, 16, 16);
//            
//            header_z.getChildren().addAll(getSeparator1(2), iconx, imageName, unexpand, expand, close, getSeparator1(8));
//            
//            mvbox.getChildren().add(header_z);
//            primaryStage.initStyle(StageStyle.TRANSPARENT);
//            
//            StackPane root2m = new StackPane();
//            root2m.getChildren().add(root);
//            root2m.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
//            root2m.getStyleClass().add("border_z");
//            
//            StackPane root3m = new StackPane();
//            root3m.setBackground(Background.EMPTY);
//            root3m.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
//            root3m.getStyleClass().add("border_a");
//            root3m.getChildren().add(root2m);
//            
//            root2m.setEffect(ds);
            
            StackPane root3m = GUITools.getWinGUI(this, primaryStage, DRD, root, mvbox);
            scene = new Scene(root3m, 950, 650);
            scene.setFill(Color.TRANSPARENT);
             
        } else {
            scene = new Scene(root, 950, 650);
        }
        
        mvbox.getChildren().add(toolbarvbox);
        mvbox.getChildren().add(basevbox);
        root.getChildren().add(mvbox);

        toolbox.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        toolbox.getStyleClass().add("toolbox");
        toolbox.setMaxWidth(9999);
        toolbox.setPrefWidth(9999);
        toolbox.setPadding(new Insets(0,0,0,2));
        toolbox.setAlignment(Pos.BOTTOM_LEFT);

        headerbox.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        headerbox.getStyleClass().add("headerbox");
        headerbox.setMaxWidth(9999);
        
        logobox.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        logobox.getStyleClass().add("headerbox");
        logobox.setMaxSize(239, 64);
        logobox.setMinSize(239, 64);
        logobox.setPrefSize(239, 64);
        logobox.setAlignment(Pos.CENTER_RIGHT);
        logobox.getChildren().add(imgLogoV);
        
        headerbox.getChildren().add(toolbox);
        headerbox.getChildren().add(logobox);
        
        toolbarvbox.setPrefSize(9999, 70);
        toolbarvbox.setMaxSize(9999, 70);
        toolbarvbox.setMinSize(64, 70);
        toolbarvbox.setStyle("-fx-background-color: #000000;");
        
        toolbarvbox.getChildren().add(headerbox);

        paginator_1.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        paginator_1.getStyleClass().add("paginator_1");
        paginator_1.setMaxSize(9999, 32);
        paginator_1.setPrefSize(9999, 32);
        paginator_1.setMinSize(32, 32);
        paginator_1.setAlignment(Pos.CENTER_RIGHT);


        
        basevbox.getChildren().add(basehbox);

        basesp.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        basesp.getStyleClass().add("BaseBox");
        basesp.setPrefSize(9999, 9999);
        basesp.setMaxSize(9999, 9999);
        
        ml.getMenu().setActionListener(menuAL);
        
        ml.getMenu().addGroup("M01", "Основное", null, "331111");
        ml.getMenu().addItem("M01", "M01-01", "Все картинки");
        ml.getMenu().addItem("M01", "M01-02", "Облако тегов");
        ml.getMenu().addItem("M01", "M01-03", "Избранные теги");
        ml.getMenu().addItem("M01", "M01-04", "Параметрический поиск");
        ml.getMenu().addItem("M01", "M01-05", "Добавить картинки");
        
        ml.getMenu().addGroup("M02", "Альбомы", null, "113311");
        //ml.getMenu().addItem("M02", "M02-01", "Избранное");
        
        ml.getMenu().addGroup("M03", "Настройки", null, "111133");
        ml.getMenu().addItem("M03", "M03-01", "Управление альбомами");
        ml.getMenu().addItem("M03", "M03-02", "Настройки");
        ml.getMenu().addItem("M03", "M03-03", "Логи");
        
        MGI = ml.getMenu().getGroup("M02");
        DBWrapper.setMenuGroupItem2(MGI);
        
        albumCats = new AlbumsCategories(MGI);
        albumCats.RefreshAll();
        
        ml.setPrefSize(240, 9999);
        ml.setMaxSize(240, 9999);
        ml.setMinSize(240, 300);
        
        ml.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        ml.getStyleClass().add("MenuList");
        
        //base2vbox.getStyleClass().add("BaseBox2");
        base2vbox.getChildren().add(basesp);
        base2vbox.getChildren().add(paginator_1);
        
        basehbox.getChildren().add(base2vbox);
        basehbox.getChildren().add(ml);

       
        primaryStage.setMinWidth(840);
        primaryStage.setMinHeight(480);
        primaryStage.setTitle("Images database");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) ResizeHelper.addResizeListener(primaryStage);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        launch(args);
    }
    
    private VBox getSeparator1() {
        VBox sep1 = new VBox();
        _s1(sep1, 9999, 16);
        return sep1;
    }
    
    private VBox getSeparator1(double sz) {
        VBox sep1 = new VBox();
        _s2(sep1, sz, 16);
        return sep1;
    }
    
    private void _s2(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
        n.setMinSize(w, h);
    }
    
    private void _s1(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
    }
    
    public static final void L(String s) {
        final SimpleDateFormat DF = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        LOG.append("[").append(DF.format(new Date())).append("]\t");
        LOG.append(s).append("\n");
    }
}
