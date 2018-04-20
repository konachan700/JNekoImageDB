package ui.dialogs;

import annotation.CssStyle;
import annotation.HasStyledElements;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import ui.StyleParser;
import ui.elements.Paginator;
import ui.elements.PanelButton;
import ui.imagelist.SelectedFilesList;
import ui.taglist.TagsList;
import ui.imagelist.FileImageDashboard;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@HasStyledElements
public class ImagesImportDialog extends DefaultWindow {
    /** TODO
     *  1. Add dropdown list with 10 existed tags for tags field
     *  2. Add "Already imported" mark for images, what are present in DB now
     *  3. Add selected/unselected state to tabs buttons in subheader
     * */

    private final IconNode iconForRootNode = new IconNode(GoogleMaterialDesignIcons.FOLDER);
    private final TreeItem<Path> rootItem = new TreeItem<> (null, iconForRootNode);

    @CssStyle({"tree_container", "StringFieldElementTextArea"})
    private final TreeView<Path> tree = new TreeView<> (rootItem);

    @CssStyle({"tags_content_pane"})
    private final TagsList tagsList = new TagsList();

    @CssStyle({"tags_content_pane"})
    private final SelectedFilesList selectedFilesList = new SelectedFilesList();

    private final Paginator paginator = new Paginator() {
        @Override public void onPageChange(int currentPage, int pageCount) {
            fileImageList.pageChanged(currentPage);
        }
    };

    private final FileImageDashboard fileImageList = new FileImageDashboard() {
        @Override public void onPageChanged(int page) {
            paginator.setCurrentPageIndex(page);
        }

        @Override public void onPageCountChanged(int pages) {
            paginator.setPageCount(pages);
        }
    };

    @CssStyle({"window_root_pane"})
    private HBox listContainer = new HBox();

    @CssStyle({"window_root_pane"})
    private HBox selAndTagsContainer = new HBox();

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

    @CssStyle({"panel_button_big_1"})
    private final PanelButton buttonCancel = new PanelButton("Close") {
        @Override
        public void onClick(ActionEvent e) {

        }
    };

    @CssStyle({"panel_button_big_1"})
    private final PanelButton buttonImport = new PanelButton("Import selected") {
        @Override
        public void onClick(ActionEvent e) {

        }
    };

    @CssStyle({"panel_button_subheader_1"})
    private final PanelButton buttonFiles = new PanelButton("Directories tree") {
        @Override
        public void onClick(ActionEvent e) {
            showContent(listContainer);
        }
    };

    @CssStyle({"panel_button_subheader_1"})
    private final PanelButton buttonSelected = new PanelButton("Add tags & Selected items") {
        @Override
        public void onClick(ActionEvent e) {
            if (showContent(selAndTagsContainer)) {
                selectedFilesList.refresh(fileImageList.getSelectedFiles());
            }
        }
    };

    @CssStyle({"panel_button_subheader_1"})
    private final PanelButton buttonSelectAll = new PanelButton("Select all") {
        @Override
        public void onClick(ActionEvent e) {
            fileImageList.selectAll();
        }
    };

    @CssStyle({"panel_button_subheader_1"})
    private final PanelButton buttonSelectNone = new PanelButton("Clear selection") {
        @Override
        public void onClick(ActionEvent e) {
            fileImageList.selectNone();
        }
    };

    private boolean showContent(Node n) {
        if (!getContent().getChildren().contains(n)) {
            getContent().getChildren().clear();
            getContent().getChildren().add(n);
            return true;
        }
        return false;
    }

    public ImagesImportDialog() {
        super("Import images from disk...", true, true, true);
        StyleParser.parseStyles(this);

        fileImageList.init();
        fileImageList.getStyleClass().add("window_root_pane");
        fileImageList.generateView(6,5);

        tree.setEditable(false);
        tree.setShowRoot(false);
        tree.setCellFactory(param -> new TreeCellFactory());
        tree.setOnMouseClicked(e -> {
            final TreeItem<Path> treeItem = tree.getSelectionModel().getSelectedItem();
            if (Objects.nonNull(treeItem) && Objects.nonNull(treeItem.getValue())) {
                fileImageList.cd(treeItem.getValue());
            }
        });

        getContent().getChildren().addAll(listContainer);
        listContainer.getChildren().addAll(tree, fileImageList);
        selAndTagsContainer.getChildren().addAll(selectedFilesList, tagsList);

        tree.setMinSize(270, 270);

        getHeader().getChildren().addAll(getSeparator(), buttonImport, buttonCancel);
        getSubheader().getChildren().addAll(buttonFiles, buttonSelected, getSeparator(), buttonSelectNone, buttonSelectAll, getSeparator(), paginator);

        refresh();
    }

    private VBox getSeparator() {
        final VBox v = new VBox();
        v.getStyleClass().addAll("null_pane", "fill_all");
        return v;
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
}
