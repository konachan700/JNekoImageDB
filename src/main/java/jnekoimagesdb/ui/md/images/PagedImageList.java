package jnekoimagesdb.ui.md.images;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.core.img.XImgPreviewGen;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.GUITools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jiconfont.javafx.IconNode;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.md.dialogs.ImageViewDialog;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;

public class PagedImageList extends ScrollPane {
    public final static String
            CSS_FILE = new File("./style/style-gmd-pil.css").toURI().toString();
    
    public static PagedImageList
            sPIL = null;
        
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(PagedImageList.class);
    
    private volatile boolean 
            isExit = false;
    
    private volatile int
            busyCounter = 0;
    
    private DSImageIDListCache.ImgType
            imageType = DSImageIDListCache.ImgType.All;
    
    private DSImageIDListCache
            currCache = DSImageIDListCache.getAll();
    
    private class PreviewGenerator implements Runnable {
        @Override
        @SuppressWarnings({"SleepWhileInLoop", "UseSpecificCatch"})
        public void run() {
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) {
                while (XImg.getPSizes().getPrimaryPreviewSize() == null) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ex) {
                        logger.error(ex.getMessage());
                        //Logger.getLogger(PagedFileList.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                }
            }
            
            while (true) {
                if (isExit) return;
                try {
                    final DSImage currDSI = prevGenDeque.pollLast();
                    if (isExit) return;
                    
                    if (currDSI != null) {
                        final Image img = XImgDatastore.createPreviewEntryFromExistDBFile(currDSI.getMD5(), XImg.PreviewType.previews);
                        if (img != null) setImage(img, currDSI); else busyCounter--;
                    } else {
                        final DSImage upDSI = uploadDeque.pollLast();
                        if (upDSI != null) {
                            try {
                                XImgDatastore.copyToExchangeFolderFromDB(SettingsUtil.getPath("pathBrowserExchange"), upDSI);
                            } catch (Exception ex) {
                                logger.error(ex.getMessage());
                                //Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else
                            try { Thread.sleep(100); } catch (Exception e) { return; }
                    }
                } catch (InterruptedException ex) {
                    busyCounter--;
                    logger.error(ex.getMessage());
                    //Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                } catch (Exception ex) {
                    busyCounter--;
                    logger.error(ex.getMessage());
                    //Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
        }
        
        private void setImage(Image img, DSImage dsi) {
            Platform.runLater(() -> { 
                elementsPool.forEach(c -> {
                    if (c.equals(dsi)) {
                        busyCounter--;
                        c.setImage(img);
                    }
                });
            });
        }
    }
    
    private final FlowPane
            container = new FlowPane();
    
    private volatile int 
            itemSizeX                   = 128,
            itemSizeY                   = 128,
            itemCountOnOneLine          = 0,
            itemCountOnOneColoumn       = 0,
            itemTotalCount              = 0,
            itemCountOnOneLineOld       = -1,
            itemCountOnOneColoumnOld    = 0,
            itemSpacer                  = 4;
    
    private String 
            groupTitle = "";
    
    private volatile long
            currentAlbumID = 0;

    private final Pagination
            pag = new Pagination();
    
    private final ArrayList<ImageListItem>
            elementsPool = new ArrayList<>();
    
    private final LinkedBlockingDeque<DSImage>
            uploadDeque = new LinkedBlockingDeque<>(),
            prevGenDeque = new LinkedBlockingDeque<>();
    
    private ExecutorService 
            previewGenService = null;
    
    private List<DSTag>
            tagsList = null,
            tagsNotList = null;

    protected interface ImageListItemActionListener {
        public void OnSelect(boolean isSelected, DSImage item);
        public void OnOpen(DSImage item, ImageListItem it);
    }
    
    private final ArrayList<DSImage>
            selectedElementsPool = new ArrayList<>();
    
    protected class ImageListItem extends Pane {
        private final ImageView 
                imageContainer = new ImageView();
        
        private final IconNode
                selectedIcon; 
        
        private DSImage
                img = null;
        
        public int 
                myNumber = 0;
        
        private final VBox 
                imageVBox = new VBox(0);
        
        private final ImageListItemActionListener
                actionListener;
       
        public byte[] getMD5() {
            return img.getMD5();
        }
        
        public void setMD5(byte[] b) {
            img.setMD5(b);
        }
        
        public DSImage get() {
            return img;
        }
        
        public final void setNullImage() {
            img = null;
            imageContainer.setImage(GUITools.loadIcon("dummy-128"));
            imageContainer.setVisible(false);
            this.getChildren().clear();
            this.getStyleClass().clear();
        }
        
        public final void setImage(Image img) {
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
            imageContainer.setFitHeight(XImg.getPSizes().getPrimaryPreviewSize().getHeight());
            imageContainer.setFitWidth(XImg.getPSizes().getPrimaryPreviewSize().getWidth());
            
            imageContainer.setImage(img);
            imageContainer.setVisible(true);
            
            if (this.getChildren().isEmpty()) addAll();
            this.getStyleClass().addAll("pil_item_root_pane");
        }
        
        public final void setImage(DSImage dsi) {
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
            imageContainer.setFitHeight(XImg.getPSizes().getPrimaryPreviewSize().getHeight());
            imageContainer.setFitWidth(XImg.getPSizes().getPrimaryPreviewSize().getWidth());
            
            img = dsi; 
            XImgPreviewGen.PreviewElement peDB;
            try {
                    peDB = XImgDatastore.readPreviewEntry(dsi.getMD5());
                    final Image im = peDB.getImage(XImg.getCrypt(), XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
                    if (im != null) {
                        imageContainer.setImage(im);
                        imageContainer.setVisible(true);
                    } else 
                        throw new IOException();
            } catch (IOException | ClassNotFoundException ex) {
                prevGenDeque.add(dsi);
                busyCounter++;
                imageContainer.setImage(GUITools.loadIcon("loading-128"));
                imageContainer.setVisible(true);
            }
                
            if (this.getChildren().isEmpty()) addAll();
            this.getStyleClass().addAll("pil_item_root_pane");
        }
        
        public final void setSelected(boolean s) {
            if (imageContainer.isVisible()) selectedIcon.setVisible(s);
        }
        
        public final boolean getSelected() {
            return selectedIcon.isVisible();
        }
        
        public void setSizes(long x, long y) {
            GUITools.setMaxSize(imageVBox, x, y);
            GUITools.setMaxSize(this, x, y);
            imageContainer.setFitHeight(y);
            imageContainer.setFitWidth(x);
        }
        
        public ImageListItem(ImageListItemActionListener al) {
            super();
            actionListener = al;
            
            selectedIcon = new IconNode();
            selectedIcon.getStyleClass().add("pil_selected_item_icon");
            
            this.getStylesheets().add(CSS_FILE);
            this.getStyleClass().addAll("pil_item_root_pane");
            this.setMaxSize(itemSizeX, itemSizeY);
            this.setPrefSize(itemSizeX, itemSizeY);
            
            this.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 1) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        setSelected(!getSelected());
                        actionListener.OnSelect(getSelected(), img);
                    }
                } else {
                    actionListener.OnOpen(img, this);
                }
                event.consume();
            });
        
