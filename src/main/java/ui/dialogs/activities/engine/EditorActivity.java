package ui.dialogs.activities.engine;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import ui.StyleParser;
import ui.UICreator;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.elements.PanelButton;
import ui.proto.UIEntity;

@HasStyledElements
public class EditorActivity<T extends UIEntity> extends ActivityPage {
	public enum Result {
		SAVE, CANCEL
	}

	public interface ResultCallback<T extends UIEntity> {
		void onResult(Result result, T uiEntity);
	}

	private Result editorResult = Result.CANCEL;
	private final UIEntity uiEntity;
	private final ResultCallback resultCallback;

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton buttonSave = new PanelButton("Save") {
		@Override
		public void onClick(ActionEvent e) {
			editorResult = Result.SAVE;
			resultCallback.onResult(editorResult, uiEntity);
			close();
		}
	};

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton buttonCancel = new PanelButton("Cancel") {
		@Override
		public void onClick(ActionEvent e) {
			editorResult = Result.CANCEL;
			resultCallback.onResult(editorResult, uiEntity);
			close();
		}
	};

	public EditorActivity(ActivityHolder activityHolder, T uiEntity, ResultCallback<T> resultCallback) {
		super(activityHolder);

		this.uiEntity = uiEntity;
		this.resultCallback = resultCallback;

		StyleParser.parseStyles(this);
		getStyleClass().addAll("editor_activity_pane");

		final VBox vBox = UICreator.createUI(uiEntity);
		this.getChildren().add(vBox);
	}

	@Override public Node[] getSubheaderElements() {
		return new Node[] { ActivityHolder.getSeparator(), buttonCancel, buttonSave };
	}

	@Override public Node[] getFooterElements() {
		return null;
	}

	public Result getResult() {
		return editorResult;
	}
}
