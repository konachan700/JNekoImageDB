package img.gui.tabs;

import datasources.DSAlbum;
import img.XImg;
import img.gui.dialogs.DialogAlbumSelect;
import jnekoimagesdb.Lang;
import img.gui.AlbumList;
import img.gui.PagedImageList;
import img.gui.ToolsPanelTop;
import static img.gui.dialogs.XImageUpload.IMG64_DELETE;
import static img.gui.dialogs.XImageUpload.IMG64_SELECT_NONE;
import static img.gui.elements.GUIElements.BTN_DEL;
import static img.gui.elements.GUIElements.BTN_SELNONE;
import img.gui.elements.SEVBox;
import img.gui.elements.SFLabel;
import img.gui.elements.STabTextButton;
import static img.gui.tabs.TabAllImages.BTN_ADD_TAGS;
import static img.gui.tabs.TabAllImages.BTN_TO_ALBUM;
import static img.gui.tabs.TabAllImages.BTN_TO_TEMP;
import java.util.ArrayList;
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

    private Pane
            topToolbar = null, bottomPanel = null;
    
    private final HBox 
            header = new HBox(4);
    
    private final PagedImageList
            pil = XImg.getPagedImageList();

    private final SFLabel
            bottomPanelForAlbums = new SFLabel("Статистика альбома", 128, 9999, 24, 24, "bottomPanelForAlbums", "TabAlbumImageList"),
            albumName = new SFLabel(Lang.TabAlbumImageList_root_album, ALBTITLE_HSIZE, ALBTITLE_HSIZE, HEADER_VSIZE, HEADER_VSIZE, "albumName", "TabAlbumImageList");

    private final ToolsPanelTop 
            panelTopAlb, panelTopImg;
    
    private final DialogAlbumSelect
            dis = new DialogAlbumSelect();

    private final AlbumList 
            myAL = new AlbumList(new AlbumList.AlbumListActionListener() {
                @Override
                public void OnAlbumChange(String newAlbumName, DSAlbum a) {
                    albumName.setText(newAlbumName);
                }

                @Override
                public void OnListCompleted(long count, DSAlbum a) {
                    currentAlbumID = (a == null) ? 0 : a.getAlbumID();
                    albumsCount = count;
                    pil.setAlbumID(currentAlbumID);
                    imagesCount = pil.getTotalImagesCount();
                    bottomPanelForAlbums.setText(String.format(Lang.TabAlbumImageList_info_format, albumsCount, imagesCount)); 
                    _clear();
                    _initAlbGUI();
                }   
            });
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TabAlbumImageList() {
        super(0, 9999, 9999);

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
        
        panelTopAlb = new ToolsPanelTop((index) -> {
            switch (index) {
                case BTN_LVL_UP:
                    myAL.levelUp();
                    break;
            }
        });
        
        panelTopImg = new ToolsPanelTop((index) -> {
            switch (index) {
                case BTN_SELNONE:
                    pil.selectNone();
                    break;
                case BTN_TO_ALBUM:
                    if (pil.getSelectedHashes().size() > 0) {
                        dis.refresh();
                        dis.showModal();
                        if (dis.isRetCodeOK()) {
                            final ArrayList<DSAlbum> sl = dis.getSelected();
                            pil.addToAlbums(sl);
                            pil.selectNone();
                            dis.clearSelected();
                        }
                    }
                    break;
                case BTN_ADD_TAGS:
                    
                    break;
                case BTN_TO_TEMP:
                    
                    break;
                case BTN_DEL:
                    
                    break;
            }
        });
        
        panelTopAlb.addButton(IMG24_LEVEL_UP, BTN_LVL_UP);
        
        panelTopImg.addButton(IMG64_SELECT_NONE, BTN_SELNONE);
        panelTopImg.addFixedSeparator();
        panelTopImg.addButton(TabAllImages.IMG48_TO_ALBUM, TabAllImages.BTN_TO_ALBUM);
        panelTopImg.addButton(TabAllImages.IMG48_ADD_TAGS, TabAllImages.BTN_ADD_TAGS);
        panelTopImg.addFixedSeparator();
        panelTopImg.addButton(IMG64_DELETE, BTN_DEL);
        panelTopImg.addSeparator();
        panelTopImg.addButton(TabAllImages.IMG48_TO_TEMP, TabAllImages.BTN_TO_TEMP);
        
        albumName.setAlignment(Pos.CENTER);
    }
    
    private void _initAlbGUI() {
        topToolbar.getChildren().add(panelTopAlb);
        bottomPanel.getChildren().add(bottomPanelForAlbums);
        if (currentAlbumID > 0) {
            header.getChildren().addAll(albumName, GUITools.getSeparator(), album, images);
        } else {
            header.getChildren().addAll(albumName, GUITools.getSeparator(), album);
        }
        this.getChildren().addAll(header, myAL);
    }
    
    public void setPanels(Pane _topToolbar, Pane _bottomPanel) {
        topToolbar = _topToolbar;
        bottomPanel = _bottomPanel;
    }
    
    private void _initImgGUI() {
        if (currentAlbumID <= 0) return;
        
        topToolbar.getChildren().add(panelTopImg);
        bottomPanel.getChildren().add(pil.getPaginator());
        header.getChildren().addAll(albumName, GUITools.getSeparator(), album, images);
        this.getChildren().addAll(header, pil);
    }
    
    public void initDB() {
        myAL.initDB();
    }
    
    public void refresh() {
        _album();
    }
    
    private void _clear() {
        this.getChildren().clear();
        header.getChildren().clear();
        topToolbar.getChildren().clear();
        bottomPanel.getChildren().clear();
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
