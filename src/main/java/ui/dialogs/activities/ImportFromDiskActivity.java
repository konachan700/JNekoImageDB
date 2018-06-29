package ui.dialogs.activities;

import static ui.dialogs.activities.engine.ActivityHolder.getSeparator;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import proto.LocalStorageService;
import proto.UseServices;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.Paginator;
import ui.elements.PanelButton;
import ui.imageview.localfs.LocalFsImageDashboard;

public class ImportFromDiskActivity extends ActivityPage implements UseServices {
	private final IconNode iconForRootNode = new IconNode(GoogleMaterialDesignIcons.FOLDER);
	private final TreeItem<Path> rootItem = new TreeItem<> (null, iconForRootNode);

	@CssStyle({"tree_container", "StringFieldElementTextArea"})
	private final TreeView<Path> tree = new TreeView<> (rootItem);

	private final Set<String> selectedTags = new HashSet<>();

	@CssStyle({"tags_null_pane"})
	private final TagsAddActivity addActivity = new TagsAddActivity(getActivityHolder(), (r,t) -> {
		if (r == TagsAddActivity.Result.ADD_TAG && t != null) {
			selectedTags.clear();
			selectedTags.addAll(t);
		}
	});

	@CssStyle({"tags_null_pane"})
	private final ImportFromDiskWaitActivity importFromDiskWaitActivity = new ImportFromDiskWaitActivity(getActivityHolder(), () -> {
		getService(LocalStorageService.class).importProcessStop();
	});

	private final LocalFsImageDashboard fileImageList = new LocalFsImageDashboard() {
		@Override public void onPageChanged(int page) {
			paginator.setCurrentPageIndex(page);
		}

		@Override public void onPageCountChanged(int pages) {
			paginator.setPageCount(pages);
		}
	};

	private final Paginator paginator = new Paginator() {
		@Override public void onPageChange(int currentPage, int pageCount) {
			fileImageList.pageChanged(currentPage);
		}
	};

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton buttonAddTags = new PanelButton("Add tags") {
		@Override
		public void onClick(ActionEvent e) {
			addActivity.showNext();
		}
	};

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton buttonImport = new PanelButton("Import selected") {
		@Override
		public void onClick(ActionEvent e) {
			importFromDiskWaitActivity.showNext();
			final Set<Path> files = fileImageList.getSelectedFiles();
			getService(LocalStorageService.class).importAllLocalDBItems(files, selectedTags, importFromDiskWaitActivity::inform);
		}
	};

	@CssStyle({"panel_button_footer_1"})
	private final PanelButton buttonSelectAll = new PanelButton("Select all") {
		@Override
		public void onClick(ActionEvent e) {
			fileImageList.selectAll();
		}
	};

	@CssStyle({"panel_button_footer_1"})
	private final PanelButton buttonSelectNone = new PanelButton("Clear selection") {
		@Override
		public void onClick(ActionEvent e) {
			fileImageList.selectNone();
		}
	};

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

	public ImportFromDiskActivity(ActivityHolder activityHolder) {
		super(activityHolder);
		StyleParser.parseStyles(this);

		//fileImageList.init();
		fileImageList.getStyleClass().add("window_root_pane");
		//fileImageList.generateView(6,5);

		tree.setEditable(false);
		tree.setShowRoot(false);
		tree.setCellFactory(param -> new TreeCellFactory());
		tree.setOnMouseClicked(e -> {
			final TreeItem<Path> treeItem = tree.getSelectionModel().getSelectedItem();
			if (Objects.nonNull(treeItem) && Objects.nonNull(treeItem.getValue())) {
				fileImageList.cd(treeItem.getValue());
			}
		});

		final HBox container = new HBox();
		container.getChildren().addAll(tree, fileImageList);
		getChildren().add(container);

		tree.setMinSize(270, 270);

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

	@Override public Node[] getSubheaderElements() {
		return new Node[] { getSeparator(), buttonAddTags, buttonImport };
	}

	@Override public Node[] getFooterElements() {
		return new Node[] { buttonSelectNone, buttonSelectAll, getSeparator(), paginator };
	}
}
