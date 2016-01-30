package jnekoimagesdb.ui.controls.menulist;

import javafx.scene.control.Label;

public class MenuLabel extends Label {
    private String 
            ID = "",
            GID = "",
            HelpText = "";
    
    public final void setID(String id) {
        ID = id;
    }
    
    public final String getID() {
        return ID;
    }
    
    public final void setGID(String id) {
        GID = id;
    }
    
    public final String getGID() {
        return GID;
    }
    
    public final void setHelpText(String t) {
        HelpText = t;
    }
    
    public final String getGelpText() {
        return HelpText;
    }
}
