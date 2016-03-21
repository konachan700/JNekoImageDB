package jnekoimagesdb.ui.controls.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jnekoimagesdb.ui.GUITools;

public class SButton extends Button {
    private ElementsIDCodes xID = ElementsIDCodes.buttonUnknown;

    public SButton(Image icon, ElementsIDCodes id, int size, GUIActionListener al) {
        super("", new ImageView(icon));
        init(id, size, al, "button");
    }

    public SButton(Image icon, ElementsIDCodes id, int size, GUIActionListener al, String styleName) {
        super("", new ImageView(icon));
        init(id, size, al, styleName);
    }

    private void init(ElementsIDCodes id, int size, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        GUITools.setFixedSize(this, size, size);
        this.setAlignment(Pos.CENTER);
        this.setOnMouseClicked((c) -> {
            al.OnItemEvent(ElementsEventCodes.eventClick, xID); 
        });
    }
    
    public void setIcon(Image icon) {
        this.setGraphic(new ImageView(icon));
    }

    public ElementsIDCodes getID() {
        return xID;
    }
}