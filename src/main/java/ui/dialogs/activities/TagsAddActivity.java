package ui.dialogs.activities;

import java.util.HashSet;
import java.util.Set;

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
	private final ResultCallback resultCallback;

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton addTags = new PanelButton("Add tags to image") {
		@Override
		public void onClick(ActionEvent e) {
			result = Result.ADD_TAG;
			resultCallback.onResult(result, extendedTagsList.getTags());
			close();
		}
	};

	public TagsAddActivity(ActivityHolder activityHolder, ResultCallback resultCallback) {
		super(activityHolder);
		StyleParser.parseStyles(this);

		this.resultCallback = resultCallback;
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

}
