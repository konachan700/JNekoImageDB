package imagelist;

import dataaccess.DBImageX;
import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.DBEngine;
import dialogs.DYesNo;
import dialogs.PleaseWait;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jnekoimagesdb.JNekoImageDB;
import smallpaginator.SmallPaginator;

public class ImageList extends FlowPane {
    private ImageEngine 
            IMG = null;
    
    private final ImageList
            THIS = this;
    
//    private final DBEngine 
//            SQL;
    private final Image broken = new Image(new File("./icons/broken.png").toURI().toString());
    
    private final Button
//            toAlbImg = new Button("", new ImageView(new Image(new File("./icons/addalbum.png").toURI().toString()))),
            selallImg = new Button("", new ImageView(new Image(new File("./icons/selectall.png").toURI().toString()))),
            selnoneImg = new Button("", new ImageView(new Image(new File("./icons/selectnone.png").toURI().toString()))),
            addtagImg = new Button("", new ImageView(new Image(new File("./icons/addtag.png").toURI().toString()))),
            deltagImg = new Button("", new ImageView(new Image(new File("./icons/cleartags.png").toURI().toString()))),
            toAlbImg = new Button("", new ImageView(new Image(new File("./icons/addalbum.png").toURI().toString()))),
            toTempImg = new Button("", new ImageView(new Image(new File("./icons/addtotemp.png").toURI().toString())));
    
    private final Label
            currCountLabel = new Label();
    
    private volatile int
            is_resized = 0,
            images_count = 0,
            currentPage = 0,
            isProcessRunning = 0;
    
    private volatile long
            albumID = 0;
    
    private volatile int
            totalImagesCount = 0;
    
    private double 
            scrollNum = 0;
    
    private SmallPaginator
            xPag = null;
    
    private final PleaseWait 
            PW;
    
    private final Pane
            xParent;
    
    private final StringBuilder
            logtxt = new StringBuilder(),
            toptxt = new StringBuilder();
    
    private HBox 
            topPanel = new HBox(2),
            paginatorPanel = new HBox(2);
    
    private final ArrayList<ImageListItem> 
            ALII = new ArrayList<>();
    
    private final ArrayList<Long> 
            selectedItems = new ArrayList<>();
    
    private final ImageListItemActionListener
            IAL = (ImageListItem item) -> {
                if (item.isSelected()) {
                    if (!selectedItems.contains(item.getID()))
                        selectedItems.add(item.getID());
                } else {
                    selectedItems.remove(item.getID());
                }
            };
    
    private final Timeline TMR = new Timeline(new KeyFrame(Duration.millis(150), ae -> {
        resizeTmr();
    }));
    
    private void resizeTmr() {
        if (isProcessRunning == 1) {
            toptxt.delete(0, toptxt.length());
            toptxt
                    .append("Memory use: ")
                    .append((Runtime.getRuntime().totalMemory()) / (1024 * 1024))
                    .append("MB; My I/O:")
                    .append((IMG.getIOPS_W() + IMG.getIOPS_R()) / 1024)
                    .append(" kBps; ")
                    ;
            PW.Update();
            return;
        }

        if (is_resized == 0) return;
                
        final double
                sz_h = this.getHeight() - 8D,
                sz_w = this.getWidth() - 8D; // Padding: 8px
        
        final int 
                count_w = (int)(sz_w / (double)(128+8+6)), 
                count_h = (int)(sz_h / (double)(128+8+6));
        
        images_count = count_h * count_w;
        if (ALII.size() < images_count) {
            for (int i=ALII.size(); i<images_count; i++) ALII.add(new ImageListItem(IAL));
        }
        
        ALII.stream().forEach((ALII1) -> {
            ALII1.clearIt();
        });
        
        currentPage = 0;
        if (xPag != null) xPag.setCurrentPage(0);

        normalRefresh();
        is_resized = 0;
    }
    
    public void setAlbimID(long _albumID) {
        albumID = _albumID;
    }
    
