package img.gui.dialogs;

import img.gui.ToolsPanelTop;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import jnekoimagesdb.GUITools;

public class DialogYesNoBox extends DialogWindow {
    private final Image 
            IMG64_SELECT_YES = GUITools.loadIcon("selected-48"), 
            IMG64_SELECT_NO  = GUITools.loadIcon("delete-48");
    
    public final static int
            SELECT_YES = 1,
            SELECT_NO = 2;
    
    private volatile int
            defRetCode = SELECT_NO;
            
    private final TextArea
            messageText = new TextArea();
    
    private final ToolsPanelTop
            panel = new ToolsPanelTop((index) -> {
                switch (index) {
                    case SELECT_YES:
                        defRetCode = SELECT_YES;
                        this.hide();
                        break;
                    default:
                        defRetCode = SELECT_NO;
                        this.hide();
                        break;
                }
            });
    
    public DialogYesNoBox() {
        super(550, 250, true);
        
        GUITools.setStyle(messageText, "DialogYesNoBox", "textarea");
        messageText.setMaxSize(9999, 9999);
        messageText.setPrefSize(9999, 9999);
        messageText.setWrapText(true);
        messageText.setEditable(false);
        this.getMainContainer().getChildren().add(messageText);
        
        this.getToolbox().getChildren().add(panel);
        panel.addSeparator();
        panel.addButton(IMG64_SELECT_NO,  SELECT_NO);
        panel.addButton(IMG64_SELECT_YES, SELECT_YES);
    }
    
    public int showYesNo(String text) {
        messageText.setText(text);
        this.showModal();
        return defRetCode;
    }
}
