package img.gui.elements;

import javafx.scene.image.Image;
import jnekoimagesdb.GUITools;

public class GUIElements {
    public static final int
            EVENT_CODE_CLICK = 1,
            EVENT_CODE_CHANGE = 2;

    public static final Image
            ITEM_SELECTED   = GUITools.loadIcon("selected-32"),  
            ITEM_ERROR      = GUITools.loadIcon("broken-file-128"), 
            ITEM_NOTHING    = GUITools.loadIcon("dummy-128"), 
            ITEM_LOADING    = GUITools.loadIcon("loading-128"), 
            ICON_NOELEMENTS = GUITools.loadIcon("delete-gray-48"), 
            ICON_DIR        = GUITools.loadIcon("dir-normal-128"),
            ICON_DIR_NA     = GUITools.loadIcon("dir-na-128"), 
            ICON_FILE_NA    = GUITools.loadIcon("file-na-128"),
            ICON_CLOCK      = GUITools.loadIcon("clock-48"); 
    
    public static final Image 
            IMG64_SELECT_NO     = GUITools.loadIcon("delete-48"), 
            IMG32_IN_PROGRESS   = GUITools.loadIcon("inprogress-32"),  
            IMG32_IN_UNKNOWN    = GUITools.loadIcon("unknown-32"), 
            IMG32_COMPLETED     = GUITools.loadIcon("selected-32");
}
