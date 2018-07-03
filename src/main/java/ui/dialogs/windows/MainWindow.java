package ui.dialogs.windows;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import proto.UseServices;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.activities.MainActivity;
import ui.dialogs.activities.TagsEditorActivity;
import ui.dialogs.activities.engine.EditorActivity;
import ui.dialogs.windows.engine.DefaultWindow;
import ui.elements.PanelButton;
import ui.elements.entity.GlobalConfigUiEntity;

@HasStyledElements
public class MainWindow extends DefaultWindow implements UseServices {
	private final ActivityHolder activityHolder = this.getActivityHolder();
	private final MainActivity mainActivity = new MainActivity(activityHolder);

	private final GlobalConfigUiEntity uiEntity = new GlobalConfigUiEntity();
	private final EditorActivity editorActivity = new EditorActivity(activityHolder, uiEntity, (a,b) -> {
		System.out.println("CLICK " + a.name() + " " + b.getClass().getSimpleName());
	});

	@CssStyle({"panel_button_big_1"})
	private final PanelButton exitButton = new PanelButton("Exit") {
		@Override
		public void onClick(ActionEvent e) {
			if (activitiesCount > 0) {
				activityHolder.close();
			} else {
				dispose();
				hide();
			}
		}
	};

	@CssStyle({"panel_button_big_1"})
	private final PanelButton searchImagesBtn = new PanelButton("Search images") {
		@Override
		public void onClick(ActionEvent e) {
			mainActivity.showFirst();
		}
	};

	@CssStyle({"panel_button_big_1"})
	private final PanelButton settingsBtn = new PanelButton("Settings") {
		@Override
		public void onClick(ActionEvent e) {
			editorActivity.showFirst();
		}
	};

	private int activitiesCount = 0;

	public MainWindow(String windowName) {
		super("mainWindow", windowName, true, true, true);
		StyleParser.parseStyles(this);

		getHeader().getChildren().addAll(searchImagesBtn, settingsBtn, getSeparator(), exitButton);
		activityHolder.setOnCountChangeListener(e -> {
			activitiesCount = e;
			if (e > 0) {
				exitButton.setText("Back");
			} else {
				exitButton.setText("Exit");
			}
		});
		mainActivity.showFirst();
	}

	private VBox getSeparator() {
		final VBox v = new VBox();
		v.getStyleClass().addAll("null_pane", "fill_all");
		return v;
	}
}
