package img.gui;

import datasources.DSAlbum;
import datasources.DSImage;
import datasources.HibernateUtil;
import img.gui.dialogs.DialogImageView;
import jnekoimagesdb.Lang;
import img.XImg;
import img.XImgDatastore;
import img.XImgImages;
import img.XImgPreviewGen;
import img.XImgPreviewGen.PreviewElement;
import img.gui.elements.GUIElements;
import static img.gui.elements.GUIElements.ICON_NOELEMENTS;
import static img.gui.elements.GUIElements.ITEM_ERROR;
import static img.gui.elements.GUIElements.ITEM_NOTHING;
import static img.gui.elements.GUIElements.ITEM_SELECTED;
import img.gui.elements.SScrollPane;
import java.awt.Container;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import jnekoimagesdb.GUITools;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class PagedImageList extends SScrollPane {
    private class PreviewGenerator implements Runnable {
        private final XImgImages
                imgConv = new XImgImages();

        @Override
        public void run() {
            
            
            while (true) {
                
                
//                try {
//                    final String pathStr = XImgDatastore.getPathString(ref);
//                    final Path path = FileSystems.getDefault().getPath(pathStr);
//                    final byte[] fileCC = Files.readAllBytes(path);
//                    final byte[] decryptedCC = XImg.getCrypt().Decrypt(fileCC);
//                    
//                    final java.awt.Image image2 = Toolkit.getDefaultToolkit().createImage(decryptedCC);
//                    final MediaTracker mediaTracker = new MediaTracker(new Container()); 
//                    mediaTracker.addImage(image2, 0); 
//                    mediaTracker.waitForAll();
//                    
//                    final byte[] preview = imgConv.getPreviewFS(image2.getGraphics());
//                    
//                    
//                    
//                } catch (IOException | InterruptedException ex1) {
//                    Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex1);
//                    imageContainer.setImage(ITEM_ERROR);
//                    imageContainer.setVisible(true);
//                }
                
            }  
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
    
    private final LinkedBlockingDeque<Path>
            prevGenDeque = new LinkedBlockingDeque<>();

    protected interface ImageListItemActionListener {
        public void OnSelect(boolean isSelected, DSImage item);
        public void OnOpen(DSImage item, ImageListItem it);
    }
    
    private final ArrayList<DSImage>
            selectedElementsPool = new ArrayList<>();
    
    protected class ImageListItem extends Pane {
        private final ImageView 
                imageContainer = new ImageView(),
                selectedIcon = new ImageView(ITEM_SELECTED);
        
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
            imageContainer.setImage(ITEM_NOTHING);
            imageContainer.setVisible(false);
            this.getChildren().clear();
            this.getStyleClass().clear();
        }
        
        public final void setImage(byte[] ref, long id) {
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
            imageContainer.setFitHeight(XImg.getPSizes().getPrimaryPreviewSize().getHeight());
            imageContainer.setFitWidth(XImg.getPSizes().getPrimaryPreviewSize().getWidth());
            
            PreviewElement peDB;
            try {
                    peDB = XImgDatastore.readPreviewEntry(ref);
                    final Image im = peDB.getImage(XImg.getCrypt(), XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
                    if (im != null) {
                        imageContainer.setImage(im);
                        imageContainer.setVisible(true);
                    } else 
                        throw new IOException();
            } catch (IOException | ClassNotFoundException ex) {
                try {
                    final String pathStr = XImgDatastore.getPathString(ref);
                    final Path path = FileSystems.getDefault().getPath(pathStr);
                    prevGenDeque.add(path);
                    imageContainer.setImage(GUIElements.ITEM_LOADING);
                    imageContainer.setVisible(true);
                } catch (IOException ex1) {
                    Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex1);
                    imageContainer.setImage(ITEM_ERROR);
                    imageContainer.setVisible(true);
                }
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
            imageContainer.setImage(ITEM_NOTHING);
                        
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
        
        final ImageView icon = new ImageView(ICON_NOELEMENTS);
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
            if (isResize || forceReload) return;
            
            int index = pag.getCurrentPageIndex();
            if (event.getDeltaY() > 0) {
                if (index > 0) pag.setCurrentPageIndex(index - 1);
            } else {
                if (index < pag.getPageCount()) pag.setCurrentPageIndex(index + 1);
            }
        });
        
        pag.currentPageIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
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
    }
    
    public long getTotalImagesCount() {
        _getCount();
        return itemTotalRecordsCount;
    }
    
    public long getImgCount(long _albumID) {
        Number _itemTotalRecordsCount;
        switch ((int)_albumID) {
            case IMAGES_ALL:
                _itemTotalRecordsCount = (Number) hibSession.createCriteria(DSImage.class).setProjection(Projections.rowCount()).uniqueResult();
                return _itemTotalRecordsCount.longValue();
            case IMAGES_NOT_IN_ALBUM: 
                _itemTotalRecordsCount = (Number) hibSession.createQuery("SELECT COUNT(*) FROM DSImage r WHERE r.albums IS EMPTY").uniqueResult();
                return _itemTotalRecordsCount.longValue();
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
        isResize = true;
        forceReload = true;
        container.getChildren().clear();
    }
    
    public List<DSImage> getImgList(long albumID, int offset, int count) {
        List<DSImage> list;
        switch ((int)albumID) {
            case IMAGES_ALL:
                list = (List<DSImage>) hibSession
                        .createCriteria(DSImage.class)
                        .setFirstResult(offset)
                        .setMaxResults(count)
                        .list();
                break;
            case IMAGES_NOT_IN_ALBUM: 
                list = hibSession.createQuery("SELECT r FROM DSImage r WHERE r.albums IS EMPTY")
                        .setFirstResult(offset)
                        .setMaxResults(count)
                        .list();
                break;
            default:
                list = hibSession
                        .createCriteria(DSImage.class)
                        .createCriteria("albums")
                        .add(Restrictions.eq("albumID", albumID))
                        .setFirstResult(offset)
                        .setMaxResults(count)
                        .list();
                break;
        }
        return list;
    }
    
    public synchronized void regenerateView(long albumID) {
        if (itemTotalCount <= 0) return;

        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        itemSizeX = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
        itemSizeY = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
        
        int off = pag.getCurrentPageIndex() * itemTotalCount;
        if (forceReload) container.getChildren().clear();
        
        List<DSImage> list = getImgList(albumID, off, itemTotalCount);
        
        int counter = 0;
        if (list != null) {
            for (DSImage dsi : list) {
                    elementsPool.get(counter).setImage(dsi.getMD5(), dsi.getImageID());
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
    
    public void initDB() {
        hibSession = HibernateUtil.getCurrentSession();
    }
}
