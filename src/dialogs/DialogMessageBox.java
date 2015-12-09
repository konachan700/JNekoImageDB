package dialogs;

import imgfsgui.ToolsPanelTop;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import jnekoimagesdb.GUITools;

public class DialogMessageBox extends DialogWindow {
    private final Image 
            IMG64_SELECT_YES = GUITools.loadIcon("selected-48");
            
    private final TextArea
            messageText = new TextArea();
    
    private final ToolsPanelTop
            panel = new ToolsPanelTop((index) -> {
                this.hide();
            });
    
    public DialogMessageBox() {
        super(550, 250, true);
        
        GUITools.setStyle(messageText, "DialogYesNoBox", "textarea");
        messageText.setMaxSize(9999, 9999);
        messageText.setPrefSize(9999, 9999);
        messageText.setWrapText(true);
        messageText.setEditable(false);
        this.getMainContainer().getChildren().add(messageText);
        
        this.getToolbox().getChildren().add(panel);
        panel.addSeparator();
        panel.addButton(IMG64_SELECT_YES, 0);
    }
    
    public void showMsgbox(String text) {
        messageText.setText(text);
        this.showModal();
    }
}
