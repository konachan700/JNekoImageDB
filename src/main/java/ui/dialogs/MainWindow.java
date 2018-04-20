package ui.dialogs;

import annotation.CssStyle;
import annotation.HasStyledElements;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import proto.UseServices;
import ui.StyleParser;
import ui.dialogs.activities.Activity;
import ui.dialogs.activities.MainActivity;
import ui.dialogs.activities.TagsEditorActivity;
import ui.elements.Paginator;
import ui.elements.PanelButton;

@HasStyledElements
public class MainWindow extends DefaultWindow implements UseServices{
	private final MainActivity mainActivity = new MainActivity();
	private final TagsEditorActivity tagsEditorActivity = new TagsEditorActivity();

	@CssStyle({"panel_button_big_1"})
	private final PanelButton exitButton = new PanelButton("Exit") {
		@Override
		public void onClick(ActionEvent e) {
			dispose();
			hide();

			//System.exit(0);
		}
	};

	@CssStyle({"panel_button_big_1"})
	private final PanelButton searchImagesBtn = new PanelButton("Search images") {
		@Override
		public void onClick(ActionEvent e) {
			showActivity(mainActivity);
		}
	};

	@CssStyle({"panel_button_big_1"})
	private final PanelButton manageTagsBtn = new PanelButton("Manage tags") {
		@Override
		public void onClick(ActionEvent e) {
			showActivity(tagsEditorActivity);
		}
	};

	@CssStyle({"panel_button_big_1"})
	private final PanelButton settingsBtn = new PanelButton("Settings") {
		@Override
		public void onClick(ActionEvent e) {

		}
	};












	public void showActivity(Activity activity) {
		if (activity.getActivity() == null) return;

		getContent().getChildren().clear();
		getContent().getChildren().addAll(activity.getActivity());

		getSubheader().getChildren().clear();
		if (activity.getSubheaderElements() != null) getSubheader().getChildren().addAll(activity.getSubheaderElements());

		getFooter().getChildren().clear();
		if (activity.getFooterElements() != null) getFooter().getChildren().addAll(activity.getFooterElements());
	}

	public MainWindow(String windowName) {
		super(windowName, true, true, true);
		StyleParser.parseStyles(this);

		getHeader().getChildren().addAll(searchImagesBtn, manageTagsBtn, settingsBtn, getSeparator(), exitButton);
		showActivity(mainActivity);
	}

	private VBox getSeparator() {
		final VBox v = new VBox();
		v.getStyleClass().addAll("null_pane", "fill_all");
		return v;
	}
}
