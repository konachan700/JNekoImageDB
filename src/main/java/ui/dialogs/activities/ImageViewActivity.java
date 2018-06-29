package ui.dialogs.activities;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import proto.UseServices;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;
import ui.dialogs.activities.engine.ActivityPage;
import ui.elements.PanelButton;
import ui.elements.VerticalIconsPanel;

@HasStyledElements
public abstract class ImageViewActivity extends ActivityPage implements UseServices {
	@CssStyle({"window_root_pane"})
	private final HBox container = new HBox();

	//@CssStyle({"window_root_pane"})
	private final BorderPane imageContainer = new BorderPane();

	private final VerticalIconsPanel verticalIconsPanel = new VerticalIconsPanel();
	private final ImageView fileImageView = new ImageView();
	private Image currImg = null;

	private volatile double
			scale = 0.95d,
			height = 0,
			width = 0,
			last_x = 0,
			last_y = 0,
			img_h = 0,
			img_w = 0;

	@CssStyle("tags_scroll_pane")
	private final ScrollPane scrollPane = new ScrollPane();

	public abstract void PrevKey();
	public abstract void NextKey();

	public ImageViewActivity(ActivityHolder activityHolder) {
		super(activityHolder);
		StyleParser.parseStyles(this);

		this.getChildren().addAll(container);
		container.getChildren().addAll(getVerticalIconsPanel(), scrollPane);
		imageContainer.setCenter(fileImageView);
		scrollPane.setContent(imageContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setFocusTraversable(true);
		scrollPane.setOnKeyPressed((KeyEvent key) -> {
			if (key.getCode() == KeyCode.LEFT)  PrevKey();
			if (key.getCode() == KeyCode.RIGHT) NextKey();
			if (key.getCode() == KeyCode.SPACE) NextKey();
			if (key.getCode() == KeyCode.PLUS) zoomIn();
			if (key.getCode() == KeyCode.MINUS) zoomOut();
		});
		scrollPane.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			width = newValue.doubleValue();
			if ((height > 0) && (width > 0)) setImg(currImg);
		});
		scrollPane.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			height = newValue.doubleValue();
			if ((height > 0) && (width > 0)) setImg(currImg);
		});

		fileImageView.getStyleClass().addAll("image_view_file_list");
		fileImageView.setPreserveRatio(true);
		fileImageView.setSmooth(true);
		fileImageView.setCache(false);
		fileImageView.setCursor(Cursor.OPEN_HAND);
		fileImageView.setOnMouseReleased((MouseEvent t) -> {
			last_x = 0;
			last_y = 0;
		});
		fileImageView.setOnMouseClicked(e -> {

		});
		fileImageView.setOnMouseDragged((MouseEvent t) -> {
			if ((height < fileImageView.getFitHeight()) || (width < fileImageView.getFitWidth())) {
				if ((last_x > 0) && (last_y > 0)) {
					scrollPane.setHvalue(scrollPane.getHvalue() + ((last_x - t.getSceneX()) / (width / 1.2d)));
					scrollPane.setVvalue(scrollPane.getVvalue() + ((last_y - t.getSceneY()) / (height / 1.2d)));
				}

				last_x = t.getSceneX();
				last_y = t.getSceneY();
			}
		});

		getVerticalIconsPanel().add("Zoom in", GoogleMaterialDesignIcons.ZOOM_IN, name -> zoomIn());
		getVerticalIconsPanel().add("Fit to win", GoogleMaterialDesignIcons.FULLSCREEN_EXIT, name -> zoomFitToWin());
		getVerticalIconsPanel().add("Show origin", GoogleMaterialDesignIcons.FULLSCREEN, name -> zoomOrig());
		getVerticalIconsPanel().add("Zoom out", GoogleMaterialDesignIcons.ZOOM_OUT, name -> zoomOut());
		getVerticalIconsPanel().addFixedSeparator();
		getVerticalIconsPanel().add("Next", GoogleMaterialDesignIcons.ARROW_FORWARD, name -> NextKey());
		getVerticalIconsPanel().add("Prev", GoogleMaterialDesignIcons.ARROW_BACK, name -> PrevKey());
	}

	private void setImgOrig(Image im) {
		if (im == null) return;

		img_w = im.getWidth();
		img_h = im.getHeight();
		fileImageView.setImage(im);
		fileImageView.setFitHeight(img_h);
		fileImageView.setFitWidth(img_w);
		scale = img_w / width;
	}

	private void setImg(Image im) {
		if (im == null) return;

		img_w = im.getWidth();
		img_h = im.getHeight();
		if ((img_w < width) && (img_h < height)) {
			fileImageView.setFitHeight(img_h);
			fileImageView.setFitWidth(img_w);
			scale = img_w / width;
		} else {
			fileImageView.setFitHeight(height * scale);
			fileImageView.setFitWidth(width * scale);
		}
		fileImageView.setImage(im);
	}

	public final void setImage(Image im) {
		if (im == null) return;

		currImg = im;
		zoomFitToWin();
	}

	public final void zoomIn() {
		if (scale < 10d) scale = scale + 0.3d;
		setImg(currImg);
	}

	public final void zoomOut() {
		if (scale > 0.4) scale = scale - 0.3d;
		setImg(currImg);
	}

	public final void zoomOrig() {
		setImgOrig(currImg);
	}

	public final void zoomFitToWin() {
		scale = 0.95d;
		setImg(currImg);
	}

	@Override public Node[] getSubheaderElements() {
		return new Node[] { };
	}

	@Override public Node[] getFooterElements() {
		return new Node[0];
	}

	public VerticalIconsPanel getVerticalIconsPanel() {
		return verticalIconsPanel;
	}
}
