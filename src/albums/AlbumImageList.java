package albums;

import dataaccess.ImageEngine;
import dataaccess.DBWrapper;
import dataaccess.Lang;
import imagelist.ImageList;
import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

@Deprecated
public class AlbumImageList extends VBox {
    private final HBox 
            tabsHBox = new HBox(6);
    
    private final VBox
            albumList = new VBox(0);
    
    private final ScrollPane
            albumListScroll = new ScrollPane();
    
    private final Label
            imgLabel = new Label(Lang.AlbumImageList_Images),
            albLabel = new Label(Lang.AlbumImageList_Albums);
    
    private long 
            albumID = 0,
            albumsCount = 0;
    
    private final ImageEngine 
            IM;
    
    private final ImageList
            IML;
    
    public AlbumImageList(ImageEngine im, Pane parent) {
        super(2);
        IM = im;
        IML = new ImageList(IM, parent);
        init();
    }
    
    public ImageList getImageList() {
        return IML;
    }
    
    public void setAlbID(long aoid) {
        IML.setAlbimID(aoid);
        IML.normalRefresh();
        imgLabel.setText(Lang.AlbumImageList_ImagesInAlbum + " \"" + DBWrapper.getAlbumName(aoid) +  "\""); 
        albumID = aoid;
        genAlbList();
        albLabel.setText(Lang.AlbumImageList_Albums + " (" + albumsCount + ")"); 
    }
    
    private final ASDNewElementActionListener
        newAL = (long parent, String title) -> {
            DBWrapper.addNewAlbumGroup(title, albumID);
            genAlbList();
        };
    
    private final ASDElementActionListener
        elAL = new ASDElementActionListener() {
            @Override
            public void OnCheck(Long id, AlbumsListElement e) { }

            @Override
            public void OnUncheck(Long id, AlbumsListElement e) { }

            @Override
            public void OnItemClick(Long id, AlbumsListElement e) {
                if (id > 0) {
                    setAlbID(id);
                } else {
                    long parent_el = DBWrapper.getParentAlbum(e.parent);
                    setAlbID(parent_el);
                }
            }

            @Override
            public void OnSave(Long id, AlbumsListElement e, String t) {
                DBWrapper.saveAlbumsCategoryChanges(t, 0, id);
            }
        };
    
    private void genAlbList() {
        if (IML.getAlbumID() == 0) return;
        albumList.getChildren().clear();
        
        ArrayList<AlbumsCategory> alac = DBWrapper.getAlbumsGroupsID(albumID);
        if (alac == null) return;
        albumsCount = 0;
        
        long parentAlbum = DBWrapper.getParentAlbum(albumID);
        if (parentAlbum > 0) {
            final AlbumsListElement el_root = new AlbumsListElement(-1L, albumID, Lang.Files3Dots, elAL);
            el_root.DisableCheck();
            albumList.getChildren().add(el_root);
        }
        
        alac.stream().map((a) -> new AlbumsListElement(a.ID, a.parent, a.name, elAL)).forEach((el) -> {
            el.DisableCheck();
            albumsCount++;
            albumList.getChildren().add(el);
        });

        final ASDNewElement ne = new ASDNewElement(newAL, albumID);
        albumList.getChildren().add(ne);
    }
    
    private void init() {
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("AlbumImageList_rootpane");
        GUITools.setMaxSize(this, 9999, 9999);
        
        albumListScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        albumListScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        albumListScroll.setFitToWidth(true);
        albumListScroll.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        albumListScroll.getStyleClass().add("AlbumImageList_scrollpane");
        albumListScroll.setContent(albumList); 
        GUITools.setMaxSize(albumListScroll, 9999, 9999);
        
        //GUITools.setMaxSize(albumList, 9999, 9999);
        albumList.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        albumList.getStyleClass().add("AlbumImageList_albumList");
        
        imgLabel.setOnMouseClicked((MouseEvent event) -> {
//            this.getChildren().remove(albumList);
            this.getChildren().remove(albumListScroll);
            this.getChildren().remove(IML);
            this.getChildren().add(IML);
            event.consume();
        });
        imgLabel.getStyleClass().add("AlbumImageList_imgLabel");
        imgLabel.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(imgLabel, 9999, 27);
        
        albLabel.setOnMouseClicked((MouseEvent event) -> {
            this.getChildren().remove(IML);
//            this.getChildren().remove(albumList);
//            this.getChildren().add(albumList);
            this.getChildren().remove(albumListScroll);
            this.getChildren().add(albumListScroll);
            genAlbList();
            event.consume();
        });
        
        albLabel.getStyleClass().add("AlbumImageList_albLabel");
        albLabel.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(albLabel, 9999, 27);
        
        tabsHBox.getStyleClass().add("AlbumImageList_tabsHBox");
        tabsHBox.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(tabsHBox, 9999, 27);
        tabsHBox.setMinHeight(27); 
        
        tabsHBox.getChildren().addAll(imgLabel, albLabel);
        this.getChildren().addAll(tabsHBox, IML);
    }
}
