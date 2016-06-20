package jnekoimagesdb.ui.md.dialogs;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FullDialog extends SimpleDialog {
    private final VBox 
            basesp = new VBox(),
            menuBox = new VBox(),
            appBox = new VBox();
            
    private final HBox 
            toolbox = new HBox();
    
    public FullDialog(int xSize, int ySize, boolean modal) {
        super(xSize, ySize, modal);

        final HBox rootPane = new HBox();
        rootPane.getStylesheets().add(CSS_FILE);
        rootPane.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane", "main_window_separator_1");
        
        menuBox.getStyleClass().addAll("main_window_menu_block_width", "main_window_max_height", "main_window_null_pane", "main_window_menu_block");
        appBox.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_appbox_pane", "main_window_separator_1");
        basesp.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_root_container");
        toolbox.getStyleClass().addAll("main_window_max_width", "main_window_toolbox_height", "main_window_toolbox_container");
        
        appBox.getChildren().addAll(toolbox, basesp);
        rootPane.getChildren().addAll(appBox, menuBox);
        this.setContent(rootPane);
    }
    
    public final void setMenu(Node menu) {
        menuBox.getChildren().add(menu);
    }
    
    public final void setPanel(Node panel) {
        toolbox.getChildren().clear();
        toolbox.getChildren().add(panel);
    }
    
    public final void setMainContent(Node content) {
        basesp.getChildren().clear();
        basesp.getChildren().add(content);
    }
}
