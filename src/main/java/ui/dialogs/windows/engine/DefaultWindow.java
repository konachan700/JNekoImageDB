package ui.dialogs.windows.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.GlobalConfig;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;

@HasStyledElements
public abstract class DefaultWindow extends Stage {
	private final Image logoImage = new Image("/style/images/logo_inv.png");
	private final ImageView imgLogoNode = new ImageView(logoImage);

	@CssStyle({"content_win_root_pane"})
	final VBox windowContainer = new VBox();

	@CssStyle({"popup_root_pane"})
	final VBox notifyContainer = new VBox(8);

	@CssStyle({"window_root_pane"})
	final AnchorPane rootWindowPane = new AnchorPane(windowContainer, notifyContainer);

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

	@CssStyle({"window_resizer"})
	final HBox resizer = new HBox();

	private Scene scene = null;

	private double xOffset = 0;
	private double yOffset = 0;

	public abstract GlobalConfig getConfig();
	public abstract ActivityHolder getActivityHolder();

	private static final ArrayList<Timer> timers = new ArrayList<>();
	private final Timer timer = new Timer();
	private final TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			final Long liveTime = getConfig().getPopupLifeTime();
			final Long time = System.currentTimeMillis();
			final Set<Node> nodes = new HashSet<>();
			Platform.runLater(() -> {
				notifyContainer.getChildren().forEach(node -> {
					if (node instanceof Popup) {
						if ((((Popup) node).getCreatedTime() + liveTime) < time) {
							nodes.add(node);
						}
					}
				});
				notifyContainer.getChildren().removeAll(nodes);
			});
		}
	};

	public void initDialogFixedWindow(String windowName, boolean hasHeader, boolean hasSubHeader, boolean hasFooter) {
		getActivityHolder().setDefaultWindow(this);

		setResizable(false);
		rootWindowPane.getStylesheets().add(getClass().getResource("/style/css/main.css").toExternalForm());
		StyleParser.parseStyles(this);

		if (hasHeader) {
			windowContainer.getChildren().add(header);
			header.getChildren().addAll(imgLogoNode, headerPanel);
			if (hasSubHeader) windowContainer.getChildren().add(getSubheader());
		}

		AnchorPane.setLeftAnchor(windowContainer, 0D);
		AnchorPane.setTopAnchor(windowContainer, 0D);
		AnchorPane.setBottomAnchor(windowContainer, 0D);
		AnchorPane.setRightAnchor(windowContainer, 0D);

		AnchorPane.setRightAnchor(notifyContainer, 16D);
		AnchorPane.setBottomAnchor(notifyContainer, 16D);

		final Double w = 640D;
		final Double h = 480D;

		scene = new Scene(rootWindowPane, w, h);
		this.setWidth(w);
		this.setHeight(h);
		windowContainer.getChildren().add(getActivityHolder());

		if (hasFooter) {
			windowContainer.getChildren().add(footerHolder);
			footerHolder.getChildren().addAll(footer);
		}

		this.getIcons().add(new Image("/style/icons/icon32.png"));
		this.getIcons().add(new Image("/style/icons/icon64.png"));
		this.getIcons().add(new Image("/style/icons/icon128.png"));
		this.setMinWidth(w);
		this.setMinHeight(h);
		this.setTitle(windowName);
		this.setScene(scene);
		this.setOnCloseRequest(Event::consume);

		timers.add(timer);
	}

	public void initResizibleWindow(String windowId, String windowName, boolean hasHeader, boolean hasSubHeader, boolean hasFooter) {
		getActivityHolder().setDefaultWindow(this);

		//initStyle(StageStyle.UNDECORATED);
		setResizable(false);
		rootWindowPane.getStylesheets().add(getClass().getResource("/style/css/main.css").toExternalForm());
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

		AnchorPane.setLeftAnchor(windowContainer, 0D);
		AnchorPane.setTopAnchor(windowContainer, 0D);
		AnchorPane.setBottomAnchor(windowContainer, 0D);
		AnchorPane.setRightAnchor(windowContainer, 0D);

		AnchorPane.setRightAnchor(notifyContainer, 16D);
		AnchorPane.setBottomAnchor(notifyContainer, 16D);

		final Double w = getConfig().getWindowDimension(windowId).getWidth();
		final Double h = getConfig().getWindowDimension(windowId).getHeight();
		final int step = getConfig().getWindowResizeStep();

		scene = new Scene(rootWindowPane, w, h);
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

		timers.add(timer);
		timer.schedule(timerTask, 100, 1000);
	}

	public void popup(String title, String text) {
		final Popup popup = new Popup(title, text, (e, p) -> notifyContainer.getChildren().remove(p));
		notifyContainer.getChildren().add(popup);
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

	public static void disposeStatic() {
		timers.forEach(Timer::cancel);
	}
}
