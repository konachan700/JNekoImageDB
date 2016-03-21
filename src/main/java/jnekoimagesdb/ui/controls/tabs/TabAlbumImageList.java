package jnekoimagesdb.ui.controls.tabs;

import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.Lang;
import jnekoimagesdb.ui.controls.AlbumList;
import jnekoimagesdb.ui.controls.PagedImageList;
import jnekoimagesdb.ui.controls.PanelButtonCodes;
import jnekoimagesdb.ui.controls.ToolsPanelTop;
import jnekoimagesdb.ui.controls.dialogs.DialogAlbumSelect;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.STabTextButton;

public class TabAlbumImageList extends SEVBox {
    public static final int
            HEADER_VSIZE = 27,
            HBUTTON_HSIZE = 150,
            ALBTITLE_HSIZE = 210;
    
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

        album = new STabTextButton(Lang.AlbumImageList_Albums, ElementsIDCodes.buttonUnknown, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            _album();
        }, "STabTextButton_green");
        
        images = new STabTextButton(Lang.AlbumImageList_Images, ElementsIDCodes.buttonUnknown, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            _clear();
            _initImgGUI();
            _images();
        }, "STabTextButton_red");
        
        panelTopAlb = new ToolsPanelTop((index) -> {
            switch (index) {
                case buttonOneLevelUp:
                    myAL.levelUp();
                    break;
                case buttonAddNewItems:
                    XImg.getUploadBox().setAlbumID(currentAlbumID);
                    XImg.getUploadBox().showModal();
                    break;
            }
        });
        
        panelTopImg = new ToolsPanelTop((index) -> {
            switch (index) {
                case buttonClearSelection: 
                    pil.selectNone();
                    break;
                case buttonAddAlbumsForSelectedItems:
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
                case buttonAddTagsForSelectedItems:
                    
                    break;
                case buttonExportToExchangeFolder:
                    pil.uploadSelected();
                    break;
                case buttonExportToCustomFolder:
                    XImg.openDir().showDialog();
                    //System.out.println(XImg.openDir().getSelected());
                    break;
                case buttonDeleteItem:
                    
                    break;
                case buttonAddSelected:
                    XImg.getUploadBox().setAlbumID(currentAlbumID);
                    XImg.getUploadBox().showModal();
                    break;
            }
        });
        
        panelTopAlb.addButton(GUITools.loadIcon("lvlup-48"), PanelButtonCodes.buttonOneLevelUp, "На один уровень вверх");
        panelTopAlb.addFixedSeparator();
        panelTopAlb.addButton(GUITools.loadIcon("todb-48"), PanelButtonCodes.buttonAddNewItems, "Добавить новые картинки в альбом...");
        
        panelTopImg.addButton(GUITools.loadIcon("selectnone-48"), PanelButtonCodes.buttonClearSelection, "Сбросить выделение"); // TODO тоже
        panelTopImg.addFixedSeparator();
        panelTopImg.addButton(GUITools.loadIcon("add-to-album-48"), PanelButtonCodes.buttonAddAlbumsForSelectedItems, "Добавить выделенные картинки в альбом");
        panelTopImg.addButton(GUITools.loadIcon("add-tags-48"), PanelButtonCodes.buttonAddTagsForSelectedItems, "Добавить теги выбранным картинкам");
        panelTopImg.addFixedSeparator();
        panelTopImg.addButton(GUITools.loadIcon("delete-48"), PanelButtonCodes.buttonDeleteItem, "Удалить выбранные картинки");
        panelTopImg.addFixedSeparator();
        panelTopImg.addButton(GUITools.loadIcon("todb-48"), PanelButtonCodes.buttonAddSelected, "Добавить новые картинки в альбом...");
        panelTopImg.addSeparator();
        panelTopImg.addButton(GUITools.loadIcon("addtotemp-48"), PanelButtonCodes.buttonExportToExchangeFolder, "Скинуть выбранное в папку обмена");
        panelTopImg.addButton(GUITools.loadIcon("export-48"), PanelButtonCodes.buttonExportToCustomFolder, "Экспортировать в...");
        
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
