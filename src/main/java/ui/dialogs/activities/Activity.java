package ui.dialogs.activities;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public interface Activity {
	Node[] getSubheaderElements();
	Node[] getFooterElements();
	Node getActivity();

	default VBox getSeparator() {
		final VBox v = new VBox();
		v.getStyleClass().addAll("null_pane", "fill_all");
		return v;
	}
}
