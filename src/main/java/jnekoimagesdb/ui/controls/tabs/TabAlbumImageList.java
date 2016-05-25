package jnekoimagesdb.ui.controls.tabs;

import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.HibernateUtil;
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
import org.hibernate.criterion.Restrictions;

public class TabAlbumImageList extends SEVBox {
    public static final int
            HEADER_VSIZE = 27,
            HBUTTON_HSIZE = 150,
            ALBTITLE_HSIZE = 210;
    
    private final ToolsPanelTop.SPanelPopupMenuButton 
            menuBtnAlbum = new ToolsPanelTop.SPanelPopupMenuButton(),
            menuBtnImage = new ToolsPanelTop.SPanelPopupMenuButton()
            ;
    
    private long 
            currentAlbumID = -1, 
            imagesCount = 0, 
            albumsCount = 0;
    
    private boolean isAlbum = true;

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
                    System.err.println("DMD: count="+count);
                    
                    currentAlbumID = (a == null) ? 0 : a.getAlbumID();
                    albumsCount = count;
                    pil.setAlbumID(currentAlbumID);
                    imagesCount = pil.getTotalImagesCount();
                    bottomPanelForAlbums.setText(String.format(Lang.TabAlbumImageList_info_format, albumsCount, imagesCount));
                    if ((count == 0) && (isAlbum)) {
                        _clear();
                        _initImgGUI();
                        _images();
                        isAlbum = false;
                    } else {
                        _clear();
                        _initAlbGUI();
                    }
                }   
            });
    
    private void paste() {
        if (myAL.getCutted() == null) return;
        final long 
                cuttedAlbumXID = myAL.getCutted().getAlbumID(),
                cuttedAlbumXIDParent = myAL.getCutted().getParentAlbumID(),
                currentAlbumXID = myAL.getAlbumID();

        long tempAlbumID = currentAlbumXID;
        if (currentAlbumXID == cuttedAlbumXIDParent) {
            XImg.msgbox("Действие не выполнено: папка назначения и исходная папка одинаковы.");
            return;
        }

        while (true) {
            if (tempAlbumID == cuttedAlbumXID) {
                XImg.msgbox("Невозможно перенести альбом сам в себя!");
                return;
            }

            if (tempAlbumID == 0) {
                final DSAlbum dsa = myAL.getCutted();

                HibernateUtil.beginTransaction(HibernateUtil.getCurrentSession());
                dsa.setParentAlbumID(myAL.getAlbumID());
                HibernateUtil.getCurrentSession().save(dsa);
                HibernateUtil.commitTransaction(HibernateUtil.getCurrentSession());

                myAL.refresh();
                break;
            }

            DSAlbum dsa = (DSAlbum) HibernateUtil
                    .getCurrentSession()
                    .createCriteria(DSAlbum.class)
                    .add(Restrictions.eq("albumID", tempAlbumID))
                    .uniqueResult();
            if (dsa != null) {
                tempAlbumID = dsa.getParentAlbumID();
            } else {
                XImg.msgbox("Общая ошибка запроса данных.");
                break;
            }
        }
    }
    
    
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
                    isAlbum = true;
                    break;
            }
        });
        
        panelTopImg = new ToolsPanelTop((index) -> {
            switch (index) {
                case buttonOneLevelUp:
                    _album();
                    myAL.levelUp();
                    isAlbum = true;
                    break;
                case buttonExportToExchangeFolder:
                    pil.uploadSelected();
                    break;
            }
        });
        
        panelTopAlb.addButton(GUITools.loadIcon("lvlup-48"), PanelButtonCodes.buttonOneLevelUp, "На один уровень вверх");
        panelTopAlb.addSeparator();
        panelTopAlb.addMenuButton(menuBtnAlbum);
        menuBtnAlbum.addMenuItem("Вставить", (c) -> {
            paste();
        });
        menuBtnAlbum.addMenuItem("Удалить выделенное", (c) -> {
            
        });
        menuBtnAlbum.addSeparator();
        menuBtnAlbum.addMenuItem("Добавить новые картинки в альбом...", (c) -> {
            XImg.getUploadBox().setAlbumID(currentAlbumID);
            XImg.getUploadBox().showModal();
        });
        
        panelTopImg.addButton(GUITools.loadIcon("lvlup-48"), PanelButtonCodes.buttonOneLevelUp, "На один уровень вверх");
        panelTopImg.addButton(GUITools.loadIcon("addtotemp-48"), PanelButtonCodes.buttonExportToExchangeFolder, "Скинуть выбранное в папку обмена");
        panelTopImg.addSeparator();
        panelTopImg.addMenuButton(menuBtnImage);
        menuBtnImage.addMenuItem("Сбросить выделение", (c) -> {
            pil.selectNone();
        });
        menuBtnImage.addSeparator();
        menuBtnImage.addMenuItem("Добавить выделенное в альбом...", (c) -> {
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
        });
        menuBtnImage.addMenuItem("Добавить теги выбранному...", (c) -> {
            
        });
        menuBtnImage.addMenuItem("Добавить новые картинки в альбом...", (c) -> {
            XImg.getUploadBox().setAlbumID(currentAlbumID);
            XImg.getUploadBox().showModal();
        });
        menuBtnImage.addSeparator();
        menuBtnImage.addMenuItem("Удалить выбранные картинки", (c) -> {
            
        });
        menuBtnImage.addSeparator();
        menuBtnImage.addMenuItem("Сохранить выделенное на диск...", (c) -> {
            XImg.openDir().showDialog();
        });

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
