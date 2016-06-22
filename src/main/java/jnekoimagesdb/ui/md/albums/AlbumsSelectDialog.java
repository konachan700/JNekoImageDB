package jnekoimagesdb.ui.md.albums;

import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.ui.md.controls.HSeparator;
import jnekoimagesdb.ui.md.dialogs.SimpleDialog;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;

public class AlbumsSelectDialog extends SimpleDialog {
    private static AlbumsSelectDialog
            asd;

    private final Albums 
            albs;
    
    private final HBox 
            toolbox = new HBox();
    
    private final AlbumsSimpleList
            albsAdd;
    
    private final TopPanel
            panelTop = new TopPanel(); 
    
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images"); 

    private final TopPanelMenuButton 
            menuBtnAlbum = new TopPanelMenuButton();

    private AlbumsSelectDialog() {
        super(900, 700, true);
        
        toolbox.getStyleClass().addAll("main_window_max_width", "main_window_toolbox_height", "main_window_toolbox_container");
        toolbox.getChildren().add(panelTop);
        
        albsAdd = new AlbumsSimpleList();
        albsAdd.setActionListener(album -> {
            albsAdd.removeItem(album); 
        });
        
        albs = Albums.getSelected();
        albs.setActionListener(album -> {
            albsAdd.addItem(album);
        });
        
        infoBox.setTitle("Добавить картинки в альбомы");
        
        panelTop.addNode(infoBox);
        panelTop.addNode(albs.getLevelUpButton());
        panelTop.addNode(menuBtnAlbum);
        
        menuBtnAlbum.addMenuItem("Сохранить изменения", (c) -> {
            super.hide();
        });
        
        menuBtnAlbum.addMenuItem("Закрыть окно", (c) -> {
            albsAdd.getSelectedList().clear();
            albsAdd.refresh();
            super.hide();
        });
        
        this.setOnCloseRequest(c -> {
            albsAdd.getSelectedList().clear();
            albsAdd.refresh();
        });
        
        final Label selectedText = new Label();
        selectedText.setAlignment(Pos.TOP_LEFT);
        selectedText.setWrapText(true);
        selectedText.getStyleClass().addAll("main_window_max_width", "main_window_messagebox_text");
        selectedText.setText("Выбранные альбомы:");
        
        this.setContent(
                toolbox,
                albs,
                new HSeparator(),
                selectedText,
                albsAdd
        );
        
        albs.refresh();
    } 
    
    public final Set<DSAlbum> getSelectedAlbums() {
        return albsAdd.getSelectedList();
    }
    
    public static AlbumsSelectDialog getDialog() {
        if (asd == null) asd = new AlbumsSelectDialog();
        return asd;
    }
}
