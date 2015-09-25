package imagelist;

import dataaccess.DBImageX;
import dataaccess.DBWrapper;
import dataaccess.ImageEngine;
import dataaccess.DBEngine;
import dataaccess.ImageCache;
import dialogs.DYesNo;
import dialogs.PleaseWait;
import java.io.File;
import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import jnekoimagesdb.GUITools;
import jnekoimagesdb.JNekoImageDB;
import smallpaginator.SmallPaginator;

public class ImageList extends FlowPane {
    private ImageEngine 
            IMG = null;
    
    private final ImageList
            THIS = this;

    private final Image broken = new Image(new File("./icons/broken.png").toURI().toString());
    
    private final Button
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
    
    private volatile boolean
            isPaginatorActive = false;
    
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
            topPanel = new HBox(0),
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
    
    private final Timeline TMR = new Timeline(new KeyFrame(Duration.millis(88), ae -> {
        displayProgress();
        resizeWindow();
        displayImages();
    }));
    
    
    private void displayProgress() {
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
        }
    }
    
    private void resizeWindow() {
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
        
        isPaginatorActive = true;
        is_resized = 0;
    }
    
    private void displayImages() {
        if (isPaginatorActive == false) return;
        if (images_count <= 0) {
            is_resized = 1;
            return;
        } 
        
        int cacheCount = 2;        
        ImageCache.setCacheSize(images_count * cacheCount);
        
        final ArrayList<DBImageX> d;
        if (albumID == 0){
            d = DBWrapper.getImagesX(-1, (currentPage*images_count), images_count * cacheCount);
        } else {
            d = DBWrapper.getImagesX(albumID, (currentPage*images_count), images_count * cacheCount);
        }

        final int tail = totalImagesCount % images_count;
        if ((xPag != null) && (images_count > 0)) xPag.setPageCount((totalImagesCount / images_count) + ((tail > 0) ? 1 : 0));

        if (d == null) {
            isPaginatorActive = false;
            return;
        }
        int cointer = 0;

        THIS.getChildren().clear();
        ALII.stream().forEach((ALII1) -> {
            ALII1.clearIt();
        });

        for (DBImageX l : d) {
            if (cointer < images_count) {
                if (ALII.get(cointer) != null) {
                    final Image buf1 = ImageCache.PopImage(IMG, l);
                    if (buf1 != null) 
                        ALII.get(cointer).setImg(128, 128, buf1); 
                    else
                        ALII.get(cointer).setImg(128, 128, broken); 
                    ALII.get(cointer).setID(l.pl_idid);
                    ALII.get(cointer).setSelected(selectedItems.contains(l.pl_idid)); 
                    THIS.getChildren().add(ALII.get(cointer));
                }
            } else {
                if (scrollNum <= 0) ImageCache.Preload(IMG, l); 
            }
            cointer++;
            
        }
        
        isPaginatorActive = false;
    }
    
    public void setAlbimID(long _albumID) {
        albumID = _albumID;
    }
    
    public final synchronized void normalRefresh() {
        is_resized = 1;
        isPaginatorActive = true;
        
        if (albumID == 0){
            totalImagesCount = (int) IMG.getImgCount();
        } else {
            totalImagesCount = (int) DBWrapper.getImagesCountInAlbum(albumID);
        }
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
            if (isPaginatorActive) return;
            currentPage = page;
            isPaginatorActive = true;
        });
        GUITools.setFixedSize(xPag, 220, 24);
        
        paginatorPanel.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        paginatorPanel.getStyleClass().add("PagPanel");
        paginatorPanel.setAlignment(Pos.CENTER_RIGHT);
        GUITools.setMaxSize(paginatorPanel, 9999, 24);
        
        currCountLabel.getStyleClass().add("PagCountLabel");
        currCountLabel.setAlignment(Pos.CENTER);
        GUITools.setFixedSize(currCountLabel, 192, 24);
        
        paginatorPanel.getChildren().addAll(currCountLabel, GUITools.getSeparator(), xPag);
        
        GUITools.setMaxSize(topPanel, 9999, 64);
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
        GUITools.setFixedSize(toAlbImg, sz, sz);
        GUITools.setFixedSize(toTempImg, sz, sz);
        GUITools.setFixedSize(addtagImg, sz, sz);
        GUITools.setFixedSize(deltagImg, sz, sz);
        GUITools.setFixedSize(selallImg, sz, sz);
        GUITools.setFixedSize(selnoneImg, sz, sz);
        
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
            if ((isProcessRunning == 1) || (is_resized == 1)) return;
                       
            selectedItems.clear();
            is_resized = 1;
            event.consume();
        });
        
        selallImg.setOnMouseClicked((MouseEvent event) -> {
            if ((isProcessRunning == 1) || (is_resized == 1)) return;
            
            _selectAll();
            is_resized = 1;
            event.consume();
        });

        topPanel.getChildren().addAll(addtagImg, deltagImg, GUITools.getSeparator(8), selallImg, selnoneImg, GUITools.getSeparator(8), toAlbImg, GUITools.getSeparator(), toTempImg);
        
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
            if ((isProcessRunning == 1) || (is_resized == 1)) return;
            if (isPaginatorActive) return;
            
            scrollNum = event.getDeltaY();
            if (scrollNum > 0) {
                if (xPag != null) xPag.Prev();
            } else {
                if (xPag != null) xPag.Next();
            }
        });
    }
    
    private void _toTempFolder() {
        final Task taskForPage = new Task<Void>() {
            @Override 
            public Void call() {
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
    }
}
