package jnekoimagesdb;

import dataaccess.Lang;
import java.io.File;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GUITools {
    public static class DragDelta {
        double x, y;
    }
    
    public static final int 
            CLOSE_EXIT = 0,
            CLOSE_HIDE_WINDOW = 1;
    
    public final static String
            CSS_FILE = new File("./style/style.css").toURI().toString();
    
    public static final void setStyle(Parent element, String className, String style) {
        element.getStylesheets().clear();
        element.getStylesheets().add(CSS_FILE);
        element.getStyleClass().clear();
        element.getStyleClass().add(className + "_" + style);
    }
    
    public static final Image loadIcon(String name) {
        return new Image(new File("./style/icons/"+name+".png").toURI().toString());
    }
    
    public static final ImageView loadIconIW(String name) {
        return new ImageView(loadIcon(name));
    }
    
    public static final StackPane getWinGUI(Object THIS, Stage primaryStage, DragDelta DRD, StackPane root, VBox mvbox, String css, int closeFlag, boolean red) {
        final Label imageName = new Label(Lang.GUITools_WinGUI_Title);
        setStyle(imageName, "GUITools", "imageName");
        imageName.setMaxSize(9999, 16);
        imageName.setPrefSize(9999, 16);
        imageName.setAlignment(Pos.CENTER_LEFT);

        HBox header_z = new HBox(6);
        header_z.setMaxSize(9999, 32);
        header_z.setPrefSize(9999, 32);
        header_z.setMinSize(32, 32);
        setStyle(header_z, "GUITools", "header_z");
        header_z.setAlignment(Pos.CENTER);

        header_z.setOnMousePressed((MouseEvent mouseEvent) -> {
            DRD.x = primaryStage.getX() - mouseEvent.getScreenX();
            DRD.y = primaryStage.getY() - mouseEvent.getScreenY();
        });

        header_z.setOnMouseDragged((MouseEvent mouseEvent) -> {
            primaryStage.setX(mouseEvent.getScreenX() + DRD.x);
            primaryStage.setY(mouseEvent.getScreenY() + DRD.y);
        });

        final Button close = new Button(Lang.NullString, loadIconIW("head-close-16"));
        setFixedSize(close, 16, 16);
        close.setOnMouseClicked((MouseEvent event) -> {
            switch (closeFlag) {
                case CLOSE_EXIT:
                    Platform.exit();
                    break;
                case CLOSE_HIDE_WINDOW:
                    primaryStage.close();
                    break;
            }
        });

        final Button expand = new Button(Lang.NullString, loadIconIW("head-maximize-16")); 
        setFixedSize(expand, 16, 16);
        expand.setOnMouseClicked((MouseEvent event) -> {
            primaryStage.setMaximized(!primaryStage.isMaximized());
        });

        final Button unexpand = new Button(Lang.NullString, loadIconIW("head-minimize-16")); 
        setFixedSize(unexpand, 16, 16);
        unexpand.setOnMouseClicked((MouseEvent event) -> {
            primaryStage.setIconified(true);
        });

        final Button iconx = new Button(Lang.NullString, loadIconIW("app-icon-16"));
        setFixedSize(iconx, 16, 16);

        header_z.getChildren().addAll(getSeparator(4), iconx, imageName, getSeparator(8), unexpand, getSeparator(8), expand, getSeparator(8), close, getSeparator(8));

        mvbox.getChildren().add(header_z);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        StackPane root2m = new StackPane();
        root2m.getChildren().add(root);
        setStyle(root2m, "GUITools", "root2m");

        return root2m;
    }
    
    public static final void setFixedSize(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
        n.setMinSize(w, h);
    }
    
    public static final void setMaxSize(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
    }
    
    public static final VBox getSeparator() {
        VBox sep1 = new VBox();
        setMaxSize(sep1, 9999, 16);
        return sep1;
    }
    
    public static VBox getSeparator(double sz) {
        VBox sep1 = new VBox();
        setFixedSize(sep1, sz, sz);
        return sep1;
    }
}
