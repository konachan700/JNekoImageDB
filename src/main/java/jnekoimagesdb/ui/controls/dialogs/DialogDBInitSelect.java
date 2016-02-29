package jnekoimagesdb.ui.controls.dialogs;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.Lang;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.STabTextButton;
import static jnekoimagesdb.ui.controls.tabs.TabAlbumImageList.ALBTITLE_HSIZE;
import static jnekoimagesdb.ui.controls.tabs.TabAlbumImageList.HBUTTON_HSIZE;
import static jnekoimagesdb.ui.controls.tabs.TabAlbumImageList.HEADER_VSIZE;

public class DialogDBInitSelect extends DialogWindow {
    
    private final HBox 
            header = new HBox(4);
     
    private final STabTextButton 
            openBtn, create;
    
    private final SFLabel
            albumName = new SFLabel("Создать/открыть БД", ALBTITLE_HSIZE, ALBTITLE_HSIZE, HEADER_VSIZE, HEADER_VSIZE, "albumName", "TabAlbumImageList");
    
    public DialogDBInitSelect() {
        super(1100, 768, false); // todo: сделать запоминание размера
        
        GUITools.setStyle(header, "TabAlbumImageList", "header");
        header.setMaxSize(9999, HEADER_VSIZE);
        header.setPrefSize(9999, HEADER_VSIZE);
        header.setMinSize(HBUTTON_HSIZE * 3, HEADER_VSIZE);
        header.setAlignment(Pos.CENTER);
        
        openBtn = new STabTextButton("Открыть", 1, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            
        }, "STabTextButton_green");
        
        create = new STabTextButton("Создать", 2, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            
        }, "STabTextButton_red");
        
        albumName.setAlignment(Pos.CENTER);
        header.getChildren().addAll(albumName, GUITools.getSeparator(), openBtn, create);
        
        this.getMainContainer().getChildren().addAll(header);
    }
    
    
}