    public final void normalRefresh() {
        final Task waitForResize = new Task<Void>() {
            @Override 
            public Void call() {
                while (images_count == 0) {}
                Platform.runLater(() -> {
                    ArrayList<DBImageX> d;
                    if (albumID == 0){
                        d = DBWrapper.getImagesX(-1, (currentPage*images_count), images_count);
                        totalImagesCount = (int) IMG.getImgCount();
                    } else {
                        d = DBWrapper.getImagesX(albumID, (currentPage*images_count), images_count);
                        totalImagesCount = (int) DBWrapper.getImagesCountInAlbum(albumID);
                    }

                    final int tail = totalImagesCount % images_count;
                    if ((xPag != null) && (images_count > 0)) xPag.setPageCount((totalImagesCount / images_count) + ((tail > 0) ? 1 : 0));

                    if (d == null) return;
                    int cointer = 0;

                    THIS.getChildren().clear();
                    ALII.stream().forEach((ALII1) -> {
                        ALII1.clearIt();
                    });

                    for (DBImageX l : d) {
                        if (ALII.get(cointer) != null) {
                            final byte[] buf1 = IMG.getThumbsFS().PopPrewievFile(l);
                            if (buf1 != null) 
                                ALII.get(cointer).setImg(128, 128, buf1); 
                            else
                                ALII.get(cointer).setImg(128, 128, broken); 
                            ALII.get(cointer).setID(l.pl_idid);
                            ALII.get(cointer).setSelected(selectedItems.contains(l.pl_idid)); 
                            THIS.getChildren().add(ALII.get(cointer));
                        }
                        cointer++;
                    }
                });
                return null;
            }
        };
        
        final Thread t = new Thread(waitForResize);
        t.setDaemon(true);
        t.start();
    }

    public ImageListItem getItem(int id) {
        return ALII.get(id);
    }
    
    public int getCount() {
        return images_count;
    }
    
    public HBox getTopPanel() {
        return topPanel;
    }
    
