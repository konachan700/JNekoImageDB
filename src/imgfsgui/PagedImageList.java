package imgfsgui;

import jnekoimagesdb.Lang;
import imgfs.ImgFS;
import imgfs.ImgFSPreviewGen.PreviewElement;
import imgfstabs.TabAlbumImageList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class PagedImageList extends GUIElements.SScrollPane {
    public static final int 
            IMAGES_ALL = -1,
            IMAGES_NOTAGGED = -2,
            IMAGES_NOT_IN_ALBUM = -3;
    
    private final FlowPane
            container = new FlowPane();
    
    private volatile int 
            itemSize                    = 128,
            itemCountOnOneLine          = 0,
            itemCountOnOneColoumn       = 0,
            itemTotalCount              = 0,
            itemCountOnOneLineOld       = -1,
            itemCountOnOneColoumnOld    = 0,
            itemTotalRecordsCount       = 0,
            itemSpacer                  = 4;
    
    private volatile long
            currentAlbumID = IMAGES_ALL;
    
    private final PagedImageList
            THIS = this;
    
    private final HBox
            dummyInfoBox = new HBox(8);
    
    private final Pagination
            pag = new Pagination();
    
    private Connection
            conn = null;
    
    private final ArrayList<ImageListItem>
            elementsPool = new ArrayList<>();
            
    
    protected interface ImageListItemActionListener {
        public void OnSelect(boolean isSelected, ImageLIByteArray itemHash);
        public void OnOpen(ImageLIByteArray itemHash);
    }
    
    protected class ImageLIByteArray {
        private byte[] itemHash;
        private long id = 0;
        
        public ImageLIByteArray() { 
            itemHash = null; 
        }
        
        public ImageLIByteArray(byte[] b, long _id) { 
            itemHash = b; 
            id = _id;
        }
        
        public long getID() {
            return id;
        }
        
        public void setID(long _id) {
            id = _id;
        }
        
        public byte[] get() { 
            return itemHash; 
        }
        
        public void set(byte[] b) { 
            itemHash = b; 
        }
        
        public int size() {  
            return itemHash.length; 
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof ImageLIByteArray) {
                return Arrays.equals(itemHash, ((ImageLIByteArray)o).get());
            } else 
                return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + Arrays.hashCode(this.itemHash);
            return hash;
        }
    }
    
    private final ArrayList<ImageLIByteArray>
            selectedElementsPool = new ArrayList<>();
    
    protected class ImageListItem extends Pane {
        private final ImageView 
                imageContainer = new ImageView(),
                selectedIcon = new ImageView(InfiniteFileList.ITEM_SELECTED);
        
        private ImageLIByteArray
                md5_array = null;
        
        private final VBox 
                imageVBox = new VBox(0);
        
        private final ImageListItemActionListener
                actionListener;
       
        public byte[] getMD5() {
            return md5_array.get();
        }
        
        public void setMD5(byte[] b) {
            md5_array.set(b);
        }
        
        public ImageLIByteArray get() {
            return md5_array;
        }
        
        public final void setNullImage() {
            imageContainer.setImage(InfiniteFileList.ITEM_NOTHING);
            imageContainer.setVisible(false);
            this.getChildren().clear();
            this.getStyleClass().clear();
        }
        
        public final void setImage(byte[] ref, long id) {
            md5_array = new ImageLIByteArray(ref, id);
            
            final byte[] content = ImgFS.getDB(ImgFS.PreviewType.previews.name()).get(ref);
            final byte[] decrypted = ImgFS.getCrypt().Decrypt(content);
            
            Image img;
            try {
                final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decrypted));
                final PreviewElement pe = (PreviewElement) ois.readObject();
                ois.close();
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                img = pe.getImage(ImgFS.getCrypt(), "p120x120s"); // !!!!!!!!!!!!!!!!!!!! DELETE IT !
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            } catch (IOException | ClassNotFoundException ex) {
                img = InfiniteFileList.ITEM_ERROR;
                Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
            }
 
            imageContainer.setImage(img);
            imageContainer.setVisible(true);
            if (this.getChildren().isEmpty()) addAll();
            GUITools.setStyle(this, "FileListItem", "root_pane");
        }
        
        public final void setSelected(boolean s) {
            if (imageContainer.isVisible()) selectedIcon.setVisible(s);
        }
        
        public final boolean getSelected() {
            return selectedIcon.isVisible();
        }
        
        @SuppressWarnings("LeakingThisInConstructor")
        public ImageListItem(ImageListItemActionListener al) {
            super();
            
            actionListener = al;
            
            GUITools.setStyle(this, "FileListItem", "root_pane");
            GUITools.setMaxSize(this, itemSize, itemSize);
            this.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 1) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        setSelected(!getSelected());
                        actionListener.OnSelect(getSelected(), md5_array);
                    }
                } else {
                    actionListener.OnOpen(md5_array);
                }
                event.consume();
            });

            imageContainer.setPreserveRatio(true);
            imageContainer.setSmooth(true);
            imageContainer.setCache(false);
            imageContainer.setImage(InfiniteFileList.ITEM_NOTHING);
                        
            GUITools.setStyle(imageVBox, "FileListItem", "imageVBox");
            GUITools.setMaxSize(imageVBox, itemSize, itemSize);
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
    
    private final ImageListItemActionListener
            elementListener = new ImageListItemActionListener() {
                @Override
                public void OnSelect(boolean isSelected, ImageLIByteArray itemHash) {
                    if (isSelected)
                        selectedElementsPool.add(itemHash);
                    else
                        selectedElementsPool.remove(itemHash);
                }

                @Override
                public void OnOpen(ImageLIByteArray itemHash) {

                }
            };
    
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
        
        final ImageView icon = new ImageView(InfiniteFileList.ICON_NOELEMENTS);
        final Label text = new Label(Lang.InfiniteImageList_no_elements_found);
        GUITools.setStyle(text, "FileListItem", "nullMessage");
        GUITools.setMaxSize(text, 9999, 48);
        text.setAlignment(Pos.CENTER_LEFT);
        
        GUITools.setStyle(dummyInfoBox, "FileListItem", "dummyInfoBox");
        GUITools.setMaxSize(dummyInfoBox, 9999, 48);
        dummyInfoBox.getChildren().addAll(icon, text);
        
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                itemCountOnOneLine = (newValue.intValue()-20) / (itemSize + itemSpacer);
                if (itemCountOnOneLineOld != itemCountOnOneLine) {
                    forceReload = true;
                    itemCountOnOneLineOld = itemCountOnOneLine;
                }
                isResize = true;
            }
        });
        
        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                itemCountOnOneColoumn = (newValue.intValue()-20) / (itemSize + itemSpacer);
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
    
    public void addToAlbums(ArrayList<Long> albums) {
        if (albums.isEmpty() || selectedElementsPool.isEmpty()) return;
        
        try {
            final PreparedStatement ps = conn.prepareStatement("MERGE INTO imggal KEY (img_iid,gal_iid) VALUES (?, ?)");
            albums.forEach((album) -> {
                selectedElementsPool.forEach((item) -> {
                    if (item.getID() > 0) {
                        try {
                            ps.setLong(1, item.getID());
                            ps.setLong(2, album);
                            ps.addBatch();
                        } catch (SQLException ex) {
                            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            });

            ps.executeBatch();
            ps.clearWarnings();
            ps.close();
            
/* test code for debug */
//            final PreparedStatement ps_count = conn.prepareStatement("SELECT COUNT(*) FROM imggal;");
//            final ResultSet rs_count = ps_count.executeQuery();
//            if (rs_count != null) {
//                if (rs_count.next()) {
//                    System.err.println("items in table 'imggal': "+rs_count.getInt(1));
//                }
//                rs_count.close();
//            }
//            ps_count.close();
        } catch (SQLException ex) {
            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void selectNone() {
        selectedElementsPool.clear();
        isResize = true;
    }
    
    public ArrayList<ImageLIByteArray> getSelectedHashes() {
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
    
    private void _getCount() {
        try {
            final PreparedStatement ps_c;
            switch ((int)currentAlbumID) {
                case IMAGES_ALL:
                    ps_c = conn.prepareStatement("SELECT COUNT(*) FROM images;");
                    itemTotalRecordsCount = (int) ImgFS.getSQLCount(ps_c);
                    ps_c.close();
                    break;
//                case IMAGES_NOTAGGED:
//                    
//                    break;
                case IMAGES_NOT_IN_ALBUM:
                    ps_c = conn.prepareStatement("SELECT COUNT(*) FROM images WHERE iid NOT IN (SELECT img_iid FROM imggal);");
                    itemTotalRecordsCount = (int) ImgFS.getSQLCount(ps_c);
                    ps_c.close();
                    break;
                default:
                    ps_c = conn.prepareStatement("SELECT COUNT(*) FROM imggal WHERE gal_iid=?;");
                    ps_c.setLong(1, currentAlbumID);
                    itemTotalRecordsCount = (int) ImgFS.getSQLCount(ps_c);
                    ps_c.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void refresh() {
        isResize = true;
        forceReload = true;
        container.getChildren().clear();
    }
    
    public synchronized void regenerateView(long albumID) {
        if (itemTotalCount <= 0) return;
        int off = pag.getCurrentPageIndex() * itemTotalCount;
        if (forceReload) container.getChildren().clear();
        
        try {
            final PreparedStatement ps;
            switch ((int)albumID) {
                case IMAGES_ALL:
                    ps = conn.prepareStatement("SELECT * FROM images ORDER BY iid ASC LIMIT ?,?;");
                    ps.setLong(1, off);
                    ps.setLong(2, itemTotalCount);
                    break;
//                case IMAGES_NOTAGGED:
//                    
//                    break;
                case IMAGES_NOT_IN_ALBUM:
                    ps = conn.prepareStatement("SELECT * FROM images WHERE iid NOT IN (SELECT img_iid FROM imggal) ORDER BY iid ASC LIMIT ?,?;");
                    ps.setLong(1, off);
                    ps.setLong(2, itemTotalCount);
                    break;
                default:
                    final StringBuilder sql = new StringBuilder();
                    sql.append("SELECT ");
                    sql.append("     imggal.img_iid  AS aiid, ");
                    sql.append("     imggal.gal_iid  AS aid, ");
                    sql.append("     images.iid      AS iid, ");
                    sql.append("     images.xmd5     AS md5 ");
                    sql.append("FROM ");
                    sql.append("     imggal ");
                    sql.append("LEFT JOIN ");
                    sql.append("     images ");
                    sql.append("WHERE ");
                    sql.append("     imggal.img_iid=images.iid AND ");
                    sql.append("     imggal.gal_iid=? ");
                    sql.append("ORDER BY iid ASC ");
                    sql.append("LIMIT ?,?;");

                    ps = conn.prepareStatement(sql.substring(0));
                    ps.setLong(1, albumID);
                    ps.setLong(2, off);
                    ps.setLong(3, itemTotalCount);
                    break;
            }
            
            int counter = 0;
            final ResultSet rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    elementsPool.get(counter).setImage(rs.getBytes("xmd5"), rs.getLong("iid"));
                    elementsPool.get(counter).setSelected(selectedElementsPool.contains(elementsPool.get(counter).get()));
                    if (forceReload) container.getChildren().add(elementsPool.get(counter));
                    counter++;
                }
                rs.close();
            }
            
            if (counter < itemTotalCount) {
                for (int i=counter; i<itemTotalCount; i++) {
                    elementsPool.get(i).setNullImage();
                    elementsPool.get(counter).setSelected(false);
                    if (forceReload) container.getChildren().add(elementsPool.get(i));
                }
            }
            
            ps.clearWarnings();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Parent getPaginator() {
        return pag;
    }
    
    public void initDB() {
        conn = ImgFS.getH2Connection();
//        try {
//            final PreparedStatement ps_count = conn.prepareStatement("SELECT COUNT(iid) FROM images;");
//            final ResultSet rs_count = ps_count.executeQuery();
//            if (rs_count != null) {
//                if (rs_count.next()) {
//                    itemTotalRecordsCount = rs_count.getInt(1);
//                    System.err.println("itemTotalRecordsCount="+itemTotalRecordsCount);
//                }
//                rs_count.close();
//            }
//            ps_count.close();
//        } catch (SQLException ex) {
//            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
