package ui.dialogs.windows;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import model.GlobalConfig;
import services.api.UtilService;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.MainActivity;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.windows.engine.DefaultWindow;
import ui.elements.PanelButton;

@Component
@HasStyledElements
public class MainWindow extends DefaultWindow {
	@Autowired
	ActivityHolder activityHolder;

	@Autowired
	MainActivity mainActivity;

	@Autowired
	UtilService utilService;

	@CssStyle({"panel_button_big_1"})
	private final PanelButton exitButton = new PanelButton("Exit") {
		@Override
		public void onClick(ActionEvent e) {
			if (activitiesCount > 0) {
				getActivityHolder().close();
			} else {
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

	private int activitiesCount = 0;

	private VBox getSeparator() {
		final VBox v = new VBox();
		v.getStyleClass().addAll("null_pane", "fill_all");
		return v;
	}

	@PostConstruct
	void init() {
		initResizibleWindow("mainWindow", "Main window", true, true, true);

		StyleParser.parseStyles(this);
		mainActivity.setActivityHolder(activityHolder);
		getHeader().getChildren().addAll(searchImagesBtn, getSeparator(), exitButton);
		getActivityHolder().setOnCountChangeListener(e -> {
			activitiesCount = e;
			if (e > 0) {
				exitButton.setText("Back");
			} else {
				exitButton.setText("Exit");
			}
		});
		mainActivity.showFirst();
	}

	@Override
	public GlobalConfig getConfig() {
		return utilService.getConfig();
	}

	@Override public ActivityHolder getActivityHolder() {
		return activityHolder;
	}
}
