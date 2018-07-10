package ui.dialogs.activities;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.PanelButton;
import ui.proto.UIEntity;
import ui.taglist.ExtendedTagsList;

@Component
@HasStyledElements
public class TagsAddActivity extends ActivityPage {
	public enum Result {
		ADD_TAG, CANCEL
	}

	public interface ResultCallback<T extends UIEntity> {
		void onResult(Result result, Set<String> tags);
	}

	private final ExtendedTagsList extendedTagsList = new ExtendedTagsList();

	private Result result = Result.CANCEL;
	private ResultCallback resultCallback = null;

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton addTags = new PanelButton("Add tags to image") {
		@Override
		public void onClick(ActionEvent e) {
			result = Result.ADD_TAG;
			getResultCallback().onResult(result, extendedTagsList.getTags());
			close();
		}
	};

	@PostConstruct
	void init() {
		StyleParser.parseStyles(this);
		this.setResultCallback(getResultCallback());
		getChildren().add(extendedTagsList);
	}

	@Override public Node[] getSubheaderElements() {
		return new Node[] { ActivityHolder.getSeparator(), addTags };
	}

	@Override public Node[] getFooterElements() {
		return new Node[0];
	}

	public Result getResult() {
		return result;
	}

	public Set<String> getTags() {
		return extendedTagsList.getTags();
	}

	public ResultCallback getResultCallback() {
		return resultCallback;
	}

	public void setResultCallback(ResultCallback resultCallback) {
		this.resultCallback = resultCallback;
	}
}
