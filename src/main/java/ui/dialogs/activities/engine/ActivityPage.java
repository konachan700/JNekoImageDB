package ui.dialogs.activities.engine;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public abstract class ActivityPage extends VBox {
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
	}

	public ActivityHolder getActivityHolder() {
		return activityHolder;
	}

	public void popup(String title, String text) {
		getActivityHolder().popup(title, text);
	}
}
