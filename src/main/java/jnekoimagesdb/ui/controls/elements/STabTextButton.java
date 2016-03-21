package jnekoimagesdb.ui.controls.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import jnekoimagesdb.ui.GUITools;

public class STabTextButton extends Button {
    private ElementsIDCodes xID = ElementsIDCodes.buttonUnknown;

    public STabTextButton(String text, ElementsIDCodes id, int sizeX, int sizeY, GUIActionListener al) {
        super(text);
        init(id, sizeX, sizeY, al, "STabTextButton");
    }

    public STabTextButton(String text, ElementsIDCodes id, int sizeX, int sizeY, GUIActionListener al, String styleName) {
        super(text);
        init(id, sizeX, sizeY, al, styleName);
    }

    private void init(ElementsIDCodes id, int sizeX, int sizeY, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        GUITools.setFixedSize(this, sizeX, sizeY);
        this.setAlignment(Pos.CENTER);
        this.setOnMouseClicked((c) -> {
            al.OnItemEvent(ElementsEventCodes.eventClick, xID); 
        });
    }

    public ElementsIDCodes getID() {
        return xID;
    }
}