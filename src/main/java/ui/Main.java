package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import model.BinaryImage;
import proto.UseServices;
import service.InitService;
import ui.dialogs.windows.MainWindow;

public class Main extends Application implements UseServices {
    @Override
    public void start(Stage primaryStage) {
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        init("123");

        final MainWindow mw = new MainWindow("Main window");
        mw.show(true);

        primaryStage.hide();
        primaryStage.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
