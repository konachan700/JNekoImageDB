package jnekoimagesdb.ui.md.dialogs.start;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jnekoimagesdb.ui.GUITools;

public class StartSplashScreen {
    public static final Image 
            SPLASH_IMAGE = GUITools.loadImage("splashLight");

    private final Stage           
            win = new Stage();
    
    private final Scene 
            scene;
    
     private final VBox
             root = new VBox();
    
    public StartSplashScreen() {
        scene = new Scene(root, 640, 480);
        win.setResizable(false);
        //win.setAlwaysOnTop(true);

        root.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_null_pane");
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            win.initStyle(StageStyle.TRANSPARENT);
        }
        
        final ImageView iw = new ImageView(SPLASH_IMAGE);
        root.getChildren().add(iw);
        
        win.setMinWidth(640);
        win.setMinHeight(480);
        win.setTitle("JNeko Images DB");
        win.setScene(scene);
    }
    
    public void show() {
        win.show();
    }
    
    public void hide() {
        win.hide();
    }
}
