package ui.dialogs.activities.engine;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public abstract class ActivityPage extends VBox {
	public interface CloseAction {
		void OnClose();
	}

	private CloseAction closeAction = null;

	private final ActivityHolder activityHolder;

	public abstract Node[] getSubheaderElements();
	public abstract Node[] getFooterElements();

	public ActivityPage(ActivityHolder activityHolder) {
		this.activityHolder = activityHolder;
	}

	public void showFirst() {
		getActivityHolder().showFirst(this);
	}

	public void showNext() {
		getActivityHolder().show(this);
	}

	public void close() {
		getActivityHolder().close();
		if (closeAction != null) {
			closeAction.OnClose();
		}
	}

	public ActivityHolder getActivityHolder() {
		return activityHolder;
	}

	public void popup(String title, String text) {
		getActivityHolder().popup(title, text);
	}

	public CloseAction getCloseAction() {
		return closeAction;
	}

	public void setCloseAction(CloseAction closeAction) {
		this.closeAction = closeAction;
	}
}
