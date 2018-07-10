package ui.dialogs.activities;

import static ui.dialogs.activities.engine.ActivityHolder.getSeparator;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.PanelButton;

public class ImportFromDiskWaitActivity extends ActivityPage {
	public interface ResultCallback {
		void onInterrupt();
	}

	private final ResultCallback resultCallback;
	private final StringBuilder sb = new StringBuilder();

	private final ProgressBar progressBar = new ProgressBar();

	@CssStyle({"text-area-max-size"})
	private final TextArea textArea = new TextArea();

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton buttonBack = new PanelButton("Stop process") {
		@Override
		public void onClick(ActionEvent e) {
			resultCallback.onInterrupt();
			getActivityHolder().lockWindow(false);
			close();
		}
	};

	public ImportFromDiskWaitActivity(ActivityHolder activityHolder, ResultCallback resultCallback) {
		super();
		this.resultCallback = resultCallback;
		StyleParser.parseStyles(this);

		textArea.setWrapText(true);
		textArea.setEditable(false);
		textArea.setText("Logs\n");

		getChildren().addAll(textArea, progressBar);
	}

	public void inform(String text, long countInQueue) {
		sb.insert(0, "\n\n");
		sb.insert(0, text);
		textArea.setText(sb.toString());

		if (countInQueue == 0) {
			buttonBack.setText("Close");
			progressBar.setVisible(false);
		}
	}

	@Override public Node[] getSubheaderElements() {
		return new Node[] { getSeparator(), buttonBack };
	}

	@Override public Node[] getFooterElements() {
		return new Node[0];
	}

	private void show() {
		getActivityHolder().lockWindow(true);
		sb.delete(0, sb.length());
		textArea.clear();
		buttonBack.setText("Stop process");
		progressBar.setVisible(true);
		super.showNext();
	}

	@Override public void showNext() {
		show();
	}

	@Override public void showFirst() {
		show();
	}
}
