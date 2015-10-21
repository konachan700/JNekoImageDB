package fsimagelist;

import dataaccess.Crypto;
import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.DBEngine;
import dataaccess.FSEngine;
import dataaccess.Lang;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import jnekoimagesdb.JNekoImageDB;
import smallpaginator.SmallPaginator;

public class FSImageList extends ScrollPane{
//    private final boolean TIME_TEST = true;
    private final FSImageList THIS = this;

    /*
        Тут надо рефакторить все. Глюк на глюке, костыли и велосипеды.
        upd.20-10-2015: стили и строки отрефакторены, убраны отладочные сообщения. Надо бы поправить и код местами, но пока лень.
    */
    
    private final Image
            fna = new Image(new File("./icons/fna.png").toURI().toString()),
            fok = new Image(new File("./icons/fr2.png").toURI().toString());
    
    private final Button 
            lvlupImg = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/lvlup.png").toURI().toString()))), 
            toAlbImg = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/addalbum.png").toURI().toString()))),
            todbImg = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/adddef.png").toURI().toString()))),
            selallImg = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/selectall.png").toURI().toString()))),
            selnoneImg = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/selectnone.png").toURI().toString()))),
            navTo = new Button(Lang.ArrowNext);
    
    private final FlowPane
            this_container = new FlowPane();
    
    private final TextArea
            taLOG = new TextArea();
    
    private final VBox
            please_wait = new VBox(8);
    
    private final HBox 
            topPanel = new HBox(2),
            paginatorPanel = new HBox(2);
    
    private final TextField
            currPathBox = new TextField();
    
    private final Label
            currentSystemLoad = new Label();
    
    private volatile String 
            currentFile2    = Lang.NullString;

    private SmallPaginator
            xPag = null;
    
    private volatile String 
            currentPath = Lang.NullString;
    
    private volatile int
            isReloading = 0,
            currentPage = 0,
            imagesOnPages = 0,
            counterV = 0,
            fsWorker = 0,
            
            todbItemsTotalCount         = 0,
            todbItemCounter             = 0,
            todbThreadsCount            = 6, // кол-во потоков. Оптимально, наверное, 8. Если у кого-то i7 или xeon, можно увеличить.
            todbMTMinimalImageCount     = 32, // минимальное кол-во картинок, с которого включается многопоточный режим. Странно было бы добавлять 3 картинки в 8 потоков...
            
            gflCountForDispString       = 512,
            gflCounterForDispString     = 0
            ;
    
    private double 
            scrollNum = 0;
    
    private final ArrayList<FSImageListItem>
            itemsZ = new ArrayList<>();
    
    private final ArrayList<File>
            selectedFiles = new ArrayList<>();
    
    private volatile boolean
            isResized = false;
    
    private ImageEngine
            imgEn;

    private FSImageListActionListener 
            FSAL = new FSImageListActionListener() {
                @Override
                public void OnClick(FSImageListItem item) {
                    if (item.getFile() == null) return;
                    if (item.getFile().isFile()) {
                        if (item.IsSelected()) {
                            if (!selectedFiles.contains(item.getFile())) selectedFiles.add(item.getFile());
                        } else {
                            selectedFiles.remove(item.getFile());
                        }
                    }
                }

                @Override
                public void OnDblFolderClick(FSImageListItem item, File f) {
                    if (f != null) setPath(f.getAbsolutePath());
                }

                @Override
                public void OnDblImageClick(FSImageListItem item, File f) {
                    
                }
            };
    
    private final FSEngine
            ImagesFS;    

    private final DBEngine 
            SQL;
    
    private String[] 
            files = null;

    private int 
            tmr_counter_info = 0;
    
    private final Timeline TMR = new Timeline(new KeyFrame(Duration.millis(100), ae -> {
        tmr_counter_info++;
        if (tmr_counter_info >= 7) {
            tmr_counter_info = 0;
            __out_str();
        }

        if (fsWorker > 0) {
            if (fsWorker > 1) {
                if (!THIS.getContent().equals(please_wait)) THIS.setContent(please_wait);
            }
            fsWorker++;
        } else {
            if (THIS.getContent().equals(please_wait)) THIS.setContent(this_container);
        }
        
        if (isResized) return; else isResized = true;
        this_container.getChildren().clear();
        while (isReloading == 1) {}
                
        final double
                sz_h = this_container.getHeight() - 8D,
                sz_w = this_container.getWidth() - 8D; // Padding: 8px
        
        final int 
                count_w = (int)(sz_w / (double)(128+8+6)), 
                count_h = (int)(sz_h / (double)(128+8+6));
        
        imagesOnPages = count_h * count_w;
        itemsZ.clear();
        for (int i=0; i<imagesOnPages; i++) {
            FSImageListItem f = new FSImageListItem();
            f.setActionListener(FSAL);
            itemsZ.add(f);
            this_container.getChildren().add(f);
        }

        if (files == null) return;
        if (imagesOnPages == 0) return;
        
        final int tail = files.length % imagesOnPages;
        if ((xPag != null) && (imagesOnPages > 0)) xPag.setPageCount((files.length / imagesOnPages) + ((tail > 0) ? 1 : 0));
        currentPage = 0;
        if (xPag != null) xPag.setCurrentPage(0);
        __reloadPage();
    }));
    
