
package imgfsgui.elements;

import javafx.scene.control.ScrollPane;
import jnekoimagesdb.GUITools;

public class SScrollPane extends ScrollPane {
    @SuppressWarnings("LeakingThisInConstructor")
    public SScrollPane() {
        super();
        GUITools.setStyle(this, "GUIElements", "SScrollPane");
        GUITools.setMaxSize(this, 9999, 9999);
    }
}
