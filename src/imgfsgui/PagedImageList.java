package imgfsgui;

import dataaccess.Lang;
import imgfs.ImgFS;
import imgfs.ImgFSPreviewGen.PreviewElement;
import imgfstabs.TabAlbumImageList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import jnekoimagesdb.GUITools;

public class PagedImageList extends FlowPane {
    private volatile int 
            itemSize                = 128,
            itemCountOnOneLine      = 0,
            itemCountOnOneColoumn   = 0,
            itemTotalCount          = 0,
            itemTotalRecordsCount   = 0,
            itemSpacer              = 4;
    
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
        public void OnSelect(boolean isSelected, byte[] itemHash);
        public void OnOpen(byte[] itemHash);
    }
    
    protected class ImageListItem extends Pane {
        private final ImageView 
            imageContainer = new ImageView(),
            selectedIcon = new ImageView(InfiniteFileList.ITEM_SELECTED);
        
        private byte[] 
                md5_im = null;
        
        private final VBox 
            imageVBox = new VBox(0);
        
        private final ImageListItemActionListener
                actionListener;
       
        public byte[] getMD5() {
            return md5_im;
        }
        
        public void setMD5(byte[] b) {
            md5_im = b;
        }
        
        public final void setNullImage() {
            imageContainer.setImage(InfiniteFileList.ITEM_NOTHING);
            imageContainer.setVisible(false);
            this.getChildren().clear();
            this.getStyleClass().clear();
        }
        
        public final void setImage(byte[] ref) {
            md5_im = ref;
            final byte[] content = ImgFS.getDB(ImgFS.PreviewType.previews.name()).get(ref);
            final byte[] decrypted = ImgFS.getCrypt().Decrypt(content);
            
            Image img;
            try {
                //System.err.println("decrypted="+decrypted.length+"; "+new String(decrypted));
                final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decrypted));
                final PreviewElement pe = (PreviewElement) ois.readObject();
                ois.close();
                img = pe.getImage(ImgFS.getCrypt(), "p120x120s"); // !!!!!!!!!!!!!!!!!!!! 
            } catch (IOException | ClassNotFoundException ex) {
                img = InfiniteFileList.ITEM_ERROR;
                Logger.getLogger(PagedImageList.class.getName()).log(Level.SEVERE, null, ex);
            }
            
             
            
            //final Image img = (content != null) ? new Image(new ByteArrayInputStream(decrypted)) : InfiniteFileList.ITEM_ERROR; 
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
                        actionListener.OnSelect(getSelected(), md5_im);
                    }
                } else {
                    actionListener.OnOpen(md5_im);
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
                public void OnSelect(boolean isSelected, byte[] itemHash) {

                }

                @Override
                public void OnOpen(byte[] itemHash) {

                }
            };
    
    private boolean isResize = false;
    private final Timeline TMR = new Timeline(new KeyFrame(Duration.millis(88), ae -> {
        if (isResize) {
            _calcPaginator();
            isResize = false;
        }
    }));
    
    @SuppressWarnings("LeakingThisInConstructor")
    public PagedImageList() {
        super();
        
        GUITools.setMaxSize(this, 9999, 9999);
        GUITools.setStyle(this, "PagedImageList", "root_pane");
        
        this.setAlignment(Pos.CENTER);
        this.setHgap(itemSpacer);
        this.setVgap(itemSpacer);
        
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
                this.getChildren().clear();
                itemCountOnOneLine = newValue.intValue() / (itemSize + itemSpacer);
                isResize = true;
            }
        });
        
        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                this.getChildren().clear();
                itemCountOnOneColoumn = newValue.intValue() / (itemSize + itemSpacer);
                isResize = true;
            }
        });
        
        pag.currentPageIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            THIS.getChildren().clear();
            _calcPaginator();
        });
        
        TMR.setCycleCount(Animation.INDEFINITE);
        TMR.play();
    }
    
    private void _calcPaginator() { 
        itemTotalCount = itemCountOnOneLine * itemCountOnOneColoumn;
        System.err.println("itemTotalCount="+itemTotalCount);
        
        if (elementsPool.size() < itemTotalCount) {
            for (int i=elementsPool.size(); i<itemTotalCount; i++) {
                elementsPool.add(new ImageListItem(elementListener));
            }
        }
        System.err.println("elementsPool.size()="+elementsPool.size());
        
        if ((itemTotalCount > 0) && (itemTotalRecordsCount > 0)) {
            pag.setVisible(true);
            //pag.setCurrentPageIndex(0);
            pag.setMaxPageIndicatorCount(itemTotalRecordsCount / itemTotalCount);
            regenerateView(0);
        } else 
            pag.setVisible(false);
    }

    public void regenerateView(long albumID) {
        if (itemTotalCount <= 0) return;
        int off = pag.getCurrentPageIndex() * itemTotalCount;
        
        try {
            final PreparedStatement ps;
            if (albumID < 0) {
                ps = conn.prepareStatement("SELECT * FROM images ORDER BY iid ASC LIMIT ?,?;");
                ps.setLong(1, off);
                ps.setLong(2, itemTotalCount);
            } else {
                /* TODO: добавить альбомы */
                ps = conn.prepareStatement("SELECT * FROM images ORDER BY iid ASC LIMIT ?,?;");
                ps.setLong(1, off);
                ps.setLong(2, itemTotalCount);
            }
            
            int counter = 0;
            final ResultSet rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    elementsPool.get(counter).setImage(rs.getBytes("xmd5"));
                    this.getChildren().add(elementsPool.get(counter));
                    counter++;
                }
                rs.close();
            }
            
            if (counter < itemTotalCount) {
                for (int i=counter; i<itemTotalCount; i++) {
                    elementsPool.get(i).setNullImage();
                    this.getChildren().add(elementsPool.get(counter));
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
        try {
            final PreparedStatement ps_count = conn.prepareStatement("SELECT COUNT(iid) FROM images;");
            final ResultSet rs_count = ps_count.executeQuery();
            if (rs_count != null) {
                if (rs_count.next()) {
                    itemTotalRecordsCount = rs_count.getInt(1);
                    System.err.println("itemTotalRecordsCount="+itemTotalRecordsCount);
                }
                rs_count.close();
            }
            ps_count.close();
        } catch (SQLException ex) {
            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