    private void __out_str() {
        if (isReloading == 1) {
            final StringBuilder sbx = new StringBuilder();
            sbx
                    .append(Lang.FSImageList_stat_str_file)
                    .append(todbItemCounter)
                    .append(Lang.FSImageList_stat_str_of)
                    .append(todbItemsTotalCount)
                    .append(Lang.FSImageList_stat_str_separator);
            if (currentFile2.length() > 0) {
                sbx
                        .append(Lang.FSImageList_stat_str_file)
                        .append(currentFile2)
                        .append(Lang.FSImageList_stat_str_separator);
                currentSystemLoad.setText(sbx.substring(0));
            } else {
                sbx
                        .append(Lang.FSImageList_stat_str_mem_use)
                        .append((Runtime.getRuntime().totalMemory()) / (1024*1024))
                        .append(Lang.FSImageList_stat_str_mem_mb)
                        .append(Lang.FSImageList_stat_str_slash)
                        .append((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024))
                        .append(Lang.FSImageList_stat_str_mem_mb)
                        .append(Lang.FSImageList_stat_str_separator)
                        .append(Lang.FSImageList_stat_str_io)
                        .append((imgEn.getIOPS_W()+imgEn.getIOPS_R())/1024)
                        .append(Lang.FSImageList_stat_str_kbps)
                        .append(Lang.FSImageList_stat_str_separator);
                currentSystemLoad.setText(sbx.substring(0));
            }
        }
    }
    
    public final int setPath(String path) {
        currentPath = path;
        __reloadAll();
        return 0;
    }

    public HBox getPaginator() {
        return paginatorPanel;
    }
    
    private synchronized int __reload_files(String path) {
        _toPWLog(Lang.FSImageList_starting_list_folder);
        
        final File f = new File(path);
        if (!f.canRead()) return -1;
        
        selectedFiles.clear();
        Platform.runLater(() -> {
            currPathBox.setText(currentPath);
        });
            
        @SuppressWarnings("Convert2Lambda")
        final FilenameFilter filterDirs = new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {
                final File f = new File(file.getAbsolutePath() + File.separator + string);
                
                gflCounterForDispString++;
                if (gflCounterForDispString > gflCountForDispString) { currentFile2 = string; gflCounterForDispString = 0; }
                
                return f.isDirectory();
            }
        };

