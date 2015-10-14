package dialogs;

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jnekoimagesdb.DragDelta;
import jnekoimagesdb.GUITools;
import jnekoimagesdb.JNekoImageDB;
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
            toolbox         = new HBox(),
            logobox         = new HBox();
    
    private final Scene scene;
    
    public DialogWindow() {
        scene = generateScene();
        init();
    }
    
    public DialogWindow(double w, double h) {
        scene = generateScene(w, h);
        init(w, h);
    }
    
    public void hide() {
        win.close();
    }
    
    public void show() {
        win.initModality(Modality.NONE); 
        win.show();
    }
    
    public Scene getScene() {
        return scene;
    }
    
    public Stage getStage() {
        return win;
    }
    
    public void showModal() {
        win.initModality(Modality.APPLICATION_MODAL); 
        win.showAndWait();
    }
    
    public HBox getToolbox() {
        return toolbox;
    }
    
    public VBox getMainContainer() {
        return basevbox;
    }
    
    private void init() {
        init(1200, 800);
    }
    
    private void init(double w, double h) {
        final Image logoImage = new Image(JNekoImageDB.class.getResourceAsStream("logo6.png"));
        final ImageView imgLogoV = new ImageView(logoImage);
        
        basevbox.getStylesheets().add(getClass().getResource("ILVWStyle.css").toExternalForm());
        basevbox.getStyleClass().add("basevbox");
        
        mvbox.getChildren().add(toolbarvbox);
        mvbox.getChildren().add(basevbox);
        root.getChildren().add(mvbox);

        toolbox.getStylesheets().add(getClass().getResource("ILVWStyle.css").toExternalForm());
        toolbox.getStyleClass().add("toolbox");
        toolbox.setMaxWidth(9999);
        toolbox.setPrefWidth(9999);
        toolbox.setPadding(new Insets(0,0,0,2));
        toolbox.setAlignment(Pos.BOTTOM_LEFT);

        headerbox.getStylesheets().add(getClass().getResource("ILVWStyle.css").toExternalForm());
        headerbox.getStyleClass().add("headerbox");
        headerbox.setMaxWidth(9999);
        
        logobox.getStylesheets().add(getClass().getResource("ILVWStyle.css").toExternalForm());
        logobox.getStyleClass().add("headerbox");
        logobox.setMaxSize(239, 64);
        logobox.setMinSize(239, 64);
        logobox.setPrefSize(239, 64);
        logobox.setAlignment(Pos.CENTER_RIGHT);
        logobox.getChildren().add(imgLogoV);
        
        headerbox.getChildren().add(toolbox);
        headerbox.getChildren().add(logobox);
        
        toolbarvbox.setPrefSize(9999, 70);
        toolbarvbox.setMaxSize(9999, 70);
        toolbarvbox.setMinSize(64, 70);
        toolbarvbox.setStyle("-fx-background-color: #000000;");
        
        toolbarvbox.getChildren().add(headerbox);
        
        win.getIcons().add(new Image(new File("./icons/icon128.png").toURI().toString()));
        win.getIcons().add(new Image(new File("./icons/icon64.png").toURI().toString()));
        win.getIcons().add(new Image(new File("./icons/icon32.png").toURI().toString()));
        
        win.setMinWidth(w / 2);
        win.setMinHeight(h / 2);
        win.setTitle("Images database");
        win.setScene(scene);
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) ResizeHelper.addResizeListener(win);
    }
    
    private Scene generateScene() {
        return generateScene(1200, 800);
    }
    
    private Scene generateScene(double w, double h) {
        final Scene scenex;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            StackPane root3m = GUITools.getWinGUI(this, win, DRD, root, mvbox, "ILVWStyle.css", GUITools.CLOSE_HIDE_WINDOW);
            scenex = new Scene(root3m, w, h);
            scenex.setFill(Color.TRANSPARENT);
        } else {
            scenex = new Scene(root, w, h);
        }
        return scenex;
    }
}
