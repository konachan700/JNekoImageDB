package jnekoimagesdb.ui.controls.dialogs;

import javafx.geometry.Pos;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SElementPair;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.STabTextButton;

public class XDialogYesNoBox extends XDialogWindow {
    public static enum XDialogYesNoBoxResult {
        dYes, dNo, dUnknown
    }
    
    private XDialogYesNoBoxResult
            dResult = XDialogYesNoBoxResult.dUnknown;
    
    private final SFLabel
            outText = new SFLabel("", 64, 9999, 44, 44, "label_darkgreen_small", "TypesListItem"),
            outTitle = new SFLabel("", 64, 9999, 20, 20, "label_darkgreen", "TypesListItem");
    
    public XDialogYesNoBox() {
        super();

        final SEVBox mtOptions = new SEVBox("svbox_sett_container_green");
        mtOptions.setAlignment(Pos.CENTER_RIGHT);
        
        mtOptions.getChildren().addAll(
                outTitle,
                outText,
                GUITools.getHSeparator(32),
                new SElementPair(
                        new STabTextButton("  Нет  ", ElementsIDCodes.buttonUnknown, 120, 32, (x, y) -> {
                            dResult = XDialogYesNoBoxResult.dNo;
                            this.hide();
                        }), 
                        4, 32, 32,
                        GUITools.getSeparator(),
                        new STabTextButton("  Да  ", ElementsIDCodes.buttonUnknown, 120, 32, (x, y) -> {
                            dResult = XDialogYesNoBoxResult.dYes;
                            this.hide();
                        })
                ).setAlign(Pos.CENTER_RIGHT)
        );
        
        super.create(null, null, mtOptions, COLOR_BLACK, 550, 200);
    }
    
    public void showYesNo(String text) {
        dResult = XDialogYesNoBoxResult.dUnknown;
        outTitle.setText("");
        outText.setText(text);
        this.showModal();
    }
    
    public void showYesNo(String title, String text) {
        dResult = XDialogYesNoBoxResult.dUnknown;
        outTitle.setText(title);
        outText.setText(text);
        this.showModal();
    }
    
    public XDialogYesNoBoxResult getResult() {
        return dResult;
    }
}
