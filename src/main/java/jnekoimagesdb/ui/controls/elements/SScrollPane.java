
package jnekoimagesdb.ui.controls.elements;

import javafx.scene.control.ScrollPane;
import jnekoimagesdb.ui.GUITools;

public class SScrollPane extends ScrollPane {
    @SuppressWarnings("LeakingThisInConstructor")
    public SScrollPane() {
        super();
        GUITools.setStyle(this, "GUIElements", "SScrollPane");
        GUITools.setMaxSize(this, 9999, 9999);
    }
}
