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
import ui.dialogs.windows.PasswordWindow;

public class Main extends Application implements UseServices {
    @Override
    public void start(Stage primaryStage) {
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        final PasswordWindow passwordWindow = new PasswordWindow();
		passwordWindow.show(true);
		if (passwordWindow.getPassword() != null) {
			init(passwordWindow.getPassword());
			passwordWindow.setPassword(null);

			final MainWindow mw = new MainWindow("Main window");
			mw.show(true);
		} else {
			PasswordWindow.disposeStatic();
		}

		exit(primaryStage);
    }

    private void exit(Stage primaryStage) {
		primaryStage.hide();
		primaryStage.close();
		Platform.exit();
	}

    public static void main(String[] args) {
        launch(args);
    }
}
