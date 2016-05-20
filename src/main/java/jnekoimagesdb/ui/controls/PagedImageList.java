package jnekoimagesdb.ui.controls;

import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.core.img.XImgPreviewGen;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.Lang;
import jnekoimagesdb.ui.controls.dialogs.DialogImageView;
import jnekoimagesdb.ui.controls.elements.SScrollPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.domain.DSTag;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.LoggerFactory;

public class PagedImageList extends SScrollPane {
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(PagedImageList.class);
    
    private volatile boolean 
            isExit = false;
    
    private volatile int
            busyCounter = 0;
    
    private class PreviewGenerator implements Runnable {
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) {
                while (XImg.getPSizes().getPrimaryPreviewSize() == null) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PagedFileList.class.getName()).log(Level.SEVERE, null, ex);
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
                        setImage(img, currDSI);
                    } else {
                        final DSImage upDSI = uploadDeque.pollLast();
                        if (upDSI != null) {
                            try {
                                XImgDatastore.copyToExchangeFolderFromDB(SettingsUtil.getPath("pathBrowserExchange"), upDSI);
                            } catch (IOException ex) {
                                Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else
                            try { Thread.sleep(300); } catch (Exception e) { return; }
                    }
                } catch (InterruptedException ex) {
                    busyCounter--;
                    //Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                } catch (IOException ex) {
                    busyCounter--;
                    Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public static final int 
            IMAGES_ALL = -1,
            IMAGES_NOTAGGED = -2,
            IMAGES_NOT_IN_ALBUM = -3;
    
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
            itemTotalRecordsCount       = 0,
            itemSpacer                  = 4;
    
    private volatile long
            currentAlbumID = IMAGES_ALL;

    private final HBox
            dummyInfoBox = new HBox(8);
    
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
                imageContainer = new ImageView(),
                selectedIcon = new ImageView(GUITools.loadIcon("selected-32"));
        
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
            GUITools.setStyle(this, "FileListItem", "root_pane");
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
            GUITools.setStyle(this, "FileListItem", "root_pane");
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
        
        @SuppressWarnings("LeakingThisInConstructor")
        public ImageListItem(ImageListItemActionListener al) {
            super();
            
            actionListener = al;
            
            GUITools.setStyle(this, "FileListItem", "root_pane");
            GUITools.setMaxSize(this, itemSizeX, itemSizeY);
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
                        
            GUITools.setStyle(imageVBox, "FileListItem", "imageVBox");
            GUITools.setMaxSize(imageVBox, itemSizeX, itemSizeY);
            imageVBox.getChildren().add(imageContainer);
            imageVBox.setAlignment(Pos.CENTER);
            
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
    
    private final DialogImageView 
            div = new DialogImageView(this);
    
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
                        div.setAlbumID(currentAlbumID);
                        div.setImageIndex(it.myNumber + off);
                        div.show();
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
            _getCount();
            _calcPaginator();
            isResize = false;
            forceReload = false;
        }
    }));

    @SuppressWarnings("LeakingThisInConstructor")
    public PagedImageList() {
        super();
        this.setVbarPolicy(ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setContent(container);
         
        GUITools.setMaxSize(container, 9999, 9999);
        GUITools.setStyle(container, "PagedImageList", "root_pane");
        
        container.setAlignment(Pos.CENTER);
        container.setHgap(itemSpacer);
        container.setVgap(itemSpacer);
        
        pag.setMaxSize(9999, 24);
        pag.setMinSize(128, 24);
        pag.setPrefSize(9999, 24);
        
        final ImageView icon = new ImageView(GUITools.loadIcon("delete-gray-48"));
        final Label text = new Label(Lang.InfiniteImageList_no_elements_found);
        GUITools.setStyle(text, "FileListItem", "nullMessage");
        GUITools.setMaxSize(text, 9999, 48);
        text.setAlignment(Pos.CENTER_LEFT);
        
        GUITools.setStyle(dummyInfoBox, "FileListItem", "dummyInfoBox");
        GUITools.setMaxSize(dummyInfoBox, 9999, 48);
        dummyInfoBox.getChildren().addAll(icon, text);
        
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

        if ((itemTotalCount > 0) && (itemTotalRecordsCount > 0)) {
            
            pag.setVisible(true);
            final int pageCount = (itemTotalRecordsCount / itemTotalCount) + (((itemTotalRecordsCount % itemTotalCount) > 0) ? 1 : 0);
            pag.setPageCount(pageCount);
            regenerateView(currentAlbumID);
        } else 
            pag.setVisible(false);
    }

    public void setAlbumID(long albumID) {
        currentAlbumID = albumID;
        selectedElementsPool.clear();
    }
    
    public long getTotalImagesCount() {
        _getCount();
        return itemTotalRecordsCount;
    }
    
    public void clearTags() {
        tagsList = null;
        tagsNotList = null;
    }
    
    public void setTagLists(List<DSTag> tags, List<DSTag> tagsNot) {
        tagsList = tags;
        tagsNotList = tagsNot;
    }
    
    private long getTagsCount() {
        Number _itemTotalRecordsCount = 0;
        
        final Set<DSImage> tagsNot = new HashSet<>();
        if (tagsNotList != null) tagsNotList.parallelStream().forEach(el -> { tagsNot.addAll(el.getImages()); });
        
        final Set<DSImage> tags = new HashSet<>();
        if (tagsList != null) tagsList.parallelStream().forEach(el -> { tags.addAll(el.getImages()); });
        
        if ((tagsList != null) && (tagsNotList != null)) {
            _itemTotalRecordsCount = (Number) hibSession.createQuery("SELECT COUNT(*) FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID NOT IN (:tags2)")
                    .setParameterList("tags1", tags)
                    .setParameterList("tags2", tagsNot)
                    .uniqueResult();
        } else if ((tagsList == null) && (tagsNotList != null)) {
            _itemTotalRecordsCount = (Number) hibSession.createQuery("SELECT COUNT(*) FROM DSImage r WHERE r.imageID NOT IN (:tags)")
                    .setParameterList("tags", tagsNot)
                    .uniqueResult();
        } else if ((tagsList != null) && (tagsNotList == null)) {
            _itemTotalRecordsCount = (Number) hibSession.createQuery("SELECT COUNT(*) FROM DSImage r WHERE r.imageID IN (:tags)")
                    .setParameterList("tags", tags)
                    .uniqueResult();
        }
        return _itemTotalRecordsCount.longValue();
    }
    
    public long getImgCount(long _albumID) {
        logger.info("## getImgCount start time: "+System.currentTimeMillis());
        Number _itemTotalRecordsCount;
        switch ((int)_albumID) {
            case IMAGES_ALL:
                final boolean notNullTags = (tagsList != null) || (tagsNotList != null);
                final boolean notEmptyTags = (notNullTags) ? ((!tagsList.isEmpty()) || (!tagsNotList.isEmpty())) : false;
                if (notNullTags && notEmptyTags) {
                    _itemTotalRecordsCount = getTagsCount();
                    logger.debug(_itemTotalRecordsCount.toString());
                    return _itemTotalRecordsCount.longValue();
                } else {
                    return DSImageIDListCache.getAll().getCount();
                }
            case IMAGES_NOT_IN_ALBUM: 
                return DSImageIDListCache.getWOAlbums().getCount();
            case IMAGES_NOTAGGED:   
                return DSImageIDListCache.getNotagged().getCount();
            default:
                _itemTotalRecordsCount = (Number) hibSession
                        .createCriteria(DSImage.class)
                        .createCriteria("albums")
                        .add(Restrictions.eq("albumID", _albumID))
                    .setProjection(Projections.rowCount()).uniqueResult();
                return _itemTotalRecordsCount.longValue();  
        }
    }
    
    private void _getCount() {
        itemTotalRecordsCount = (int) getImgCount(currentAlbumID);
    }
    
    public void refresh() {
        try { container.getChildren().clear(); } catch (java.lang.IllegalArgumentException e) {}
        isResize = true;
        forceReload = true;
    }
    
    private List<DSImage> getTagsList(int offset, int count) {
        // Реализация мне очень не нравится, попахивает индусятиной. Но как сделать иначе, ума не приложу.        
        List<DSImage> list; 
        
        final Set<DSImage> tagsNot = new HashSet<>();
        if (tagsNotList != null) tagsNotList.parallelStream().forEach(el -> { tagsNot.addAll(el.getImages()); });
        
        final Set<DSImage> tags = new HashSet<>();
        if (tagsList != null) tagsList.parallelStream().forEach(el -> { tags.addAll(el.getImages()); });

        if ((tagsList != null) && (tagsNotList != null)) {
            list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID NOT IN (:tags2)")
                    .setParameterList("tags1", tags)
                    .setParameterList("tags2", tagsNot)
                    .setFirstResult(offset)
                    .setMaxResults(count)
                    .list();
            return list;
        } else if ((tagsList == null) && (tagsNotList != null)) {
            list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.imageID NOT IN (:tags)")
                    .setParameterList("tags", tagsNot)
                    .setFirstResult(offset)
                    .setMaxResults(count)
                    .list();
            return list;
        } else if ((tagsList != null) && (tagsNotList == null)) {
            list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.imageID IN (:tags)")
                    .setParameterList("tags", tags)
                    .setFirstResult(offset)
                    .setMaxResults(count)
                    .list();
            return list;
        }

        return new ArrayList<>();
    }
    
    public List<DSImage> getImgListA(long albumID, int offset, int count) {
        final DSImageIDListCache dsc;
        switch ((int) albumID) {
            case IMAGES_ALL:
                dsc = DSImageIDListCache.getAll();
                break;
            case IMAGES_NOT_IN_ALBUM:
                dsc = DSImageIDListCache.getWOAlbums();
                break;
            case IMAGES_NOTAGGED:
                dsc = DSImageIDListCache.getNotagged();
                break;
            default:
                dsc = DSImageIDListCache.getInAlbum();
                dsc.reload(albumID);
                break;
        }
        
        final Set<Long> ids = new HashSet<>();
        for (int i=0; i<count; i++) {
            ids.add(dsc.getIDReverse(i + offset));
        }
        
        final List<DSImage> list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.imageID IN (:ids) ORDER BY r.imageID ASC")
                .setParameterList("ids", ids)
                .list();
        return list;
    }
    
    
    
    public List<DSImage> getImgList__OLD(long albumID, int offset, int count) {
        logger.info("## getImgList start time: "+System.currentTimeMillis());
        List<DSImage> list;
        switch ((int)albumID) {
            case IMAGES_ALL:
                final boolean notNullTags = (tagsList != null) || (tagsNotList != null);
                final boolean notEmptyTags = (notNullTags) ? ((!tagsList.isEmpty()) || (!tagsNotList.isEmpty())) : false;
                if (notNullTags && notEmptyTags) {
                    list = getTagsList(offset, count);
                } else {
                    final Set<Long> ids = new HashSet<>();
                    for (int i=0; i<count; i++) {
                        ids.add(DSImageIDListCache.getAll().getIDReverse(i + offset));
                    }
                    
                    list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.imageID IN (:ids) ORDER BY r.imageID ASC")
                            .setParameterList("ids", ids)
                            .list();
                }
                break;
            case IMAGES_NOT_IN_ALBUM: 
                final Set<Long> ids = new HashSet<>();
                for (int i=0; i<count; i++) {
                    ids.add(DSImageIDListCache.getWOAlbums().getIDReverse(i + offset));
                }

                list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.imageID IN (:ids) ORDER BY r.imageID ASC")
                        .setParameterList("ids", ids)
                        .list();
                break;
            case IMAGES_NOTAGGED: 
                list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.tags IS EMPTY ORDER BY r.imageID DESC")
                        .setFirstResult(offset)
                        .setMaxResults(count)
                        .list();
                break;
            default:
                list = hibSession
                        .createCriteria(DSImage.class)
                        .createCriteria("albums")
                        .add(Restrictions.eq("albumID", albumID))
                        //.addOrder(Order.desc("imageID"))
                        .setFirstResult(offset)
                        .setMaxResults(count)
                        .list();
                break;
        }
        logger.info("## getImgList   end time: "+System.currentTimeMillis());
        return list;
    }
    
    public synchronized void regenerateView(long albumID) {
        if (itemTotalCount <= 0) return;

        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        itemSizeX = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
        itemSizeY = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
        
        int off = pag.getCurrentPageIndex() * itemTotalCount;
        if (forceReload) container.getChildren().clear();
        
        List<DSImage> list = getImgListA(albumID, off, itemTotalCount);
        
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
        regenerateView(currentAlbumID);
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
    
    public void dispose() {
        isExit = true;
        previewGenService.shutdownNow();
    }
}

//-XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=45