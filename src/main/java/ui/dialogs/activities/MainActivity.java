package ui.dialogs.activities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.entity.ImageEntity;
import model.entity.TagEntity;
import proto.LocalDaoService;
import proto.UseServices;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.Paginator;
import ui.elements.PanelButton;
import ui.imageview.localdb.LocalDbImageDashboard;
import ui.taglist.ExtendedTagsList;

@HasStyledElements
public class MainActivity extends ActivityPage implements UseServices {
	private LocalDaoService localDaoService;

	@CssStyle({"window_root_pane"})
	private final ImportFromDiskActivity rootImportActivity = new ImportFromDiskActivity(this.getActivityHolder());

	@CssStyle({"window_root_pane"})
	private final TagsEditorActivity tagsEditorActivity = new TagsEditorActivity(this.getActivityHolder());

	@CssStyle({"window_root_pane"})
	private final ImageViewActivityLocalDb imageViewActivity = new ImageViewActivityLocalDb(this.getActivityHolder());

	@CssStyle({"window_root_pane"})
	private final HBox container = new HBox();

	@CssStyle({"tree_container", "StringFieldElementTextArea"})
	private final VBox containerTags = new VBox();

	private final ExtendedTagsList extendedTagsList = new ExtendedTagsList();
	private final Set<TagEntity> tagFilter = new HashSet<>();

	@CssStyle({"window_root_pane"})
	private final LocalDbImageDashboard localDbImageDashboard = new LocalDbImageDashboard() {
		@Override public void onPageChanged(int page) {
			paginator.setCurrentPageIndex(page);
		}

		@Override public void onPageCountChanged(int pages) {
			paginator.setPageCount(pages);
			extendedTagsList.setDefaultTags(localDbImageDashboard.getRecomendedTags());
		}

		@Override public void onItemClick(MouseEvent e, ImageEntity image, int pageId, int id, int pageCount) {
			imageViewActivity.showNext(tagFilter, image, pageId, id, localDbImageDashboard.getElementsPerPage(), pageCount);
		}
	};

	@CssStyle({"tags_null_pane"})
	private final TagsAddActivity addTagsActivity = new TagsAddActivity(getActivityHolder(), (r,t) -> {
		if (r == TagsAddActivity.Result.ADD_TAG && t != null && !t.isEmpty() && !localDbImageDashboard.getSelectedFiles().isEmpty()) {
			final List<TagEntity> list = localDaoService.tagGetOrCreate(t);
			localDbImageDashboard.getSelectedFiles().forEach(e -> localDaoService.imagesWrite(e.getImageHash(), list));
			localDbImageDashboard.selectNone();
		}
	});

	private final Paginator paginator = new Paginator() {
		@Override public void onPageChange(int currentPage, int pageCount) {
			localDbImageDashboard.pageChanged(currentPage);
		}
	};

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton importImages = new PanelButton("Import images") {
		@Override
		public void onClick(ActionEvent e) {
			rootImportActivity.showNext();
		}
	};

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton tagEditor = new PanelButton("Tag editor") {
		@Override
		public void onClick(ActionEvent e) {
			tagsEditorActivity.showNext();
		}
	};

	@CssStyle({"panel_button_footer_1"})
	private final PanelButton addTagsToSelected = new PanelButton("Add tags for selected") {
		@Override
		public void onClick(ActionEvent e) {
			if (localDbImageDashboard.getSelectedFiles().isEmpty()) {
				popup("Error!", "Select at least a one file before setting tags!");
				return;
			}
			addTagsActivity.showNext();
		}
	};

	@CssStyle({"panel_button_footer_1"})
	private final PanelButton buttonSelectAll = new PanelButton("Select all") {
		@Override
		public void onClick(ActionEvent e) {
			localDbImageDashboard.selectAll();
		}
	};

	@CssStyle({"panel_button_footer_1"})
	private final PanelButton buttonSelectNone = new PanelButton("Clear selection") {
		@Override
		public void onClick(ActionEvent e) {
			localDbImageDashboard.selectNone();
		}
	};

	public MainActivity(ActivityHolder activityHolder) {
		super(activityHolder);
		StyleParser.parseStyles(this);
		this.localDaoService = getService(LocalDaoService.class);

		localDbImageDashboard.generateView(6,5);

		extendedTagsList.setDisableNonExisted(true);
		extendedTagsList.setSearchListener(list -> {
			tagFilter.clear();
			tagFilter.addAll(list);
			refresh();
		});

		containerTags.getChildren().addAll(extendedTagsList);
		container.getChildren().addAll(containerTags, localDbImageDashboard);
		getChildren().add(container);
	}

	public void refresh() {
		if (localDbImageDashboard.getElementsPerPage() == 0) return;
		localDbImageDashboard.refresh(0);
		localDbImageDashboard.setFilterByTags(tagFilter);

		paginator.setCurrentPageIndex(0);
	}

	@Override
	public void showNext() {
		super.showNext();
		refresh();
	}

	@Override
	public void showFirst() {
		super.showFirst();
		refresh();
	}

	@Override public Node[] getSubheaderElements() {
		return new Node[] { importImages, tagEditor };
	}

	@Override public Node[] getFooterElements() {
		return new Node[] { addTagsToSelected, buttonSelectAll, buttonSelectNone, ActivityHolder.getSeparator(), paginator };
	}
}
