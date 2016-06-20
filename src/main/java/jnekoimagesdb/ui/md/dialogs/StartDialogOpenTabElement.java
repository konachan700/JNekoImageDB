package jnekoimagesdb.ui.md.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import jiconfont.javafx.IconNode;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;

public class StartDialogOpenTabElement extends HBox {
    private final StartDialogOpenTabActionListener elementAL;

    private final Label 
            title = new Label();
    
    private boolean 
            selected = false;
    
    private String 
            dbName = "";
    
    @SuppressWarnings("LeakingThisInConstructor")
    public StartDialogOpenTabElement(String name, StartDialogOpenTabActionListener al) {
        super();
        elementAL = al;
        dbName = name;
        
        this.setAlignment(Pos.CENTER);
        
        final TopPanelButton navigateToImg = new TopPanelButton("dialog_db_select_icon", "Открыть БД", c -> {
            if (c.getClickCount() == 1) elementAL.OnSelect(dbName);
        });
        
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().addAll("dialog_db_element_icon");

        this.getChildren().addAll(iconNode, title, navigateToImg);

        title.setText(name);
        title.setAlignment(Pos.CENTER_LEFT);
        title.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane", "dialog_db_font");

        setSelected(false);
    }
    
    public final void setSelected(boolean sel) {
        selected = sel;
        this.getStyleClass().clear();
        if (selected) 
            this.getStyleClass().addAll("main_window_max_width", "dialog_db_element_height", "dialog_db_element_root_pane", "dialog_db_element_root_pane_selected");
        else
            this.getStyleClass().addAll("main_window_max_width", "dialog_db_element_height", "dialog_db_element_root_pane", "dialog_db_element_root_pane_non_selected");
    }
}
