package jnekoimagesdb.ui.md.imagelist;

import com.sun.javafx.scene.control.skin.PaginationSkin;
import java.awt.Dimension;
import java.awt.Toolkit;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import jnekoimagesdb.core.threads.UPools;
import jnekoimagesdb.core.threads.UThreadWorker;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.md.dialogs.imageview.ImageViewDialog;
import jnekoimagesdb.ui.md.paginator.Paginator;
import jnekoimagesdb.ui.md.paginator.PaginatorActionListener;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;

public class PagedImageList extends ScrollPane implements PagedImageListElementActionListener, PaginatorActionListener {
    public static PagedImageList
            sPIL = null;
        
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(PagedImageList.class);

    private volatile int
            busyCounter = 0;
    
    private DSImageIDListCache.ImgType
            imageType = DSImageIDListCache.ImgType.All;
    
    private DSImageIDListCache
            currCache = DSImageIDListCache.getAll();

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

//    private final Pagination
//            pag = new Pagination();
    
    private final Paginator 
            newPaginator = new Paginator(this);
    
    private final ArrayList<PagedImageListElement>
            elementsPool = new ArrayList<>();
    
    private final LinkedBlockingDeque<DSImage>
            uploadDeque = new LinkedBlockingDeque<>(),
            prevGenDeque = new LinkedBlockingDeque<>();
    
    private final UThreadWorker
            workerPreviewGen = (thread) -> {
                try {
                    final DSImage currDSI = prevGenDeque.pollLast();
                    if (currDSI != null) {
                        final Image img = XImgDatastore.createPreviewEntryFromExistDBFile(currDSI.getMD5(), XImg.PreviewType.previews);
                        if (img != null) {
                            Platform.runLater(() -> { 
                                elementsPool.forEach(c -> {
                                    if (c.equals(currDSI)) {
                                        busyCounter--;
                                        c.setImage(img);
                                    }
                                });
                            });
                        } else 
                            busyCounter--;
                    } else 
                        return false;
                } catch (InterruptedException | IOException ex) {
                    busyCounter--;
                    logger.error(ex.getMessage());
                }
                return true;
            },
            
            workerUploader = (thread) -> {
                try {
                    final DSImage upDSI = uploadDeque.pollLast();
                    if (upDSI != null) {
                        XImgDatastore.copyToExchangeFolderFromDB(SettingsUtil.getPath("pathBrowserExchange"), upDSI);
                    } else 
                        return false;
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
                return true;
            };

    private List<DSTag>
            tagsList = null,
            tagsNotList = null;

    private final ArrayList<DSImage>
            selectedElementsPool = new ArrayList<>();

    private Session 
            hibSession = null;
    
    private boolean 
            isResize = false,
            forceReload = false;
    
    private final Timeline TMR = new Timeline(new KeyFrame(Duration.millis(88), ae -> {
        newPaginator.setDisable(busyCounter != 0);
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
        this.getStyleClass().addAll("pil_root_sp_pane");

        container.getStyleClass().addAll("pil_root_pane", "pil_max_width", "pil_max_height");
        container.setAlignment(Pos.CENTER);
        container.setHgap(itemSpacer);
        container.setVgap(itemSpacer);

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

            if (key.getCode() == KeyCode.LEFT)  newPaginator.pagPrev();
            if (key.getCode() == KeyCode.RIGHT) newPaginator.pagNext();
            if (key.getCode() == KeyCode.SPACE) newPaginator.pagNext();
        });
        
        this.setOnScroll((ScrollEvent event) -> {
            if (busyCounter != 0) return;
            if (isResize || forceReload) return;
            
            if (event.getDeltaY() > 0) {
                newPaginator.pagPrev();
            } else {
                newPaginator.pagNext();
            }
        });
        
        TMR.setCycleCount(Animation.INDEFINITE);
        TMR.play();
    }
    
    public void addToAlbums(Collection<DSAlbum> albums) {
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
                elementsPool.add(new PagedImageListElement(itemSizeX, itemSizeY, this));
            }
        }

        if ((itemTotalCount > 0) && (currCache.getCount() > 0)) {
            newPaginator.setVisible(true);
            final int pageCount = (currCache.getCount() / itemTotalCount) + (((currCache.getCount() % itemTotalCount) > 0) ? 1 : 0);
            newPaginator.setPageCount(pageCount);
            regenerateView();
        } else 
            newPaginator.setVisible(false);
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
        
        int off = (newPaginator.getCurrentPageIndex() - 1) * itemTotalCount;
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
        return newPaginator;
    }
    
    public void uploadSelected() {
        uploadDeque.addAll(selectedElementsPool);
        UPools.getGroup(UPools.PREVIEW_POOL).resume();
        selectedElementsPool.clear();
        regenerateView();
    }

    public void initDB() {
        if (hibSession != null) return;

        hibSession = HibernateUtil.getCurrentSession();
        DSImageIDListCache.reloadAllStatic();
        
        UPools.addWorker(UPools.PREVIEW_POOL, workerPreviewGen);
        UPools.addWorker(UPools.PREVIEW_POOL, workerUploader);
        UPools.getGroup(UPools.PREVIEW_POOL).resume();
    }
    
    public void setImageType(DSImageIDListCache.ImgType imgt, long albumID) {
        newPaginator.setCurrentPageIndex(1);
        imageType = imgt;
        currentAlbumID = albumID;
        currCache = DSImageIDListCache.getStatic(imageType);
        if (albumID > 0) currCache.reload(currentAlbumID);
    }
    
    public void setImageType(DSImageIDListCache.ImgType imgt) {
        newPaginator.setCurrentPageIndex(1);
        imageType = imgt;
        currCache = DSImageIDListCache.getStatic(imageType);
    }

    public static PagedImageList get() {
        if (sPIL == null) sPIL = new PagedImageList();
        return sPIL;
    }
    
    @Override
    public void OnSelect(boolean isSelected, DSImage item) {
        if (isSelected)
            selectedElementsPool.add(item);
        else
            selectedElementsPool.remove(item);
    }

    @Override
    public void OnOpen(DSImage item, PagedImageListElement it) {
        if (it.myNumber >= 0) {
            int off = (newPaginator.getCurrentPageIndex() - 1) * itemTotalCount;

            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int)(screenSize.getWidth() * 0.8);
            int height = (int)(screenSize.getHeight() * 0.8);

            final ImageViewDialog imgd = new ImageViewDialog(width, height);
            imgd.show(currCache.getCount() - 1 - (it.myNumber + off), imageType); 
        }
    }

    @Override
    public void OnError(DSImage item) {
        prevGenDeque.add(item);
        busyCounter++;
    }

    @Override
    public void OnPageChange(int page, int pages) {
        if (busyCounter != 0) return;
        if (isResize || forceReload) return;
        _calcPaginator();
    }
}

//-XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=45