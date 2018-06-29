package ui.dialogs.windows.engine;

import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import proto.UseServices;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;

@HasStyledElements
public class DefaultWindow extends Stage implements UseServices {
	private final Image logoImage = new Image("/style/images/logo_inv.png");
	private final ImageView imgLogoNode = new ImageView(logoImage);

	@CssStyle({"window_root_pane"})
	final VBox windowContainer = new VBox();

	@CssStyle({"window_header"})
	final HBox header = new HBox();

	@CssStyle({"window_header_panel"})
	final HBox headerPanel = new HBox();

	@CssStyle({"window_subheader"})
	final HBox subheader = new HBox(6);

	@CssStyle({"window_footer"})
	final HBox footerHolder = new HBox();

	@CssStyle({"window_header_panel"})
	final HBox footer = new HBox();

	@CssStyle({"content_pane"})
	private final ActivityHolder activityHolder = new ActivityHolder(footer, subheader, headerPanel);

	@CssStyle({"window_resizer"})
	final HBox resizer = new HBox();

	private final Scene scene;

	private double xOffset = 0;
	private double yOffset = 0;

	public DefaultWindow(String windowId, String windowName, boolean hasHeader, boolean hasSubHeader, boolean hasFooter) {
		super();

		//initStyle(StageStyle.UNDECORATED);
		setResizable(false);
		windowContainer.getStylesheets().add(getClass().getResource("/style/css/main.css").toExternalForm());
		StyleParser.parseStyles(this);

		if (hasHeader) {
			windowContainer.getChildren().add(header);
			header.getChildren().addAll(imgLogoNode, headerPanel);
			if (hasSubHeader) windowContainer.getChildren().add(getSubheader());

			header.setOnMousePressed(e -> {
				xOffset = getX() - e.getScreenX();
				yOffset = getY() - e.getScreenY();
			});

			header.setOnMouseDragged(e -> {
				setX(e.getScreenX() + xOffset);
				setY(e.getScreenY() + yOffset);
			});
		}

		final Double w = getConfig().getWindowDimension(windowId).getWidth();
		final Double h = getConfig().getWindowDimension(windowId).getHeight();
		final int step = getConfig().getWindowResizeStep();

		scene = new Scene(windowContainer, w, h);
		this.setWidth(w);
		this.setHeight(h);

		windowContainer.getChildren().add(getActivityHolder());
		if (hasFooter) {
			windowContainer.getChildren().add(footerHolder);
			footerHolder.getChildren().addAll(footer, resizer);
			resizer.setCursor(Cursor.NW_RESIZE);
			resizer.setOnMouseDragged(e -> {
				int w1 = ((int)(e.getSceneX() + step)) / step * step;
				int h1 = ((int)(e.getSceneY() + step)) / step * step;
				if (((int)getWidth()) != w1) setWidth(w1);
				if (((int)getHeight()) != h1) setHeight(h1);
			});
		}

		this.getIcons().add(new Image("/style/icons/icon32.png"));
		this.getIcons().add(new Image("/style/icons/icon64.png"));
		this.getIcons().add(new Image("/style/icons/icon128.png"));
		this.setMinWidth(w);
		this.setMinHeight(h);
		this.setTitle(windowName);
		this.setScene(scene);
		this.setOnCloseRequest(Event::consume);

		heightProperty().addListener((e, o, n) -> getConfig().getWindowDimension(windowId).setHeight(n.doubleValue()));
		widthProperty().addListener((e, o, n) -> getConfig().getWindowDimension(windowId).setWidth(n.doubleValue()));
	}

	public void show(boolean modal) {
		if (modal) {
			//if (modal) this.initModality(Modality.APPLICATION_MODAL);
			this.showAndWait();
		} else {
			//if (modal) this.initModality(Modality.NONE);
			this.show();
		}
	}

	public HBox getHeader() {
		return headerPanel;
	}

	public HBox getSubheader() {
		return subheader;
	}

	public HBox getFooter() {
		return footer;
	}

	public ActivityHolder getActivityHolder() {
		return activityHolder;
	}
}
