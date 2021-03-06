package ui.dialogs.activities.engine;

import java.util.Stack;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import ui.dialogs.windows.engine.DefaultWindow;

public class ActivityHolder extends HBox {
	public static interface OnCountChangeListener {
		void activitiesCountChanged(int count);
	}

	private OnCountChangeListener onCountChangeListener = null;

	private final Stack<ActivityPage> prevActivities = new Stack<>();
	private ActivityPage currentActivity;
	private final Pane footerHolder, subheaderHolder, windowHeader;
	private final DefaultWindow defaultWindow;

	public ActivityHolder(DefaultWindow defaultWindow) {
		super();
		this.defaultWindow = defaultWindow;
		this.footerHolder = defaultWindow.getFooter();
		this.subheaderHolder = defaultWindow.getSubheader();
		this.windowHeader = defaultWindow.getHeader();
	}

	public void popup(String title, String text) {
		this.defaultWindow.popup(title, text);
	}

	public void showFirst(ActivityPage activity) {
		prevActivities.clear();
		currentActivity = null;
		show(activity);
	}

	public void show(ActivityPage activity) {
		if (currentActivity != null) {
			prevActivities.push(currentActivity);
		}
		currentActivity = activity;
		this.getChildren().clear();
		this.getChildren().add(activity);
		setFooterAndHeader(activity);
		if (onCountChangeListener != null) {
			onCountChangeListener.activitiesCountChanged(prevActivities.size());
		}
	}

	public void close() {
		if (prevActivities.empty()) {
			return;
		}
		final ActivityPage ap = currentActivity;

		currentActivity = prevActivities.pop();
		this.getChildren().clear();
		this.getChildren().add(currentActivity);
		setFooterAndHeader(currentActivity);
		if (onCountChangeListener != null) {
			onCountChangeListener.activitiesCountChanged(prevActivities.size());
		}

		if (ap.getCloseAction() != null) {
			ap.getCloseAction().OnClose();
		}
	}

	public void lockWindow(boolean lock) {
		windowHeader.getChildren().forEach(e -> e.setVisible(!lock));
	}

	private void setFooterAndHeader(ActivityPage activity) {
		if (footerHolder != null) {
			footerHolder.getChildren().clear();
			final Node[] footerElements = activity.getFooterElements();
			if (footerElements != null) {
				footerHolder.getChildren().addAll(footerElements);
			}
		}

		if (subheaderHolder != null) {
			subheaderHolder.getChildren().clear();
			final Node[] subheaderElements = activity.getSubheaderElements();
			if (subheaderElements != null) {
				subheaderHolder.getChildren().addAll(subheaderElements);
			}
		}
	}

	public static VBox getSeparator() {
		final VBox v = new VBox();
		v.getStyleClass().addAll("null_pane", "fill_all");
		return v;
	}

	public OnCountChangeListener getOnCountChangeListener() {
		return onCountChangeListener;
	}

	public void setOnCountChangeListener(OnCountChangeListener onCountChangeListener) {
		this.onCountChangeListener = onCountChangeListener;
	}
}