        @SuppressWarnings("Convert2Lambda")
        final FilenameFilter filterImages = new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {
                final String s = string.toLowerCase(Locale.ENGLISH);
                
                gflCounterForDispString++;
                if (gflCounterForDispString > gflCountForDispString) { currentFile2 = string; gflCounterForDispString = 0; }
                
                if (s.contains(".jpg")) // Выглядит жутковато, но... Экономим на сравнении строк, потому что 90% картинок это *.jpg
                    return true;
                else {
                    if (s.contains(".png")) 
                        return true;
                    else {
                        if (s.contains(".jpeg")) return true;
                    }
                }
                
                return false;
            }
        };

        gflCounterForDispString = 0;
        
        _toPWLog(Lang.FSImageList_get_folder_list);
        String[] dirsArray = f.list(filterDirs);
        
        _toPWLog(Lang.FSImageList_get_files_list);
        String[] imagesArray = f.list(filterImages);
        
        _toPWLog(Lang.FSImageList_log_str_folders + dirsArray.length + Lang.FSImageList_log_str_files + imagesArray.length + ".\n");
        
        currentPage = 0;
        currentPath = f.getAbsolutePath();

        if (dirsArray.length <= 0) {
            files = Arrays.copyOf(imagesArray, imagesArray.length);
        } else {
            files = new String[dirsArray.length + imagesArray.length];
            System.arraycopy(dirsArray, 0, files, 0, dirsArray.length);
            System.arraycopy(imagesArray, 0, files, dirsArray.length, imagesArray.length);
        }

        currentFile2 = "";
        return 0;
    }
    
    private synchronized int __addAll() {
        if (imagesOnPages <= 0) return -2;

        Platform.runLater(() -> { 
            itemsZ.stream().forEach((f) -> {
                f.clearIt();
            });
        });
        
        counterV = 0;
        int counter = (imagesOnPages*currentPage), trash = 0;
        
        for (;;counter++) {
            if (counter >= files.length) {
                for (int j=counterV; j<imagesOnPages; j++) {
                    @SuppressWarnings("UnnecessaryUnboxing") // Типичный костыль. Но как сделать иначе - не знаю.
                    int tval1 = new Integer(j).intValue();
                    Platform.runLater(() -> { itemsZ.get(tval1).hideIt(); });
                }
                break;
            }
            if ((counter-trash) >= (imagesOnPages*(currentPage+1))) break;
            
            @SuppressWarnings("UnnecessaryUnboxing") // Типичный костыль. Но как сделать иначе - не знаю.
            int tval1 = new Integer(counterV).intValue();
            
            final File fx = new File(currentPath + File.separator + files[counter]);
            if (imgEn.isImage(currentPath + File.separator + files[counter]) || fx.isDirectory()) {
                if (fx.isDirectory()) {
                    if (fx.canRead())
                        Platform.runLater(() -> { itemsZ.get(tval1).setInitInfo(fok, fx, true); });
                    else
                        Platform.runLater(() -> { itemsZ.get(tval1).setInitInfo(fna, fx, true); });
                } else {
                    final byte[] md5e = Crypto.MD5(fx.getAbsolutePath().getBytes());
                    final long IID = DBWrapper.getIDByMD5(md5e);
                    if (IID != -1) {
                        final Image imgc = ImagesFS.PopImage(IID);
                        if (imgc != null) 
                            Platform.runLater(() -> { 
                                itemsZ.get(tval1).setInitInfo(imgc, fx, false);
                                itemsZ.get(tval1).setSelected(selectedFiles.contains(fx));
                            });
                        else 
                            JNekoImageDB.L(Lang.FSImageList_err_pop_1 + fx.getAbsolutePath());
                    } else {
                        final long small_pns_id = ImagesFS.PushFileMT(ImageEngine.ResizeImage(fx.getAbsolutePath(), ImageEngine.SMALL_PREVIEW_SIZE, ImageEngine.SMALL_PREVIEW_SIZE), null);
                        if (small_pns_id > 0) {
                            DBWrapper.addPreviewAssoc(small_pns_id, md5e); 
                            final Image imgc = ImagesFS.PopImage(small_pns_id);
                            if (imgc != null) 
                                Platform.runLater(() -> { 
                                    itemsZ.get(tval1).setInitInfo(imgc, fx, false);
                                    itemsZ.get(tval1).setSelected(selectedFiles.contains(fx));
                                });
                            else 
                                JNekoImageDB.L(Lang.FSImageList_err_pop_2 + fx.getAbsolutePath());
                        } 
                        else
                            Platform.runLater(() -> { 
                                itemsZ.get(tval1).setInitInfo(fx);
                            });
                    } 
                }
                counterV++;
                if (counterV >= (imagesOnPages)) break;
            } else
                trash++;
        }
        
        System.gc();
        return 0;
    }

    public HBox getTopPanel() {
        return topPanel;
    }
    
    public boolean isStart() {
        return (currentPath.length() == 0) && (files == null);
    }
    
    public VBox getPW() {
        return please_wait;
    }
    
    public FSImageList(Crypto k, ImageEngine ie, DBEngine sql) {
        super();
        SQL   = sql;
        
        ImagesFS = new FSEngine(k, "imgtfs", SQL);
        imgEn = ie;

        this_container.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this_container.getStyleClass().add("FSImageList_this_container");
      
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("FSImageList_root_pane");
        
        paginatorPanel.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        paginatorPanel.getStyleClass().add("FSImageList_paginatorPanel");
        paginatorPanel.setAlignment(Pos.CENTER_RIGHT);
        _s1(paginatorPanel, 9999, 24);
        _s1(currPathBox, 9999, 24);
        _s2(navTo, 48, 24);
        
        currPathBox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        currPathBox.getStyleClass().add("FSImageList_currPathBox");
        
        navTo.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        navTo.getStyleClass().add("FSImageList_button");
        
        navTo.setOnMouseClicked((MouseEvent event) -> {
            File f = new File(currPathBox.getText().trim());
            if (f.isDirectory() && f.canRead()) {
                setPath(currPathBox.getText().trim());
            }
        });
        
        xPag = new SmallPaginator((int page) -> {
            currentPage = page;
            __reloadPage();
        });
        _s2(xPag, 220, 24);

        paginatorPanel.getChildren().addAll(currPathBox, navTo, getSeparator1(64), xPag);
        
        please_wait.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        please_wait.getStyleClass().add("FSImageList_please_wait");
        please_wait.setMaxSize(9999, 9999);
        please_wait.setPrefSize(9999, 9999);
        please_wait.setAlignment(Pos.TOP_LEFT); 
        
        final Label pw = new Label(Lang.FSImageList_please_wait_1);
        pw.getStyleClass().add("FSImageList_please_wait_label");
        final DropShadow ds = new DropShadow();
        ds.setOffsetY(0f);
        ds.setRadius(7f);
        ds.setSpread(0.8f);
        ds.setColor(Color.color(0.99f, 0.99f, 0.99f));
        pw.setEffect(ds);
        pw.setMaxSize(9999, 21);
        pw.setPrefSize(9999, 21);
        
        currentSystemLoad.setEffect(ds);
        currentSystemLoad.getStyleClass().add("FSImageList_currentSystemLoad");
        
        taLOG.setMaxSize(9999, 9999);
        taLOG.setPrefSize(9999, 9999);
        taLOG.setWrapText(true);
        taLOG.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        taLOG.getStyleClass().add("FSImageList_logbox");
        taLOG.getStyleClass().add("FSImageList_root_pane");
        
        please_wait.getChildren().add(pw);
        please_wait.getChildren().add(currentSystemLoad);
        please_wait.getChildren().add(taLOG);
        
        this.setVbarPolicy(ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setMaxSize(9999, 9999);
        this.setPrefSize(9999, 9999);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setContent(this_container);
        
        this_container.setVgap(8);
        this_container.setHgap(8);
        this_container.setAlignment(Pos.CENTER);
        this_container.setColumnHalignment(HPos.CENTER);
        this_container.setOrientation(Orientation.HORIZONTAL);
        this_container.setRowValignment(VPos.TOP);
        
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            this_container.getChildren().clear();
            isResized = false;
        });
        
        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            this_container.getChildren().clear();
            isResized = false;
        });
        
        this.setOnScroll((ScrollEvent event) -> {
            if (isReloading == 1) return;
            
            scrollNum = scrollNum + event.getDeltaY();
            if (scrollNum >= 60) {
                if (xPag != null) xPag.Prev();
                scrollNum = 0;
            }
            
            if (scrollNum <= -60) {
                if (xPag != null) xPag.Next();
                scrollNum = 0;
            }
        }); 
        
        todbImg.setOnMouseClicked((MouseEvent event) -> {
            if (selectedFiles.size() > 0) {
                THIS.setContent(please_wait);
                __toDB();
            }
        });
        
        lvlupImg.setOnMouseClicked((MouseEvent event) -> {
            File ff = new File(currentPath).getParentFile();
            if (ff != null) {
                setPath(ff.getAbsolutePath());
            } else {
                File[] fr = File.listRoots();
                ArrayList<String> al = new ArrayList<>();
                for (File fa : fr) {
                    if (fa.canRead()) al.add(fa.getAbsolutePath());
                    //System.out.println(fa.getAbsolutePath()); 
                }
                files = new String[al.size()];
                files = al.toArray(files);
                currentPath = "";
                __reloadAll();
            }
        });
        
        selnoneImg.setOnMouseClicked((MouseEvent event) -> {
            selectedFiles.clear();
            __reloadPage();
        });
        
        selallImg.setOnMouseClicked((MouseEvent event) -> {
            selectedFiles.clear();
            for (String f : files) {
                File a = new File(currentPath + File.separator + f);
                if (a.isFile()) selectedFiles.add(a);
            }
            __reloadPage();
        });
        
        _s1(topPanel, 9999, 64);
        topPanel.setMinSize(128, 64);
        topPanel.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        topPanel.getStyleClass().add("FSImageList_topPanel");
        
        lvlupImg.getStyleClass().add("FSImageList_button");
        selallImg.getStyleClass().add("FSImageList_button");
        selnoneImg.getStyleClass().add("FSImageList_button");
        todbImg.getStyleClass().add("FSImageList_button");
        toAlbImg.getStyleClass().add("FSImageList_button");
        
        final int sz = 64;
        _s2(lvlupImg, sz, sz);
        _s2(selallImg, sz, sz);
        _s2(selnoneImg, sz, sz);
        _s2(toAlbImg, sz, sz);
        _s2(todbImg, sz, sz);

        topPanel.getChildren().addAll(lvlupImg, getSeparator1(12), selallImg, selnoneImg, getSeparator1(), toAlbImg, todbImg);
        
        TMR.setCycleCount(Animation.INDEFINITE);
        TMR.play();
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

    private void __reloadAll() {
        if (isReloading == 1) return;
        final Task taskFullReload = new Task<Void>() {
            @Override 
            public Void call() {
//                long xt = new Date().getTime();
                if (isReloading == 1) {
                    return null;
                }
                fsWorker = 1;
                isReloading = 1;
                __reload_files(currentPath);
                
                isResized = false;
                isReloading = 2;
                fsWorker = 0;

                //if (TIME_TEST) JNekoImageDB.L("__reloadAll exec time: "+(new Date().getTime() - xt));
                return null;
            }
        };

        final Thread t = new Thread(taskFullReload);
        t.setDaemon(true);
        t.start();
    }
    
    private void __reloadPage() {
        final Task taskForPage = new Task<Void>() {
            @Override 
            public Void call() {
//                long xt = new Date().getTime();
                if (isReloading == 1) return null;
                isReloading = 1;
                __addAll();
                isReloading = 2;
                //if (TIME_TEST) JNekoImageDB.L("__reloadPage() exec time: "+(new Date().getTime() - xt));
                return null;
            }
        };
        final Thread t = new Thread(taskForPage);
        t.setDaemon(true);
        t.start();
    }
    
    /*
        TODO: Тут нужно сделать формирование лог-файла со списком всех файлов, которые не получилось добавить.
    */
    private void __x1() {
        final File f = selectedFiles.get(todbItemCounter);
        final String fl = f.getAbsolutePath();
        long xt = new Date().getTime();

        final byte[] b = FSEngine.getFileMD5MT(fl);
        if (b != null) {
            if (imgEn.isMD5(b)) {
                _toPWLog("["+(new Date().getTime() - xt)+"] "+Lang.FSImageList_log_str_file+" ["+f.getName()+"] " + Lang.FSImageList_log_str_already_exist); 
            } else {
                final long res = imgEn.UploadImage(fl, b);
                if (res <= 0)
                    _toPWLog("["+(new Date().getTime() - xt)+"] "+Lang.FSImageList_log_str_file+" ["+f.getName()+"] " + Lang.FSImageList_log_str_cannoit_be_added);
//                else // не думаю, что сообщать о каждом успешно добавленном есть хорошая идея. 
//                    _toPWLog("["+(new Date().getTime() - xt)+"] ["+todbItemCounter+"] Файл ["+f.getName()+"] успешно добавлен.");
            }
        } else {
            _toPWLog("["+(new Date().getTime() - xt)+"] "+Lang.FSImageList_log_str_file+" ["+f.getName()+"] " + Lang.FSImageList_log_broken);
        }
    }
    
    private synchronized void __toDB() {
        taLOG.setText(Lang.NullString);
        if (isReloading == 1) return;
        isReloading         = 1; // Блокирует повторное выполнение метода, по факту не нужно
        fsWorker            = 1; // Показывает заставку с прогрессом выполнения
        todbItemsTotalCount = selectedFiles.size();
        todbItemCounter     = 0;
        
        final Runnable taskR = () -> {
            for (; todbItemCounter<todbItemsTotalCount; todbItemCounter++) {
                __x1();
            }
            
            selectedFiles.clear();
            
            fsWorker        = 0;
            isReloading     = 2;
        };
        
        if (selectedFiles.size() > todbMTMinimalImageCount) {
            for (int i=0; i<todbThreadsCount; i++) {
                DBWrapper.Sleep(175);
                final Thread t = new Thread(taskR);
                t.setDaemon(true);
                t.start();
            }
        } else {
            final Thread t = new Thread(taskR);
            t.setDaemon(true);
            t.start();
        }
    }
    
    private void _toPWLog(String s) {
        Platform.runLater(() -> { 
            if (taLOG.getText().length() > (128 * 1024)) taLOG.setText(Lang.NullString);
            final SimpleDateFormat DF = new SimpleDateFormat(Lang.DateTimeFormatWithSeconds);
            taLOG.appendText("[" + DF.format(new Date()) + "] " + s + "\n");
            taLOG.setScrollTop(Double.MIN_VALUE);
        });
    }
    
    private static void _L(String s) {
        //System.out.println(s);
        JNekoImageDB.L(s);
    }
}
