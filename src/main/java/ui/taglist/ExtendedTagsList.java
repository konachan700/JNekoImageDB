package ui.taglist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.entity.TagEntity;
import proto.LocalDaoService;
import proto.UseServices;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.elements.PanelButton;

@HasStyledElements
public class ExtendedTagsList extends VBox implements UseServices {
	public interface SearchListener {
		void onSearch(Collection<TagEntity> tags);
	}

	@CssStyle({"tags_scroll_pane"})
	private final ScrollPane scrollPane = new ScrollPane();

	@CssStyle({"tags_null_folw_pane"})
	private final FlowPane flowPane = new FlowPane();

	@CssStyle({"tags_null_folw_pane"})
	private final FlowPane flowPaneSearch = new FlowPane();

	@CssStyle({"tag_text_field"})
	private final TextField textField = new TextField();

	@CssStyle({"tag_add_container"})
	private final HBox hBox = new HBox();

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton button = new PanelButton("Add tag") {
		@Override public void onClick(ActionEvent e) {
			addTag(textField.getText());
			textField.setText("");
		}
	};

	@CssStyle({"tags_null_pane"})
	private final VBox container = new VBox();

	@CssStyle({"tags_group_label"})
	private final Label quickSearch = new Label("Quick search");

	@CssStyle({"tags_group_label"})
	private final Label addToImage = new Label("Tags for image");

	private final Map<String, TagElement> map = new HashMap<>();
	private final LocalDaoService dao;
	private Set<TagEntity> defaultTags = null;
	private boolean disableNonExisted = false;
	private SearchListener searchListener = null;

	public ExtendedTagsList() {
		StyleParser.parseStyles(this);
		dao = getService(LocalDaoService.class);

		flowPane.setVgap(4);
		flowPane.setHgap(6);

		flowPaneSearch.setVgap(4);
		flowPaneSearch.setHgap(6);

		container.getChildren().addAll(quickSearch, flowPaneSearch, addToImage, flowPane);

		scrollPane.setContent(container);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		hBox.getChildren().addAll(textField, button);
		getChildren().addAll(hBox, scrollPane);

		textField.setPromptText("Type a tag...");
		textField.textProperty().addListener((e, o, n) -> {
			final String input = n.trim();
			if (input.isEmpty()) {
				flowPaneSearch.getChildren().clear();
				if (defaultTags != null) {
					defaultTags.stream().limit(16).forEach(t -> addTagToSearchBox(t.getTagText()));
				}
				return;
			}

			final List<String> list = Optional.ofNullable(dao.tagFindStartedWith(input, 16)).orElse(new ArrayList<>(1))
					.stream()
					.map(TagEntity::getTagText)
					.collect(Collectors.toList());
			if (list.isEmpty()) {
				flowPaneSearch.getChildren().clear();
			} else {
				flowPaneSearch.getChildren().clear();
				list.forEach(this::addTagToSearchBox);
			}
		});

		textField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				addTag(textField.getText());
			}
		});
	}

	private void addTagToSearchBox(String tag) {
		final TagElement tagElement = new TagElement(tag);
		tagElement.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				flowPaneSearch.getChildren().clear();
				addTag(tag);
			}
		});
		flowPaneSearch.getChildren().add(tagElement);
	}

	public void addTag(String tag) {
		if (tag == null || tag.trim().isEmpty() || map.containsKey(tag)) {
			sendEvent();
			return;
		}

		final boolean isTagExist = dao.tagIsExist(tag);
		if (disableNonExisted && !isTagExist) return;

		final TagElement tagElement = new TagElement(tag, isTagExist ? "tags_exist_tag" : "tags_not_exist_tag");
		tagElement.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				map.remove(tag);
				flowPane.getChildren().remove(tagElement);
				sendEvent();
			}
		});
		map.put(tag, tagElement);
		flowPane.getChildren().add(tagElement);
		textField.setText("");

		sendEvent();
	}

	private void sendEvent() {
		if (searchListener != null) {
			if (disableNonExisted) {
				searchListener.onSearch(map.keySet().stream().map(dao::tagGetEntity).collect(Collectors.toList()));
			}
		}
	}

	public Set<String> getTags() {
		return map.keySet();
	}

	public Set<TagEntity> getDefaultTags() {
		return defaultTags;
	}

	public void setDefaultTags(Set<TagEntity> defaultTags) {
		this.defaultTags = defaultTags;
		if (map.isEmpty()) {
			flowPaneSearch.getChildren().clear();
			if (defaultTags != null) {
				defaultTags.stream().limit(16).forEach(t -> addTagToSearchBox(t.getTagText()));
			}
		}
	}

	public boolean isDisableNonExisted() {
		return disableNonExisted;
	}

	public void setDisableNonExisted(boolean disableNonExisted) {
		this.disableNonExisted = disableNonExisted;
	}

	public SearchListener getSearchListener() {
		return searchListener;
	}

	public void setSearchListener(SearchListener searchListener) {
		this.searchListener = searchListener;
	}
}