    public ImageList(ImageEngine im, Pane parent) {
        super(Orientation.HORIZONTAL);
        this.setVgap(8);
        this.setHgap(8);
        this.setRowValignment(VPos.TOP);
        this.setColumnHalignment(HPos.CENTER);
        this.setAlignment(Pos.CENTER);
        this.setMaxSize(9999, 9999);
        this.setPrefSize(9999, 9999);
        
        IMG = im;
//        SQL = sql;
        xParent = parent;

        xPag = new SmallPaginator((int page) -> {
            currentPage = page;
            normalRefresh();
        });
        _s2(xPag, 220, 24);
        
        paginatorPanel.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        paginatorPanel.getStyleClass().add("PagPanel");
        paginatorPanel.setAlignment(Pos.CENTER_RIGHT);
        _s1(paginatorPanel, 9999, 24);
        
        currCountLabel.getStyleClass().add("PagCountLabel");
        currCountLabel.setAlignment(Pos.CENTER);
        _s2(currCountLabel, 192, 24);
        
        paginatorPanel.getChildren().addAll(currCountLabel, getSeparator1(), xPag);
        
        _s1(topPanel, 9999, 64);
        topPanel.setMinSize(128, 64);
        topPanel.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        topPanel.getStyleClass().add("topPanel");
        
        selallImg.getStyleClass().add("ImgButtonB");
        selnoneImg.getStyleClass().add("ImgButtonB");
        addtagImg.getStyleClass().add("ImgButtonG");
        deltagImg.getStyleClass().add("ImgButtonR2");
        toTempImg.getStyleClass().add("ImgButtonR");
        toAlbImg.getStyleClass().add("ImgButtonR2");
        
        final int sz = 64;
        _s2(toAlbImg, sz, sz);
        _s2(toTempImg, sz, sz);
        _s2(addtagImg, sz, sz);
        _s2(deltagImg, sz, sz);
        _s2(selallImg, sz, sz);
        _s2(selnoneImg, sz, sz);
        
        PW = new PleaseWait(xParent, toptxt, logtxt);
        
        toTempImg.setOnMouseClicked((MouseEvent event) -> {
            if (isProcessRunning == 1) return;
            
            if (selectedItems.size() <= 0) return;
            final DYesNo d = new DYesNo(topPanel, 
                    new DYesNo.DYesNoActionListener() {
                        @Override
                        public void OnYes() {
                            _toTempFolder();
                        }

                        @Override
                        public void OnNo() { }
                    }, "Процесс может занять длительное время. Продолжить?");           
            event.consume();
        }); 
        
        selnoneImg.setOnMouseClicked((MouseEvent event) -> {
            if (isProcessRunning == 1) return;
                       
            selectedItems.clear();
            is_resized = 1;
            event.consume();
        });
        
        selallImg.setOnMouseClicked((MouseEvent event) -> {
            if (isProcessRunning == 1) return;
            
            _selectAll();
            is_resized = 1;
            event.consume();
        });

        topPanel.getChildren().addAll(addtagImg, deltagImg, getSeparator1(8), selallImg, selnoneImg, getSeparator1(), toAlbImg, toTempImg);
        
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("ImageList");
        
        totalImagesCount = (int) IMG.getImgCount();
        currCountLabel.setText(totalImagesCount+" images");
        
        TMR.setCycleCount(Animation.INDEFINITE);
        TMR.play();
                
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            this.getChildren().clear();
            is_resized = 1;
        });
        
        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            this.getChildren().clear();
            is_resized = 1;
        });
        
        this.setOnScroll((ScrollEvent event) -> {
            if (isProcessRunning == 1) return;
            if (event.getDeltaY() > 0) {
                if (xPag != null) xPag.Prev();
            } else {
                if (xPag != null) xPag.Next();
            }
            
            
//            scrollNum = scrollNum + event.getDeltaY();
//            if (scrollNum >= 60) {
//                if (xPag != null) xPag.Prev();
//                scrollNum = 0;
//            }
//            
//            if (scrollNum <= -60) {
//                if (xPag != null) xPag.Next();
//                scrollNum = 0;
//            }
        });
    }
    
    private void _toTempFolder() {
        final Task taskForPage = new Task<Void>() {
            @Override 
            public Void call() {
//                long xt = new Date().getTime();
                if (isProcessRunning == 1) return null;
                isProcessRunning = 1;
                
                String path = DBWrapper.ReadAPPSettingsString("ff_uploadPath"); 
                if ((path.length() > 0) && (new File(path).canWrite()) && (new File(path).isDirectory())) {
                    selectedItems.stream().forEach((l) -> {
                        int ix = IMG.DownloadImageToFS(l, path);
                        logtxt.append("File: [").append(path).append((ix == 0) ? "] OK\n" : "] FAILED\n");
                    });
                } else {
                    JNekoImageDB.L("Папка выгрузки не существует или недоступна для записи!");
                }

                PW.setVis(false);
                selectedItems.clear();
                is_resized = 1;
                isProcessRunning = 2;
                return null;
            }
        };
        PW.setVis(true);
        final Thread t = new Thread(taskForPage);
        t.setDaemon(true);
        t.start();
    }
    
    private void _selectAll() {
        final Task taskForPage = new Task<Void>() {
            @Override 
            public Void call() {
                if (isProcessRunning == 1) return null;
                isProcessRunning = 1;

                logtxt.append("Получаю список изображений...");
                selectedItems.clear();
                if (albumID == 0) 
                    selectedItems.addAll(IMG.getImages("ORDER BY oid DESC;"));
                else {
                    int cnt = (int) DBWrapper.getImagesCountInAlbum(albumID);
                    selectedItems.addAll(DBWrapper.getImagesByGroupOID(albumID, 0, cnt));
                }
                
                PW.setVis(false);
                is_resized = 1;
                isProcessRunning = 2;
                return null;
            }
        };
        PW.setVis(true);
        final Thread t = new Thread(taskForPage);
        t.setDaemon(true);
        t.start();
    }
    
    public HBox getPaginator() {
        return paginatorPanel;
//        return xPag;
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
}
