package ui.dialogs.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import model.entity.TagEntity;
import services.api.LocalDaoService;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.PanelButton;
import ui.taglist.TagElement;

@Component
public class TagsEditorActivity extends ActivityPage {
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

	@Autowired
	LocalDaoService localDaoService;

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

	private void onTagClick(MouseEvent me, String tag, TagElement te) {
		switch (me.getButton()) {
		case PRIMARY:

			break;
		case SECONDARY:

			break;
		default:
		}
	}

	private void add() {
		final String tag = addTagTF.getText().trim();
		localDaoService.tagSave(tag);
		addTagTF.setText("");
		find(null);
	}

	private void find(String tag) {
		flowPane.getChildren().clear();
		totalCount.setText("Total tags count: " + localDaoService.tagGetCount());

		final List<TagEntity> tagEntities;
		if (tag != null && !tag.trim().isEmpty()) {
			tagEntities = Optional.ofNullable(localDaoService.tagFindStartedWith(tag, 200)).orElse(new ArrayList<>(1));
		} else {
			tagEntities = Optional.ofNullable(localDaoService.tagGetEntitiesList(0, 200)).orElse(new ArrayList<>(1));
		}

		tagEntities.stream()
				.map(t -> t.getTagText())
				.forEach(t -> {
					final TagElement te = new TagElement(t);
					te.setOnMouseClicked(event -> onTagClick(event, t, te));
					flowPane.getChildren().add(te);
				});
	}

	public TagsEditorActivity() {
		super();

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

	@Override public void showFirst() {
		super.showFirst();
		find("");
	}

	@Override public void showNext() {
		super.showNext();
		find("");
	}

	@Override public Node[] getSubheaderElements() {
		return new Node[] {  };
	}

	@Override public Node[] getFooterElements() {
		return new Node[] { totalCount };
	}
}
