package dialogs;

import dataaccess.Lang;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jnekoimagesdb.GUITools;
import jnekoimagesdb.GUITools.DragDelta;
import jnekoimagesdb.ResizeHelper;

public class DialogWindow {
    private final    Stage           win         = new Stage();
    private final    DragDelta       DRD         = new DragDelta();
    private final    StackPane       root        = new StackPane();
    
    private final VBox 
            mvbox           = new VBox(),
            toolbarvbox     = new VBox(),
            basevbox        = new VBox();
    
    private final HBox 
            headerbox       = new HBox(), 
            toolbox         = new HBox();
    
    private final Scene scene;
    
    public DialogWindow() {
        scene = generateScene();
        init(false);
    }
    
    public DialogWindow(double w, double h, boolean dialog) {
        scene = generateScene(w, h);
        init(w, h, dialog);
    }
    
    public void hide() {
        win.close();
    }
    
    public void show() {
        win.show();
    }
    
    public Scene getScene() {
        return scene;
    }
    
    public Stage getStage() {
        return win;
    }
    
    public void showModal() {
        win.showAndWait();
    }
    
    public HBox getToolbox() {
        return toolbox;
    }
    
    public VBox getMainContainer() {
        return basevbox;
    }
    
    private void init(boolean dialog) {
        init(1200, 800, dialog);
    }
    
    private void init(double w, double h, boolean dialog) {
        GUITools.setStyle(basevbox, "DialogWindow", "basevbox");
       
        if (!dialog) {
            mvbox.getChildren().add(toolbarvbox);
            mvbox.getChildren().add(basevbox);
            win.initModality(Modality.NONE);
        } else {
            mvbox.getChildren().add(basevbox);
            mvbox.getChildren().add(toolbarvbox);
            win.initModality(Modality.APPLICATION_MODAL);
        }
        
        root.getChildren().add(mvbox);

        GUITools.setStyle(toolbox, "DialogWindow", "toolbox");
        GUITools.setMaxSize(toolbox, 9999, 9999);
        toolbox.setAlignment(Pos.BOTTOM_LEFT);

        GUITools.setStyle(headerbox, "DialogWindow", "headerbox");
        headerbox.setMaxWidth(9999);
        headerbox.getChildren().add(toolbox);

        GUITools.setMaxSize(toolbarvbox, 9999, 70);       
        toolbarvbox.getChildren().add(headerbox);
        
        win.getIcons().add(GUITools.loadIcon("win-icon-128"));
        win.getIcons().add(GUITools.loadIcon("win-icon-64"));
        win.getIcons().add(GUITools.loadIcon("win-icon-32"));
        
        win.setMinWidth(w / 2);
        win.setMinHeight(h / 2);
        win.setTitle(Lang.DialogWindow_Title);
        win.setScene(scene);

        if (System.getProperty("os.name").toLowerCase().contains("win")) ResizeHelper.addResizeListener(win);
    }
    
    private Scene generateScene() {
        return generateScene(1200, 800);
    }
    
    private Scene generateScene(double w, double h) {
        final Scene scenex;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            StackPane root3m = GUITools.getWinGUI(this, win, DRD, root, mvbox, "", GUITools.CLOSE_HIDE_WINDOW, true);
            scenex = new Scene(root3m, w, h);
            scenex.setFill(Color.TRANSPARENT);
        } else {
            scenex = new Scene(root, w, h);
        }
        return scenex;
    }
}
