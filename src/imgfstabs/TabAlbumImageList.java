package imgfstabs;

import jnekoimagesdb.Lang;
import imgfsgui.AlbumList;
import imgfsgui.GUIElements.STabTextButton;
import imgfsgui.GUIElements.SEVBox;
import imgfsgui.GUIElements.SFLabel;
import imgfsgui.PagedImageList;
import imgfsgui.ToolsPanelTop;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import jnekoimagesdb.GUITools;

public class TabAlbumImageList extends SEVBox {
    public static final int
            HEADER_VSIZE = 27,
            HBUTTON_HSIZE = 150,
            ALBTITLE_HSIZE = 210,
            BTN_LVL_UP = 1;
    
    public static final Image 
            IMG24_LEVEL_UP = GUITools.loadIcon("lvlup-48");
    
    private long 
            currentAlbumID = -1, 
            imagesCount = 0, 
            albumsCount = 0;

    private final STabTextButton 
            album, images;

    private final Pane
            topToolbar, bottomPanel;
    
    private final HBox 
            header = new HBox(4);
    
    private final PagedImageList 
            pil = new PagedImageList();

    private final SFLabel
            bottomPanelForAlbums = new SFLabel("Статистика альбома", 128, 9999, 24, 24, "bottomPanelForAlbums", "TabAlbumImageList"),
            albumName = new SFLabel(Lang.TabAlbumImageList_root_album, ALBTITLE_HSIZE, ALBTITLE_HSIZE, HEADER_VSIZE, HEADER_VSIZE, "albumName", "TabAlbumImageList");

    private final ToolsPanelTop 
            panelTop;

    private final AlbumList 
            myAL = new AlbumList(new AlbumList.AlbumListActionListener() {
                @Override
                public void OnAlbumChange(String newAlbumName, long ID, long PID) {
                    albumName.setText(newAlbumName);
                }

                @Override
                public void OnListCompleted(long count, long ID, long PID) {
                    currentAlbumID = ID;
                    albumsCount = count;
                    pil.setAlbumID(currentAlbumID);
                    imagesCount = pil.getTotalImagesCount();
                    bottomPanelForAlbums.setText(String.format(Lang.TabAlbumImageList_info_format, albumsCount, imagesCount)); 
                    _clear();
                    _initAlbGUI();
                }   
            });
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TabAlbumImageList(Pane _topToolbar, Pane _bottomToolbar) {
        super(0, 9999, 9999);
                
        bottomPanel = _bottomToolbar;
        topToolbar = _topToolbar;
        
        GUITools.setStyle(header, "TabAlbumImageList", "header");
        header.setMaxSize(9999, HEADER_VSIZE);
        header.setPrefSize(9999, HEADER_VSIZE);
        header.setMinSize(HBUTTON_HSIZE * 3, HEADER_VSIZE);
        header.setAlignment(Pos.CENTER);

        album = new STabTextButton(Lang.AlbumImageList_Albums, 1, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            _album();
        }, "STabTextButton_green");
        
        images = new STabTextButton(Lang.AlbumImageList_Images, 2, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            _clear();
            _initImgGUI();
            _images();
        }, "STabTextButton_red");
        
        panelTop = new ToolsPanelTop((index) -> {
            switch (index) {
                case BTN_LVL_UP:
                    myAL.levelUp();
                    break;
            }
        });
        
        albumName.setAlignment(Pos.CENTER);
    }
    
    private void _initAlbGUI() {
        topToolbar.getChildren().add(panelTop);
        bottomPanel.getChildren().add(bottomPanelForAlbums);
        if (currentAlbumID > 0) {
            panelTop.addButton(IMG24_LEVEL_UP, BTN_LVL_UP);
            header.getChildren().addAll(albumName, GUITools.getSeparator(), album, images);
        } else {
            header.getChildren().addAll(albumName, GUITools.getSeparator(), album);
        }
        this.getChildren().addAll(header, myAL);
    }
    
    private void _initImgGUI() {
        if (currentAlbumID <= 0) return;
        
        topToolbar.getChildren().add(panelTop);
        bottomPanel.getChildren().add(pil.getPaginator());
        header.getChildren().addAll(albumName, GUITools.getSeparator(), album, images);
        this.getChildren().addAll(header, pil);
    }
    
    public void initDB() {
        myAL.initDB();
        pil.initDB();
    }
    
    public void refresh() {
        _album();
    }
    
    private void _clear() {
        this.getChildren().clear();
        header.getChildren().clear();
        topToolbar.getChildren().clear();
        bottomPanel.getChildren().clear();
        panelTop.clearAll();
    }
    
    private void _images() {
        pil.setAlbumID(currentAlbumID);
        pil.refresh();
        imagesCount = pil.getTotalImagesCount();
        bottomPanelForAlbums.setText(String.format(Lang.TabAlbumImageList_info_format, albumsCount, imagesCount));
    }
    
    private void _album() {
        myAL.refresh();
    }
    
    public Parent getBottomPanel() {
        return bottomPanelForAlbums;
    }
    
    public void setAlbumTabVisible(boolean v) {
        album.setVisible(v);
    }
    
    public void setImagesTabVisible(boolean v) {
        images.setVisible(v);
    }
    
    public void setAlbumName(String s) {
        album.setText((s.length() < 32) ? s : s.substring(0, 29) + "...");
    }
}
