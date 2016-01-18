package dialogs;

import datasources.DSAlbum;
import jnekoimagesdb.Lang;
import imgfsgui.AlbumList;
import imgfsgui.ToolsPanelTop;
import static imgfstabs.TabAlbumImageList.BTN_LVL_UP;
import static imgfstabs.TabAlbumImageList.IMG24_LEVEL_UP;
import java.util.ArrayList;
import javafx.scene.image.Image;
import jnekoimagesdb.GUITools;

public class DialogAlbumSelect extends DialogWindow {
    private final DialogAlbumSelect THIS = this;
    
    public static final Image
            IMG48_BTN_OK = GUITools.loadIcon("selected-48");
    
    public static final int
            BTN_OK = 100;
    
    private final ToolsPanelTop 
            panelTop;
    
    private final AlbumList 
            myAL = new AlbumList(new AlbumList.AlbumListActionListener() {
                @Override
                public void OnAlbumChange(String newAlbumName, DSAlbum a) {
                    //albumName.setText(newAlbumName);
                }

                @Override
                public void OnListCompleted(long count, DSAlbum a) {
                    _panelInit((a == null) ? 0 : a.getAlbumID());
                }   
            });
        
    public DialogAlbumSelect() {
        super(800, 600, true); // todo: сделать запоминание размера
        
        panelTop = new ToolsPanelTop((index) -> {
            switch (index) {
                case BTN_LVL_UP:
                    myAL.levelUp();
                    break;
                case BTN_OK:
                    this.setRetCodeOK(true);
                    this.hide();
                    break;
            }
        });
        
        myAL.setDialogMode(true);
        
        THIS.getToolbox().getChildren().add(panelTop);
        THIS.getMainContainer().getChildren().add(myAL);
    }
    
    private void _panelInit(long id) {
        panelTop.clearAll();
        if (id > 0) panelTop.addButton(IMG24_LEVEL_UP, BTN_LVL_UP); 
        panelTop.addSeparator();
        panelTop.addButton(IMG48_BTN_OK, BTN_OK);
    }
    
    public final void refresh() {
        myAL.refresh();
    }
    
    public final void dbInit() {
        myAL.initDB();
    }
    
    public final ArrayList<DSAlbum> getSelected() {
        return myAL.getSelected();
    }
    
    public final void clearSelected() {
        myAL.clearSelected();
    }
}
