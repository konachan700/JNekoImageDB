package jnekoimagesdb.ui.md.dialogs;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PanelDialog extends SimpleDialog {
    private final VBox 
            basesp = new VBox(),
            appBox = new VBox();
            
    private final HBox 
            toolbox = new HBox(),
            paginator_1 = new HBox();;
    
    public PanelDialog(int xSize, int ySize, boolean modal) {
        super(xSize, ySize, modal);

        final HBox rootPane = new HBox();
        rootPane.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane", "main_window_separator_1");
        
        appBox.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_appbox_pane", "main_window_separator_1");
        basesp.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_root_container");
        toolbox.getStyleClass().addAll("main_window_max_width", "main_window_toolbox_height", "main_window_toolbox_container");
        paginator_1.getStyleClass().addAll("main_window_max_width", "main_window_paginator_height", "main_window_paginator_container");
        
        appBox.getChildren().addAll(toolbox, basesp, paginator_1);
        rootPane.getChildren().addAll(appBox);
        this.setContent(rootPane);
    }
    
    public final void setPanel(Node panel) {
        toolbox.getChildren().clear();
        toolbox.getChildren().add(panel);
    }
    
    public final void setMainContent(Node content) {
        basesp.getChildren().clear();
        basesp.getChildren().add(content);
    }
    
    public final void setPaginator(Node content) {
        paginator_1.getChildren().clear();
        paginator_1.getChildren().add(content);
    }
}