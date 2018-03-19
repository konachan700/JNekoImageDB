package ui;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import service.RootService;
import ui.activity.AbstractActivity;
import ui.activity.AllImagesActivity;
import ui.dialog.ImportImagesDialog;
import ui.dialog.PasswordDialog;
import ui.dialog.YesNoDialog;
import ui.imagelist.DBImageList;
import ui.menu.Menu;
import ui.menu.MenuGroup;
import ui.menu.MenuItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static service.RootService.getAuthService;

public class Main extends Application {
    private final PasswordDialog passwordDialog = new PasswordDialog();


    private static final Map<String, AbstractActivity> activities = new HashMap<>();

    private final HBox rootContainerBox = new HBox();
    private final VBox rootMenuBox = new VBox();
    private final VBox optionsBox = new VBox();
    private final VBox rootPane = new VBox();


    private void dispose() {
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        if (!RootService.loadConfig()) {
            new YesNoDialog("Cannot write application settings.", false).showAndWait();
            dispose();
        }

        passwordDialog.showAndWait();
        if (passwordDialog.getAction() != PasswordDialog.ACTION_OK) {
            dispose();
        } else {
            final String password = passwordDialog.getPassword();
            final byte[] authData = getAuthService().getAuthDataByPassword(password);
            if (Objects.isNull(authData)) {
                final YesNoDialog yesNoDialog = new YesNoDialog("Database, associated with this password do not exist. Would you create a new database with this password?", true);
                yesNoDialog.showAndWait();
                if (yesNoDialog.getAction() == YesNoDialog.ACTION_YES) {
                    final byte[] temporaryAuthData = getAuthService().createAuthDataByPassword(password);
                    if (Objects.isNull(temporaryAuthData)) {
                        new YesNoDialog("Cannot write auth-file to disk. Check your permissions.", false).showAndWait();
                        dispose();
                    } else {
                        RootService.initAllEncryptedStorages(temporaryAuthData);
                    }
                } else {
                    dispose();
                }
            } else {
                RootService.initAllEncryptedStorages(authData);
            }
        }

        final Scene scene = new Scene(rootContainerBox, RootService.getAppSettings().getMainWindowWidth(), RootService.getAppSettings().getMainWindowHeight());
        scene.heightProperty().addListener((e, o, n) -> {
            RootService.getAppSettings().setMainWindowHeight(n.doubleValue());
            RootService.saveConfig();
        });
        scene.widthProperty().addListener((e, o, n) -> {
            RootService.getAppSettings().setMainWindowWidth(n.doubleValue());
            RootService.saveConfig();
        });

        primaryStage.getIcons().add(new Image("/style/icons/icon32.png"));
        primaryStage.getIcons().add(new Image("/style/icons/icon64.png"));
        primaryStage.getIcons().add(new Image("/style/icons/icon128.png"));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest((e) -> {
            //RootService.dispose();
        });

        rootContainerBox.getStylesheets().add(getClass().getResource("/style/css/main.css").toExternalForm());

        final Menu mn = new Menu(
                new MenuGroup(
                        "Images", "menu_group_container_red",
                        new MenuItem("All local images", (c) -> {
                            showActicity("AllImages");
                        }),
                        /*new MenuItem("Albums", (c) -> {

                        }),*/
                        new MenuItem("Tags", (c) -> {

                        })
                ),
                new MenuGroup(
                        "Service", "menu_group_container_green",
                        new MenuItem("Settings", (c) -> {

                        }),
                        new MenuItem("Logs", (c) -> {

                        })
                )
        );

        addActivity("AllImages", new AllImagesActivity());
        showActicity("AllImages");

        rootMenuBox.getStyleClass().addAll("null_pane", "menu_270px_width", "max_height");
        optionsBox.getStyleClass().addAll("null_pane", "max_width", "height_48px");
        rootPane.getStyleClass().addAll("null_pane", "max_width", "max_height");

        rootMenuBox.getChildren().addAll(mn);
        rootContainerBox.getChildren().addAll(rootMenuBox, rootPane);
    }

    public void addActivity(String name, AbstractActivity activity) {
        if (activities.containsValue(activity)) return;
        activities.put(name, activity);
    }

    public void showActicity(String name) {
        rootPane.getChildren().clear();
        final AbstractActivity n = activities.get(name);
        if (Objects.nonNull(n)) {
            rootPane.getChildren().add(n);
            n.onShow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
