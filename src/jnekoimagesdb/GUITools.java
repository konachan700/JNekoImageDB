package jnekoimagesdb;

import java.io.File;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GUITools {
    public static final int 
            CLOSE_EXIT = 0,
            CLOSE_HIDE_WINDOW = 1;
    
    public static StackPane getWinGUI(Object THIS, Stage primaryStage, DragDelta DRD, StackPane root, VBox mvbox, String css, int closeFlag) {
        final DropShadow ds = new DropShadow();
        ds.setOffsetY(0f);
        ds.setRadius(0);
        ds.setWidth(12f); 
        ds.setHeight(12f);
        ds.setSpread(0.5f);
        ds.setColor(Color.color(0.0f, 0.0f, 0.0f));
        
//        final DropShadow dsn = new DropShadow();
//        dsn.setOffsetY(0f);
//        dsn.setRadius(4f);
//        dsn.setSpread(0.4f);
//        dsn.setColor(Color.color(0.0f, 0.0f, 0.0f));

        final Label imageName = new Label("JNeko Image Database");
        imageName.getStyleClass().add("appnamez");
        imageName.setMaxSize(9999, 16);
        imageName.setPrefSize(9999, 16);
        imageName.setAlignment(Pos.CENTER_LEFT);
//        imageName.setEffect(dsn);

        HBox header_z = new HBox(6);
        header_z.setMaxSize(9999, 32);
        header_z.setPrefSize(9999, 32);
        header_z.setMinSize(32, 32);
        header_z.getStylesheets().add(THIS.getClass().getResource(css).toExternalForm());
        header_z.getStyleClass().add("header_z");
        header_z.setAlignment(Pos.CENTER);

        header_z.setOnMousePressed((MouseEvent mouseEvent) -> {
            DRD.x = primaryStage.getX() - mouseEvent.getScreenX();
            DRD.y = primaryStage.getY() - mouseEvent.getScreenY();
        });

        header_z.setOnMouseDragged((MouseEvent mouseEvent) -> {
            primaryStage.setX(mouseEvent.getScreenX() + DRD.x);
            primaryStage.setY(mouseEvent.getScreenY() + DRD.y);
        });

        final Button close = new Button("", new ImageView(new Image(new File("./icons/close.png").toURI().toString())));
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

        final Button expand = new Button("", new ImageView(new Image(new File("./icons/up.png").toURI().toString()))); 
        setFixedSize(expand, 16, 16);
        expand.setOnMouseClicked((MouseEvent event) -> {
            primaryStage.setMaximized(!primaryStage.isMaximized());
        });

        final Button unexpand = new Button("", new ImageView(new Image(new File("./icons/dwn.png").toURI().toString())));
        setFixedSize(unexpand, 16, 16);
        unexpand.setOnMouseClicked((MouseEvent event) -> {
            primaryStage.setIconified(true);
        });

        final Button iconx = new Button("", new ImageView(new Image(new File("./icons/icon.png").toURI().toString()))); 
        setFixedSize(iconx, 16, 16);

        header_z.getChildren().addAll(getSeparator(4), iconx, imageName, unexpand, expand, close, getSeparator(8));

        mvbox.getChildren().add(header_z);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        StackPane root2m = new StackPane();
        root2m.getChildren().add(root);
        root2m.getStylesheets().add(THIS.getClass().getResource(css).toExternalForm());
        root2m.getStyleClass().add("border_z");

        StackPane root3m = new StackPane();
        root3m.setBackground(Background.EMPTY);
        root3m.getStylesheets().add(THIS.getClass().getResource(css).toExternalForm());
        root3m.getStyleClass().add("border_a");
        root3m.getChildren().add(root2m);

        root2m.setEffect(ds);

        return root3m;
    }
    
    public static void setFixedSize(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
        n.setMinSize(w, h);
    }
    
    public static void setMaxSize(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
    }
    
    public static VBox getSeparator() {
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
