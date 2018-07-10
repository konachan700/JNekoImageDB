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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import services.api.LocalStorageService;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.Paginator;
import ui.elements.PanelButton;
import ui.imageview.localfs.LocalFsImageDashboard;

@Component
public class ImportFromDiskActivity extends ActivityPage {
	private final IconNode iconForRootNode = new IconNode(GoogleMaterialDesignIcons.FOLDER);
	private final TreeItem<Path> rootItem = new TreeItem<> (null, iconForRootNode);

	@Autowired
	LocalStorageService localStorageService;

	@Autowired
	LocalFsImageDashboard fileImageList;

	@Autowired
	Paginator paginator;

	@Autowired
	@CssStyle({"tags_null_pane"})
	TagsAddActivity addActivity;

	@CssStyle({"tree_container", "StringFieldElementTextArea"})
	private final TreeView<Path> tree = new TreeView<> (rootItem);

	private final Set<String> selectedTags = new HashSet<>();

	@CssStyle({"tags_null_pane"})
	private final ImportFromDiskWaitActivity importFromDiskWaitActivity = new ImportFromDiskWaitActivity(getActivityHolder(), () -> {
		localStorageService.importProcessStop();
	});

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
			final Set<Path> files = fileImageList.getSelectedFiles();
			if (files.isEmpty()) {
				popup("Error!", "Select at least a one file before import!");
				return;
			}

			importFromDiskWaitActivity.showNext();
			localStorageService.importAllLocalDBItems(files, selectedTags, importFromDiskWaitActivity::inform);
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
			getChildren().add(new TreeItem<>());

			file = f;
			expandedProperty().addListener((e, o, n) -> {
				getChildren().clear();
				if (n) {
					final List<File> list = Optional.ofNullable(getFile().getAbsoluteFile().listFiles())
							.map(Arrays::asList)
							.orElse(null);
					if (Objects.isNull(list) || list.isEmpty()) {
						return;
					}
					Collections.sort(list);
					list.stream()
							.filter(File::isDirectory)
							.filter(File::canRead)
							.filter(f1 -> !f1.getName().startsWith("."))
							.forEach(el -> getChildren().add(new CustomTreeItem(el.getAbsoluteFile())));
				} else {
					getChildren().clear();
					getChildren().add(new TreeItem<>());
				}
			});
		}

		public File getFile() {
			return file;
		}
	}

	@PostConstruct
	void init() {
		StyleParser.parseStyles(this);

		paginator.setPageChangeAction((currentPage, pageCount) -> fileImageList.pageChanged(currentPage));

		addActivity.setActivityHolder(this.getActivityHolder());
		addActivity.setResultCallback((r,t) -> {
			if (r == TagsAddActivity.Result.ADD_TAG && t != null) {
				selectedTags.clear();
				selectedTags.addAll(t);
			}
		});

		fileImageList.getStyleClass().add("window_root_pane");
		fileImageList.setOnPageChangedEvent(paginator::setCurrentPageIndex);
		fileImageList.setOnPageCountChangedEvent(paginator::setPageCount);

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
