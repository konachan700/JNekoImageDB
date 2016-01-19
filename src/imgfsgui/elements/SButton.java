package imgfsgui.elements;

import static imgfsgui.elements.GUIElements.EVENT_CODE_CLICK;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jnekoimagesdb.GUITools;

public class SButton extends Button {
    private volatile int xID = -1;

    public SButton(Image icon, int id, int size, GUIActionListener al) {
        super("", new ImageView(icon));
        init(id, size, al, "button");
    }

    public SButton(Image icon, int id, int size, GUIActionListener al, String styleName) {
        super("", new ImageView(icon));
        init(id, size, al, styleName);
    }

    private void init(int id, int size, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        GUITools.setFixedSize(this, size, size);
        this.setAlignment(Pos.CENTER);
        this.setOnMouseClicked((c) -> {
            al.OnItemEvent(EVENT_CODE_CLICK, xID); 
        });
    }

    public int getID() {
        return xID;
    }
}