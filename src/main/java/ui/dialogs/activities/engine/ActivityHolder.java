package ui.dialogs.activities.engine;

import java.util.Stack;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.dialogs.windows.engine.DefaultWindow;

@Component
@Scope("prototype")
public class ActivityHolder extends HBox {
	public interface OnCountChangeListener {
		void activitiesCountChanged(int count);
	}

	private OnCountChangeListener onCountChangeListener = null;
	private final Stack<ActivityPage> prevActivities = new Stack<>();
	private ActivityPage currentActivity = null;
	private DefaultWindow defaultWindow = null;

	public ActivityHolder() {
		super();
	}

	public void popup(String title, String text) {
		if (defaultWindow == null) {
			return;
		}
		this.getDefaultWindow().popup(title, text);
	}

	public void showFirst(ActivityPage activity) {
		if (defaultWindow == null) {
			return;
		}
		prevActivities.clear();
		currentActivity = null;
		show(activity);
	}

	public void show(ActivityPage activity) {
		if (defaultWindow == null) {
			return;
		}
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
		if (defaultWindow == null) {
			return;
		}
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
		if (defaultWindow == null) {
			return;
		}
		defaultWindow.getHeader().getChildren().forEach(e -> e.setVisible(!lock));
	}

	private void setFooterAndHeader(ActivityPage activity) {
		if (defaultWindow == null) {
			return;
		}
		if (defaultWindow.getFooter() != null) {
			defaultWindow.getFooter().getChildren().clear();
			final Node[] footerElements = activity.getFooterElements();
			if (footerElements != null) {
				defaultWindow.getFooter().getChildren().addAll(footerElements);
			}
		}

		if (defaultWindow.getSubheader() != null) {
			defaultWindow.getSubheader().getChildren().clear();
			final Node[] subheaderElements = activity.getSubheaderElements();
			if (subheaderElements != null) {
				defaultWindow.getSubheader().getChildren().addAll(subheaderElements);
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

	public DefaultWindow getDefaultWindow() {
		return defaultWindow;
	}

	public void setDefaultWindow(DefaultWindow defaultWindow) {
		this.defaultWindow = defaultWindow;
	}
}
