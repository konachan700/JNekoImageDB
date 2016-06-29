package jnekoimagesdb.ui.md.albums;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.HibernateUtil;
import org.hibernate.criterion.Restrictions;

public class AlbumsSimpleList extends ScrollPane {
    public final static String
            CSS_FILE = new File("./style/style-gmd-albums.css").toURI().toString();
    
    private final VBox 
            rootContainer = new VBox();
    
    private final Set<DSAlbum> 
            selectedAlbums = new HashSet<>();

    private final AlbumsElementActionListener elementAL = new AlbumsElementActionListener() {
        @Override
        public void OnClick(AlbumsElement element, DSAlbum album, MouseEvent value) { }

        @Override
        public void OnDoubleClick(AlbumsElement element, DSAlbum album, MouseEvent value) { }

        @Override
        public void OnEdit(DSAlbum album, String title, String text) { }

        @Override
        public void OnDelete(DSAlbum album) { }

        @Override
        public void OnToImagesButtonClick(DSAlbum album, MouseEvent value) {
            if (tabAL != null) tabAL.OnNavigateToImages(album); 
        }
    };
    
    private AlbumsActionListener 
            tabAL = null;
    
    public AlbumsSimpleList() {
        super();
        
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setFitToHeight(false);
        this.setFitToWidth(true);
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_element_textarera");
        
        rootContainer.setAlignment(Pos.TOP_CENTER);
        rootContainer.getStyleClass().addAll("albums_max_width", "albums_x_root_pane", "albums_h_space");
        
        this.setContent(rootContainer);
    }
    
    public void setActionListener(AlbumsActionListener al) {
        tabAL = al;
    }
    
    public final Set<DSAlbum> getSelectedList() {
        return selectedAlbums;
    }
    
    public final void clear() {
        selectedAlbums.clear();
        refresh();
    }
    
    public final void addItem(DSAlbum da) {
        selectedAlbums.add(da);
        refresh();
    }
    
    public final void removeItem(DSAlbum da) {
        selectedAlbums.remove(da);
        refresh();
    }
    
    public final void refresh() {
        rootContainer.getChildren().clear();
        selectedAlbums.forEach(item -> {
                rootContainer.getChildren().add(new AlbumsElement(item, elementAL).setDeleteMode());
        }); 
    }

}
