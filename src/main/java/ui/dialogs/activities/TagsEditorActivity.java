package ui.dialogs.activities;

import java.util.Date;
import java.util.Optional;

import annotation.CssStyle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import model.TagMetadata;
import proto.LocalDaoService;
import proto.UseServices;
import ui.StyleParser;
import ui.elements.PanelButton;
import ui.taglist.TagElement;

public class TagsEditorActivity extends VBox implements Activity, UseServices {
	@CssStyle({"tags_scroll_pane"})
	private final ScrollPane scrollPane = new ScrollPane();

	@CssStyle({"tags_null_pane"})
	private final FlowPane flowPane = new FlowPane();

	@CssStyle({"tag_text_field"})
	private final TextField addTagTF = new TextField();

	@CssStyle({"tag_text_field"})
	private final TextField findTagTF = new TextField();

	@CssStyle({"tag_add_container_a1"})
	private final HBox addTagBox = new HBox();

	@CssStyle({"tag_add_container_a2"})
	private final HBox findTagBox = new HBox();

	@CssStyle({"tag_add_container"})
	private final HBox hBox = new HBox();

	@CssStyle({"tag_total_count_label"})
	private final Label totalCount = new Label("0");

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton addTagBtn = new PanelButton("Add tag", GoogleMaterialDesignIcons.ADD) {
		@Override public void onClick(ActionEvent e) {
			add();
		}
	};

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton findTagBtn = new PanelButton("Find", GoogleMaterialDesignIcons.FIND_IN_PAGE) {
		@Override public void onClick(ActionEvent e) {
			final String tag = findTagTF.getText().trim();
			find(tag);
		}
	};

	private void onTagClick(String tag, TagElement te) {

	}

	private void add() {
		Optional.ofNullable(getService(LocalDaoService.class)).ifPresent(dao -> {
			final String tag = addTagTF.getText().trim();
			final TagMetadata tagMetadata = new TagMetadata();
			tagMetadata.setMeta("createDate", System.currentTimeMillis());
			tagMetadata.setMeta("createType", "tm");
			dao.tagSave(tag, tagMetadata);
			addTagTF.setText("");
			find(null);
		});
	}

	private void find(String tag) {
		flowPane.getChildren().clear();

		Optional.ofNullable(getService(LocalDaoService.class))
				.map(e -> e.tagGetCount())
				.ifPresent(e -> totalCount.setText("Total tags count: " + e));

		Optional.ofNullable(getService(LocalDaoService.class))
				.map(e -> e.getTags())
				.map(e -> e.keySet())
				.ifPresent(e -> {
					if (tag != null && !tag.trim().isEmpty()) {
						e.parallelStream()
								.filter(t -> t.startsWith(tag))
								.limit(256)
								.forEach(t -> {
									Platform.runLater(() -> {
										final TagElement te = new TagElement(t);
										te.setOnMouseClicked(event -> onTagClick(t, te));
										flowPane.getChildren().add(te);
									});
								});
					} else {
						e.stream()
								.limit(256)
								.forEach(t -> {
									final TagElement te = new TagElement(t);
									te.setOnMouseClicked(event -> onTagClick(t, te));
									flowPane.getChildren().add(te);
								});
					}
				});
	}

	public TagsEditorActivity() {
		StyleParser.parseStyles(this);
		flowPane.setVgap(4);
		flowPane.setHgap(6);

		addTagTF.setPromptText("Type tag for add...");
		findTagTF.setPromptText("Type for search...");

		findTagTF.setOnKeyReleased(e -> find(findTagTF.getText().trim()));
		addTagTF.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) add();
		});

		scrollPane.setContent(flowPane);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		hBox.getChildren().addAll(addTagBox, findTagBox);
		addTagBox.getChildren().addAll(addTagTF, addTagBtn);
		findTagBox.getChildren().addAll(findTagTF, findTagBtn);

		this.getChildren().addAll(hBox, scrollPane);
	}

	@Override public Node[] getSubheaderElements() {
		return null;
	}

	@Override public Node[] getFooterElements() {
		return new Node[] { totalCount };
	}

	@Override public Node getActivity() {
		find(null);
		return this;
	}
}
