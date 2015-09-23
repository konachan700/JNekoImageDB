package menulist;

import javafx.scene.control.TextArea;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;

public class MenuList extends VBox {
    private final VBox
            scrollable = new VBox(),
            helptext = new VBox();
    
//    private final TextArea
//            helpTextArea = new TextArea();
    
    @SuppressWarnings("FieldMayBeFinal")
    private String 
            bgColor = "#000",
            scrollableBgColor = "#000";
    
    final MenuListScrollable 
            ms = new MenuListScrollable();
    
    public MenuList() {
        ms.setPrefHeight(Integer.MAX_VALUE);
        ms.setPrefWidth(Integer.MAX_VALUE);
        
        scrollable.getChildren().add(ms);
        scrollable.setStyle("-fx-background-color:" + scrollableBgColor + ";");
        this.getChildren().add(scrollable);
        
//        helpTextArea.setEditable(false);
//        helpTextArea.setBorder(Border.EMPTY);
//        helpTextArea.setText("Select element for help");
//        helpTextArea.setWrapText(true);
        this.getStylesheets().add(getClass().getResource("HelpTextArea.css").toExternalForm());
        this.getStyleClass().add("text-area");
//        helptext.getChildren().add(helpTextArea);
        
//        helptext.setPrefSize(Integer.MAX_VALUE, 70);
//        helptext.setMaxSize(Integer.MAX_VALUE, 70);
//        helptext.setMinSize(0, 70);
//        helptext.setStyle("-fx-background-color:" + bgColor + ";");
//        
//        this.getChildren().add(helptext);
    }
    
    public final MenuListScrollable getMenu() {
        return ms;
    }
}
