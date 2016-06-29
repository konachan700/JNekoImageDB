package jnekoimagesdb.ui.md.dialogs.imageview;

import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.ui.md.albums.AlbumsSimpleList;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;

public class ImageViewDialogTabAlbums extends VBox {
    private final AlbumsSimpleList 
            asl = new AlbumsSimpleList();
    
    private final TopPanel
            panelTop = new TopPanel();
    
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images");
    
    private final TopPanelMenuButton 
            menuBtnAlbum = new TopPanelMenuButton();
    
    public ImageViewDialogTabAlbums() {
        super();
        this.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane");
        this.setAlignment(Pos.CENTER);
        this.getChildren().add(asl);
        
        menuBtnAlbum.addMenuItem("Добавить картинку в альбомы", (c) -> {
            
        });
        
        infoBox.setTitle("Албомы");
        infoBox.setText("Список альбомов, в которых находится картинка");
        
        panelTop.addNode(infoBox);
        panelTop.addNode(menuBtnAlbum);
        
        asl.setActionListener(item -> {
                
        });
    }
    
    public final void refresh(DSImage img) {
        asl.clear();
        final Set<DSAlbum> albs = img.getAlbums();
        if (albs.isEmpty()) return;
        albs.forEach(album -> {
                asl.addItem(album);
        });
        asl.refresh();
    }
    
    public final Node getTopPanel() {
        return panelTop;
    }
}
