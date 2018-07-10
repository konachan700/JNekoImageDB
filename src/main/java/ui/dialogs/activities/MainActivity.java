package ui.dialogs.activities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.entity.TagEntity;
import services.api.LocalDaoService;
import services.api.UtilService;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.Paginator;
import ui.elements.PanelButton;
import ui.imageview.localdb.LocalDbImageDashboard;
import ui.taglist.ExtendedTagsList;

@Component
@HasStyledElements
public class MainActivity extends ActivityPage {
	@Autowired
	LocalDaoService localDaoService;

	@Autowired
	UtilService utilService;

	@Autowired
	ExtendedTagsList extendedTagsList;

	@Autowired
	@CssStyle({"window_root_pane"})
	LocalDbImageDashboard localDbImageDashboard;

	@Autowired
	@CssStyle({"window_root_pane"})
	ImportFromDiskActivity rootImportActivity;

	@Autowired
	@CssStyle({"window_root_pane"})
	TagsEditorActivity tagsEditorActivity;

	@Autowired
	@CssStyle({"window_root_pane"})
	ImageViewActivityLocalDb imageViewActivity;

	@Autowired
	@CssStyle({"tags_null_pane"})
	TagsAddActivity addTagsActivity;

	@Autowired
	Paginator paginator;

	@CssStyle({"window_root_pane"})
	private final HBox container = new HBox();

	@CssStyle({"tree_container", "StringFieldElementTextArea"})
	private final VBox containerTags = new VBox();

	private final Set<TagEntity> tagFilter = new HashSet<>();

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

	@PostConstruct
	void init() {
		StyleParser.parseStyles(this);

		paginator.setPageChangeAction((currentPage, pageCount) -> localDbImageDashboard.pageChanged(currentPage));
		addTagsActivity.setResultCallback((r,t) -> {
			if (r == TagsAddActivity.Result.ADD_TAG && t != null && !t.isEmpty() && !localDbImageDashboard.getSelectedFiles().isEmpty()) {
				final List<TagEntity> list = localDaoService.tagGetOrCreate(t);
				localDbImageDashboard.getSelectedFiles().forEach(e -> localDaoService.imagesWrite(e.getImageHash(), list));
				localDbImageDashboard.selectNone();
			}
		});
		rootImportActivity.setCloseAction(() -> {
			tagFilter.clear();
			refresh();
		});
		localDbImageDashboard.setOnPageChangedEvent(paginator::setCurrentPageIndex);
		localDbImageDashboard.setOnPageCountChangedEvent(e -> {
			paginator.setPageCount(e);
			extendedTagsList.setDefaultTags(localDbImageDashboard.getRecomendedTags());
		});
		localDbImageDashboard.generateView(utilService.getConfig().getLocalDbPreviewsCountInRow(), utilService.getConfig().getLocalDbPreviewsCountInCol());

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
	public void setActivityHolder(ActivityHolder activityHolder) {
		super.setActivityHolder(activityHolder);

		imageViewActivity.setActivityHolder(activityHolder);
		addTagsActivity.setActivityHolder(activityHolder);
		rootImportActivity.setActivityHolder(activityHolder);
		addTagsActivity.setActivityHolder(activityHolder);
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
