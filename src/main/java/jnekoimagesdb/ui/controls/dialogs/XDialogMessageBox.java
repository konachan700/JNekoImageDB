package jnekoimagesdb.ui.controls.dialogs;

import javafx.geometry.Pos;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SElementPair;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.STabTextButton;

public class XDialogMessageBox extends XDialogWindow {
    private final SFLabel
            outText = new SFLabel("", 64, 9999, 64, 64, "label_darkgreen_small", "TypesListItem");
    
    public XDialogMessageBox() {
        super();

        final SEVBox mtOptions = new SEVBox("svbox_sett_container_green");
        mtOptions.setAlignment(Pos.CENTER_RIGHT);
        
        mtOptions.getChildren().addAll(
                outText,
                GUITools.getHSeparator(32),
                new SElementPair(
                        GUITools.getSeparator(), 
                        4, 32, 32,
                        GUITools.getSeparator(),
                        new STabTextButton("OK", ElementsIDCodes.buttonUnknown, 120, 32, (x, y) -> {
                            this.hide();
                        })
                ).setAlign(Pos.CENTER_RIGHT)
        );
        
        super.create(null, null, mtOptions, COLOR_BLACK, 550, 200);
    }

    public void show(String text) {
        outText.setText(text);
        super.showModal();
    }
}
