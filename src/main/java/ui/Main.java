package ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import ui.dialogs.windows.MainWindow;
import ui.dialogs.windows.engine.DefaultWindow;
import ui.imageview.AbstractImageDashboard;

@EnableAutoConfiguration
@SpringBootApplication
@Configuration
@ComponentScan(basePackages = { "services", "model", "ui", "utils", "worker" })
public class Main extends Application {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private static String[] savedArgs;
	private ConfigurableApplicationContext context;

	@Autowired
	MainWindow mainWindow;

    @Override
    public void start(Stage primaryStage) {
		context = SpringApplication.run(Main.class, savedArgs);
		context.getAutowireCapableBeanFactory().autowireBean(this);

        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

		mainWindow.show(true);

		exit(primaryStage);
    }

    private void exit(Stage primaryStage) {
		AbstractImageDashboard.disposeStatic();
		DefaultWindow.disposeStatic();

		context.close();

		primaryStage.hide();
		primaryStage.close();
		Platform.exit();
	}

    public static void main(String[] args) {
		System.setProperty("prism.lcdtext", "false");
		System.setProperty("prism.text", "t2k");
		savedArgs = args;
        launch(args);
    }
}
