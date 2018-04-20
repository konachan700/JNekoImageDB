package ui.dialogs;

import java.util.Optional;
import java.util.UUID;

import annotation.HasStyledElements;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import annotation.CssStyle;
import proto.UseServices;
import proto.SettingsService;
import ui.StyleParser;

@HasStyledElements
public class DefaultWindow extends Stage implements UseServices {
	private final UUID windowUuid;
	private final SettingsService settingsService;

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
	final HBox content = new HBox();

	@CssStyle({"window_resizer"})
	final HBox resizer = new HBox();

	private final Scene scene;

	private double xOffset = 0;
	private double yOffset = 0;

	public DefaultWindow(String windowName, boolean hasHeader, boolean hasSubHeader, boolean hasFooter) {
		super();
		this.windowUuid = UUID.nameUUIDFromBytes(windowName.getBytes());
		this.settingsService = getService(SettingsService.class);

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

		final Double w = Optional.ofNullable(settingsService).map(e -> e.readSettingsEntry(windowUuid, "DefaultWindow_width", 800D)).orElse(800D);
		final Double h = Optional.ofNullable(settingsService).map(e -> e.readSettingsEntry(windowUuid, "DefaultWindow_height", 600D)).orElse(600D);

		scene = new Scene(windowContainer, w, h);
		this.setWidth(w);
		this.setHeight(h);

		windowContainer.getChildren().add(getContent());
		if (hasFooter) {
			windowContainer.getChildren().add(footerHolder);
			if (settingsService != null) {
				footerHolder.getChildren().addAll(footer, resizer);
				resizer.setCursor(Cursor.NW_RESIZE);
				resizer.setOnMouseDragged(e -> {
					int w1 = ((int)(e.getSceneX() + 32)) / 32 * 32;
					int h1 = ((int)(e.getSceneY() + 32)) / 32 * 32;
					if (((int)getWidth()) != w1) setWidth(w1);
					if (((int)getHeight()) != h1) setHeight(h1);
				});
			} else {
				footerHolder.getChildren().addAll(footer);
			}
		}

		if (settingsService != null) {
			heightProperty().addListener((e, o, n) -> {
				settingsService.writeSettingsEntry(windowUuid, "DefaultWindow_height", n.doubleValue());
			});
			widthProperty().addListener((e, o, n) -> {
				settingsService.writeSettingsEntry(windowUuid, "DefaultWindow_width", n.doubleValue());
			});
		}

		this.getIcons().add(new Image("/style/icons/icon32.png"));
		this.getIcons().add(new Image("/style/icons/icon64.png"));
		this.getIcons().add(new Image("/style/icons/icon128.png"));
		this.setMinWidth(w);
		this.setMinHeight(h);
		this.setTitle(windowName);
		this.setScene(scene);
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

	public HBox getContent() {
		return content;
	}
}
