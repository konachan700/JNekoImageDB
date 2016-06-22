package jnekoimagesdb.ui.md.dialogs;

import java.io.File;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.Lang;

public class SimpleDialog extends Stage {
    public final static String
            CSS_FILE = new File("./style/style-gmd-main-window.css").toURI().toString();
    
    private final VBox 
            windowContainer = new VBox();
    
    public SimpleDialog(int xSize, int ySize, boolean modal) {
        super();
        final Scene scene = new Scene(windowContainer, xSize, ySize);
        
        windowContainer.getStylesheets().add(CSS_FILE);
        windowContainer.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane");
        
        if (modal) this.initModality(Modality.APPLICATION_MODAL);
        this.getIcons().add(GUITools.loadIcon("win-icon-128"));
        this.getIcons().add(GUITools.loadIcon("win-icon-64"));
        this.getIcons().add(GUITools.loadIcon("win-icon-32"));
        this.setMinWidth(xSize);
        this.setMinHeight(ySize);
        this.setTitle(Lang.JNekoImageDB_title);
        this.setScene(scene);
    }
    
    public void setContent(Node content) {
        windowContainer.getChildren().clear();
        windowContainer.getChildren().add(content);
    }
    
    public void setContent(Node ... content) {
        windowContainer.getChildren().clear();
        windowContainer.getChildren().addAll(content);
    }

    public void clearContent() {
        windowContainer.getChildren().clear();
    }
}
