package jnekoimagesdb.ui.md.albums;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.ui.md.images.PagedImageList;
import jnekoimagesdb.ui.md.dialogs.MessageBox;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;
import org.hibernate.criterion.Restrictions;

public class Albums extends ScrollPane {
    public final static String
            CSS_FILE = new File("./style/style-gmd-albums.css").toURI().toString();

    private final VBox 
            rootContainer = new VBox();
    
//    private final PagedImageList
//            pil = XImg.getPagedImageList();
    
    private AlbumsActionListener 
            tabAL = null;
    
    private final TopPanelMenuButton 
            menuBtnAlbum = new TopPanelMenuButton();

    private final Label
            bottomPanelForAlbums = new Label("Статистика альбома"),
            noAlbumsMessage = new Label("В этом альбоме нет ни одного подраздела.");
    
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images");    
    
    private boolean
            topAlbum = true;
    
    private DSAlbum
            currentAlbum = null, 
            selectedAlbum = null;
    
    private final TopPanel
            panelTop;
    
    private final ArrayList<AlbumsElement> 
            currentElementList = new ArrayList<>();
    
    private final MenuItem
            addNewImagesMenu;
    
    private final TopPanelButton
            tpbLevelUp;
    
    private final AlbumsElementActionListener elementAL = new AlbumsElementActionListener() {
        @Override
        public void OnClick(AlbumsElement element, DSAlbum album, MouseEvent value) {
            currentElementList.forEach(ae -> {
                ae.setSelected(false);
            });
            element.setSelected(true);
            selectedAlbum = album;
        }

        @Override
        public void OnDoubleClick(AlbumsElement element, DSAlbum album, MouseEvent value) {
            currentAlbum = album;
            topAlbum = false;
            PagedImageList.get().setImageType(DSImageIDListCache.ImgType.InAlbum, album.getAlbumID());
            refresh();
        }

        @Override
        public void OnEdit(DSAlbum album, String title, String text) {
            if (title.trim().length() < 2) return;
                    
            HibernateUtil.beginTransaction(HibernateUtil.getCurrentSession());
            album.setAlbumName(title);
            album.setAlbumText(text);
            HibernateUtil.getCurrentSession().save(album);
            HibernateUtil.commitTransaction(HibernateUtil.getCurrentSession());
                    
            refresh();
        }

        @Override
        public void OnDelete(DSAlbum album) {
            
        }

        @Override
        public void OnToImagesButtonClick(DSAlbum album, MouseEvent value) {
            if (tabAL != null) tabAL.OnNavigateToImages(album);
        }
    };
    
    public void setActionListener(AlbumsActionListener al) {
        tabAL = al;
    }
    
    public Albums() {
        super();
        
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setFitToHeight(false);
        this.setFitToWidth(true);
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_element_textarera");
        
        rootContainer.setAlignment(Pos.TOP_CENTER);
        rootContainer.getStyleClass().addAll("albums_max_width", "albums_x_root_pane", "albums_h_space");
        
        bottomPanelForAlbums.getStyleClass().addAll("albums_max_width", "albums_max_height", "tags_list_bottom_panel");
        
        noAlbumsMessage.setAlignment(Pos.CENTER);
        noAlbumsMessage.getStyleClass().addAll("albums_max_width", "albums_no_items_height", "albums_null_pane", "albums_list_no_items");
        
        this.setContent(rootContainer);
        
        menuBtnAlbum.addMenuItem("Вставить", (c) -> {
            paste();
        });
        menuBtnAlbum.addMenuItem("Удалить выделенное", (c) -> {
            
        });
        addNewImagesMenu = menuBtnAlbum.addMenuItem("Добавить новые картинки в альбом...", (c) -> {
            if (currentAlbum == null) return;
            XImg.getUploadBox().setAlbumID(currentAlbum.getAlbumID());
            XImg.getUploadBox().showModal();
        });
        
        tpbLevelUp = new TopPanelButton("panel_icon_tags_one_level_up", "На один уровень вверх", c -> {
                    levelUp();
                });
        
        panelTop = new TopPanel(); 
        panelTop.addNode(infoBox);
        panelTop.addNode(tpbLevelUp);
        panelTop.addNode(menuBtnAlbum);
        
        //tpbLevelUp.setVisible(false);
    }
    
    public Parent getPanel() {
        return panelTop;
    }
    
    public final Node getPaginator() {
        return bottomPanelForAlbums;
    }
    
    public final void refresh() {
        rootContainer.getChildren().clear();
        currentElementList.clear();
        
        addNewImagesMenu.setVisible(!topAlbum); 
        tpbLevelUp.setVisible(!topAlbum); 
        
        if (currentAlbum != null) {
            infoBox.setTitle(currentAlbum.getAlbumName()); 
            //infoBox.setText(currentAlbum.getAlbumText());
            infoBox.setText("");
        } else {
            infoBox.setTitle("Корневой альбом");
            infoBox.setText("");
        }
        
        List<DSAlbum> list = HibernateUtil.getCurrentSession()
                .createCriteria(DSAlbum.class)
                .add(Restrictions.eq("parentAlbumID", topAlbum ? 0 : currentAlbum.getAlbumID()))
                .list();
        
        if (list.size() > 0) {
            for (DSAlbum ds : list) {
                AlbumsElement ae = new AlbumsElement(ds, elementAL);
                rootContainer.getChildren().add(ae);
                currentElementList.add(ae);
            }
        } else {
            rootContainer.getChildren().add(noAlbumsMessage);
        }
        
        bottomPanelForAlbums.setText("Альбомов: "+list.size());
    }
    
    private void levelUp() {
        if (currentAlbum == null) return;
        if (currentAlbum.getParentAlbumID() > 0) {
            List<DSAlbum> list = HibernateUtil.getCurrentSession()
                    .createCriteria(DSAlbum.class)
                    .add(Restrictions.eq("albumID", currentAlbum.getParentAlbumID()))
                    .list();

            if (list.size() > 0) {
                DSAlbum ds = list.get(0);
                currentAlbum = ds;
//                infoBox.setTitle(ds.getAlbumName()); 
//                infoBox.setText(ds.getAlbumText());
            }
            topAlbum = false;
        } else {
            topAlbum = true;
            currentAlbum = null;
//            infoBox.setTitle("Корневой альбом");
//            infoBox.setText("");
        }
        refresh();
    }
    
    private void paste() {
        if (selectedAlbum == null) return;
        final long 
                cuttedAlbumXID = selectedAlbum.getAlbumID(),
                cuttedAlbumXIDParent = selectedAlbum.getParentAlbumID(),
                currentAlbumXID = (currentAlbum == null) ? 0 : currentAlbum.getAlbumID();

        long tempAlbumID = currentAlbumXID;
        if (currentAlbumXID == cuttedAlbumXIDParent) {
            MessageBox.show("Действие не выполнено: папка назначения и исходная папка одинаковы.");
            return;
        }

        while (true) {
            if (tempAlbumID == cuttedAlbumXID) {
                MessageBox.show("Невозможно перенести альбом сам в себя!");
                return;
            }

            if (tempAlbumID == 0) {
                final DSAlbum dsa = selectedAlbum;

                HibernateUtil.beginTransaction(HibernateUtil.getCurrentSession());
                dsa.setParentAlbumID((currentAlbum == null) ? 0 : currentAlbum.getAlbumID());
                HibernateUtil.getCurrentSession().save(dsa);
                HibernateUtil.commitTransaction(HibernateUtil.getCurrentSession());

                refresh();
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
                MessageBox.show("Общая ошибка запроса данных.");
                break;
            }
        }
    }
}
