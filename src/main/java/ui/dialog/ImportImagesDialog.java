package ui.dialog;

import fao.ImageFile;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import service.RootService;
import service.fs.FilePusherActionListener;
import service.fs.FilePusherTask;
import service.resizer.ImageResizeTaskType;
import ui.imagelist.BaseImageList;
import ui.imagelist.FileImageList;
import ui.simplepanel.Panel;
import ui.simplepanel.PanelButton;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImportImagesDialog extends Stage implements FilePusherActionListener {
    private final IconNode iconForRootNode = new IconNode(GoogleMaterialDesignIcons.FOLDER);

    private final class TreeCellFactory extends TreeCell<Path> {
        private final IconNode iconForNode = new IconNode(GoogleMaterialDesignIcons.FOLDER);
        @Override
        public void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                iconForNode.getStyleClass().add("tree_dir_icon");
                final String name = item.toFile().getName();
                setText(name.isEmpty() ? item.toFile().getAbsolutePath() : name);
                setGraphic(iconForNode);
            }
        }
    }

    private final class CustomTreeItem extends TreeItem<Path> {
        private final IconNode iconForNode = new IconNode(GoogleMaterialDesignIcons.FOLDER);
        private final File file;

        public CustomTreeItem(File f) {
            super(f.getAbsoluteFile().toPath());

            iconForNode.getStyleClass().add("tree_dir_icon");
            super.setGraphic(iconForNode);
            getChildren().add(new TreeItem());

            file = f;
            expandedProperty().addListener((e, o, n) -> {
                getChildren().clear();
                if (n.booleanValue()) {
                    final List<File> list = Optional.ofNullable(getFile().getAbsoluteFile().listFiles())
                            .map(arr -> Arrays.asList(arr))
                            .orElse(null);
                    if (Objects.isNull(list) || list.isEmpty()) {
                        return;
                    }
                    Collections.sort(list);
                    list.stream()
                            .filter(f1 -> f1.isDirectory())
                            .filter(f1 -> f1.canRead())
                            .filter(f1 -> !f1.getName().startsWith("."))
                            .forEach(el -> getChildren().add(new CustomTreeItem(el.getAbsoluteFile())));
                } else {
                    getChildren().clear();
                    getChildren().add(new TreeItem());
                }
            });
        }

        public File getFile() {
            return file;
        }
    }

    private final FileImageList baseImageList = new FileImageList();
    private final VBox treePane = new VBox();
    private final VBox rootPane = new VBox();

    private final WaitDialog waitDialog = new WaitDialog();

    private final Panel bottomPanel = new Panel(
            Panel.getSpacer(),
            baseImageList.getPaginator()
    );

    private final Panel topPanel = new Panel(
            new PanelButton("Refresh", GoogleMaterialDesignIcons.REFRESH, e -> {
                refresh();
            }),
            Panel.getSpacer(),
            new PanelButton("Select all", GoogleMaterialDesignIcons.SELECT_ALL, e -> {
                baseImageList.selectAll();
            }),
            new PanelButton("Clear selection", GoogleMaterialDesignIcons.DELETE, e -> {
                baseImageList.selectNone();
            }),
            Panel.getFixedSpacer(),
            new PanelButton("Import to DB", GoogleMaterialDesignIcons.IMPORT_EXPORT, e -> {
                final Set<ImageFile> selectedImageFiles = baseImageList.getSelectedImageFiles();
                if (selectedImageFiles.isEmpty()) return;
                selectedImageFiles.forEach(img -> {
                    RootService.getFileService().pushImageToStorage(new FilePusherTask(this, img.getImagePath()));
                });

                waitDialog.clearText();
                waitDialog.startProgress();
                baseImageList.selectNone();
            }),
            new PanelButton("Close", GoogleMaterialDesignIcons.EXIT_TO_APP, e -> {
                this.hide();
            })
    );

    private final HBox contentBoxPane = new HBox();
    private final TreeItem<Path> rootItem = new TreeItem<> (null, iconForRootNode);
    private final TreeView<Path> tree = new TreeView<> (rootItem);

    public void dispose() {
        baseImageList.dispose();
    }

    public ImportImagesDialog() {
        super();

        iconForRootNode.getStyleClass().add("tree_dir_icon");

        rootPane.getStylesheets().add(getClass().getResource("/style/css/main.css").toExternalForm());
        baseImageList.getStyleClass().addAll("max_width", "max_height");
        contentBoxPane.getStyleClass().addAll("null_pane", "max_width", "max_height", "border_1px");
        treePane.getStyleClass().addAll("null_pane", "max_height", "menu_270px_width", "right_border_only");
        tree.getStyleClass().addAll("StringFieldElementTextArea", "max_width", "max_height");
        rootPane.getStyleClass().addAll("null_pane", "max_width", "max_height");

        treePane.getChildren().addAll(tree);
        contentBoxPane.getChildren().addAll(treePane, baseImageList);
        rootPane.getChildren().addAll(topPanel, contentBoxPane, bottomPanel);

        final Scene scene = new Scene(rootPane, RootService.getAppSettings().getImportWindowWidth(), RootService.getAppSettings().getImportWindowHeight());
        scene.heightProperty().addListener((e, o, n) -> {
            RootService.getAppSettings().setImportWindowHeight(n.doubleValue());
            RootService.saveConfig();
        });
        scene.widthProperty().addListener((e, o, n) -> {
            RootService.getAppSettings().setImportWindowWidth(n.doubleValue());
            RootService.saveConfig();
        });

        this.getIcons().add(new Image("/style/icons/icon32.png"));
        this.getIcons().add(new Image("/style/icons/icon64.png"));
        this.getIcons().add(new Image("/style/icons/icon128.png"));

        this.setTitle("Import images from disk...");
        this.setScene(scene);
        //this.setOnCloseRequest((e) -> {});
        this.setOnShown(e -> {

        });

        baseImageList.generateView(5, 5);

        tree.setEditable(false);
        tree.setShowRoot(false);
        tree.setCellFactory(param -> new TreeCellFactory());
        tree.setOnMouseClicked(e -> {
            final TreeItem<Path> treeItem = tree.getSelectionModel().getSelectedItem();
            if (Objects.nonNull(treeItem) && Objects.nonNull(treeItem.getValue())) {
                final CopyOnWriteArrayList<ImageFile> list = RootService.getFileService().readImagesFromDirectory(treeItem.getValue());
                if (Objects.nonNull(list)) {
                    //System.out.println("readImagesFromDirectory " + treeItem.getValue().toFile().getAbsolutePath());
                    baseImageList.setImages(list);
                }
            }
        });

        refresh();
    }

    public void refresh() {
        rootItem.getChildren().clear();
        FileSystems.getDefault().getRootDirectories().forEach(dir -> {
            final CustomTreeItem customTreeItem = new CustomTreeItem(dir.toFile());
            rootItem.getChildren().add(customTreeItem);
        });
        if (rootItem.getChildren().size() == 1) {
            rootItem.getChildren().get(0).setExpanded(true);
        }
    }

    @Override
    public void onPush(Path p, int totalCount, int currentCount) {
        waitDialog.setCaption("Processing " + currentCount + "...");
        waitDialog.setText("Saved: " + p.toFile().getName());
    }

    @Override
    public void onDuplicateDetected(Path p) {
        waitDialog.setText("Duplicate: " + p.toFile().getName());
    }

    @Override
    public void onError(Path p, Exception e) {
        waitDialog.setText("Error or duplicate content: " + p.toFile().getName());
    }

    @Override
    public void onZeroQuene() {
        waitDialog.stopProgress();
    }
}
