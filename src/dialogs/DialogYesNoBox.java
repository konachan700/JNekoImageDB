package dialogs;

import dataaccess.Lang;
import imgfsgui.ToolsPanelTop;
import java.io.File;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

public class DialogYesNoBox extends DialogWindow {
    private final Image 
            IMG64_SELECT_YES = new Image(new File("./icons/d_yes.png").toURI().toString()),
            IMG64_SELECT_NO  = new Image(new File("./icons/d_no.png").toURI().toString());
    
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
        super(750, 330);
        
        messageText.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        messageText.getStyleClass().add("DialogYesNoBox_textarea");
        messageText.setMaxSize(9999, 9999);
        messageText.setPrefSize(9999, 9999);
        messageText.setWrapText(true);
        this.getMainContainer().getChildren().add(messageText);
        
        this.getToolbox().getChildren().add(panel);
        panel.addButton(IMG64_SELECT_YES, SELECT_YES);
        panel.addButton(IMG64_SELECT_NO,  SELECT_NO);
    }
    
    public int showYesNo(String text) {
        messageText.setText(text);
        this.showModal();
        return defRetCode;
    }
}