            imageContainer.setPreserveRatio(true);
            imageContainer.setSmooth(true);
            imageContainer.setCache(false);
            imageContainer.setImage(GUITools.loadIcon("dummy-128"));
            
            imageVBox.setAlignment(Pos.CENTER);    
            imageVBox.getStyleClass().addAll("pil_item_null_pane");
            imageVBox.getChildren().add(imageContainer);
            
            selectedIcon.setVisible(false);
            
            addAll();
        }
        
        private void addAll() {
            this.getChildren().addAll(imageVBox, selectedIcon);
            imageVBox.relocate(0, 0);
            selectedIcon.relocate(10, 10);
        }
        
        public boolean equals(DSImage o) {
            if ((o == null) || (img == null)) return false;
            return Arrays.equals(((DSImage) o).getMD5(), img.getMD5());
        }
    }

    private final ImageListItemActionListener
            elementListener = new ImageListItemActionListener() {
                @Override
                public void OnSelect(boolean isSelected, DSImage item) {
                    if (isSelected)
                        selectedElementsPool.add(item);
                    else
                        selectedElementsPool.remove(item);
                }

                @Override
                public void OnOpen(DSImage item, ImageListItem it) {
                    if (it.myNumber >= 0) {
                        int off = pag.getCurrentPageIndex() * itemTotalCount;
                        
                        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                        int width = (int)(screenSize.getWidth() * 0.8);
                        int height = (int)(screenSize.getHeight() * 0.8);
        
                        final ImageViewDialog imgd = new ImageViewDialog(width, height);
                        imgd.show(currCache.getCount() - 1 - (it.myNumber + off), imageType); 
                    }
                }
            };
    
    private Session 
            hibSession = null;
    
    private boolean 
            isResize = false,
            forceReload = false;
    
    private final Timeline TMR = new Timeline(new KeyFrame(Duration.millis(88), ae -> {
        if (isResize) {
            _calcPaginator();
            isResize = false;
            forceReload = false;
        }
    }));

    @SuppressWarnings("LeakingThisInConstructor")
    private PagedImageList() {
        super();
        this.setVbarPolicy(ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setContent(container);
        this.getStylesheets().add(CSS_FILE); 
        this.getStyleClass().addAll("pil_root_sp_pane");

        container.getStyleClass().addAll("pil_root_pane", "pil_max_width", "pil_max_height");
        container.setAlignment(Pos.CENTER);
        container.setHgap(itemSpacer);
        container.setVgap(itemSpacer);
        
        pag.setMaxSize(9999, 24);
        pag.setMinSize(128, 24);
        pag.setPrefSize(9999, 24);

        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
                itemSizeX = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
                itemSizeY = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
                
                itemCountOnOneLine = (newValue.intValue()-20) / (itemSizeX + itemSpacer);
                if (itemCountOnOneLineOld != itemCountOnOneLine) {
                    forceReload = true;
                    itemCountOnOneLineOld = itemCountOnOneLine;
                }
                isResize = true;
            }
        });
        
        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
                itemSizeX = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
                itemSizeY = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
        
                itemCountOnOneColoumn = (newValue.intValue()-20) / (itemSizeY + itemSpacer);
                if (itemCountOnOneColoumnOld != itemCountOnOneColoumn) {
                    forceReload = true;
                    itemCountOnOneColoumnOld = itemCountOnOneColoumn;
                }
                isResize = true;
            }
        });
        
        this.setFocusTraversable(true);
        this.setOnKeyPressed((KeyEvent key) -> {
            if (isResize || forceReload) return;
            
            int index = pag.getCurrentPageIndex();
            if (key.getCode() == KeyCode.LEFT)  if (index > 0) pag.setCurrentPageIndex(index - 1);
            if (key.getCode() == KeyCode.RIGHT) if (index < pag.getPageCount()) pag.setCurrentPageIndex(index + 1);
            if (key.getCode() == KeyCode.SPACE) if (index < pag.getPageCount()) pag.setCurrentPageIndex(index + 1);
        });
        
        this.setOnScroll((ScrollEvent event) -> {
            if (busyCounter != 0) return;
            if (isResize || forceReload) return;
            
            int index = pag.getCurrentPageIndex();
            if (event.getDeltaY() > 0) {
                if (index > 0) pag.setCurrentPageIndex(index - 1);
            } else {
                if (index < pag.getPageCount()) pag.setCurrentPageIndex(index + 1);
            }
        });
        
        pag.currentPageIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (busyCounter != 0) return;
            if (isResize || forceReload) return;
            _calcPaginator();
        });
        
        TMR.setCycleCount(Animation.INDEFINITE);
        TMR.play();
    }
    
    public void addToAlbums(ArrayList<DSAlbum> albums) {
        if (albums.isEmpty() || selectedElementsPool.isEmpty()) return;
        HibernateUtil.beginTransaction(hibSession);
        albums.stream().map((dsa) -> {
            if (dsa.getImages() == null) {
                Set<DSImage> s = new HashSet<>();
                s.addAll(selectedElementsPool);
                dsa.setImages(s);
            } else 
                dsa.getImages().addAll(selectedElementsPool);
            return dsa;
        }).forEach((dsa) -> {
            hibSession.save(dsa);
        });
        HibernateUtil.commitTransaction(hibSession);
    }
    
    public void selectNone() {
        selectedElementsPool.clear();
        isResize = true;
    }
    
    public ArrayList<DSImage> getSelectedHashes() {
        return selectedElementsPool;
    }
    
    private void _calcPaginator() { 
        itemTotalCount = itemCountOnOneLine * itemCountOnOneColoumn;
        if (elementsPool.size() < itemTotalCount) {
            for (int i=elementsPool.size(); i<itemTotalCount; i++) {
                elementsPool.add(new ImageListItem(elementListener));
            }
        }

        if ((itemTotalCount > 0) && (currCache.getCount() > 0)) {
            pag.setVisible(true);
            final int pageCount = (currCache.getCount() / itemTotalCount) + (((currCache.getCount() % itemTotalCount) > 0) ? 1 : 0);
            pag.setPageCount(pageCount);
            regenerateView();
        } else 
            pag.setVisible(false);
    }

    public long getTotalImagesCount() {
        return currCache.getCount();
    }
    
    public void clearTags() {
        tagsList = null;
        tagsNotList = null;
    }
    
    public void setTagLists(List<DSTag> tags, List<DSTag> tagsNot) {
        tagsList = tags;
        tagsNotList = tagsNot;
    }
    
    public void refresh() {
        switch (imageType) {
            case All:
                groupTitle = "Все картинки";
                break;
            case NotInAnyAlbum:
                groupTitle = "Не входящие в альбомы";
                break;
            case Notagged:
                groupTitle = "Картинки без тегов";
                break;
            case InAlbum:
                groupTitle = "Альбом #" + currentAlbumID;
                break;
            default:
                groupTitle = "Картинки";
                break;
        } 
        
        try { container.getChildren().clear(); } catch (java.lang.IllegalArgumentException e) {}
        isResize = true;
        forceReload = true;
    }
    
    public String getGroupTitle() {
        return groupTitle;
    }
    
    public List<DSImage> getImgListA(DSImageIDListCache.ImgType imgt, long albumID, int offset, int count) {
        final Set<Long> ids = new HashSet<>();
        for (int i=0; i<count; i++) {
            ids.add(currCache.getIDReverse(i + offset));
        }
        
        final List<DSImage> list = hibSession
                .createQuery("SELECT r FROM DSImage r WHERE r.imageID IN (:ids) ORDER BY r.imageID DESC")
                .setParameterList("ids", ids)
                .list();
        return list;
    }
    
    public void regenerateView() {
        if (itemTotalCount <= 0) return;

        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        itemSizeX = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
        itemSizeY = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
        
        int off = pag.getCurrentPageIndex() * itemTotalCount;
        if (forceReload) container.getChildren().clear();
        
        List<DSImage> list = getImgListA(imageType, currentAlbumID, off, itemTotalCount);
        
        int counter = 0;
        if (list != null) {
            for (DSImage dsi : list) {
                    elementsPool.get(counter).setImage(dsi);
                    elementsPool.get(counter).setSelected(selectedElementsPool.contains(elementsPool.get(counter).get()));
                    elementsPool.get(counter).myNumber = counter;
                    elementsPool.get(counter).setSizes(itemSizeX, itemSizeY);
                    if (forceReload) container.getChildren().add(elementsPool.get(counter));
                    counter++;
            }
            
            if (counter < itemTotalCount) {
                for (int i=counter; i<itemTotalCount; i++) {
                    elementsPool.get(i).setNullImage();
                    elementsPool.get(i).setSelected(false);
                    elementsPool.get(i).myNumber = -1;
                    elementsPool.get(counter).setSizes(itemSizeX, itemSizeY);
                    if (forceReload) container.getChildren().add(elementsPool.get(i));
                }
            }
        }
    }
    
    public long getAlbumID() {
        return currentAlbumID;
    }
        
    public Parent getPaginator() {
        return pag;
    }
    
    public void uploadSelected() {
        uploadDeque.addAll(selectedElementsPool);
        selectedElementsPool.clear();
        regenerateView();
    }

    public void initDB() {
        if (hibSession != null) return;
        
        final int threadsCount = SettingsUtil.getInt("mainPreviewThreadsCount", 4);
        hibSession = HibernateUtil.getCurrentSession();
        previewGenService = Executors.newFixedThreadPool(threadsCount);
        for (int i=0; i<threadsCount; i++) {
           previewGenService.submit(new PreviewGenerator());
        }
        
        DSImageIDListCache.reloadAllStatic();
    }
    
    public void setImageType(DSImageIDListCache.ImgType imgt, long albumID) {
        imageType = imgt;
        currentAlbumID = albumID;
        currCache = DSImageIDListCache.getStatic(imageType);
        if (albumID > 0) currCache.reload(currentAlbumID);
    }
    
    public void setImageType(DSImageIDListCache.ImgType imgt) {
        imageType = imgt;
        currCache = DSImageIDListCache.getStatic(imageType);
    }
    
    public void dispose() {
        isExit = true;
        previewGenService.shutdownNow();
    }
    
    public static PagedImageList get() {
        if (sPIL == null) sPIL = new PagedImageList();
        return sPIL;
    }
}

//-XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=45