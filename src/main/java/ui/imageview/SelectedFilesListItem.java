package ui.imageview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import annotation.CssStyle;
import annotation.HasStyledElements;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.StyleParser;

@HasStyledElements
public abstract class SelectedFilesListItem extends HBox {
	@CssStyle({"image_list_item_titlebox"})
	private final VBox titleContainer = new VBox();

	@CssStyle({"content_pane"})
	private final VBox fillerBox = new VBox();

	@CssStyle({"image_list_item_lv"})
	private final FileImageView imageView = new FileImageView() {
		@Override public void onClick(MouseEvent event, int index, BaseImageView object) {}

		@Override public byte[] requestItem(int index) {
			try {
				final Path p = requestFileItem(index);
				if (p == null) return null;

				Platform.runLater(() -> {
					title.setText(p.toFile().getName());
					fileSize.setText(p.toFile().length() + " bytes");
				});

				return Files.readAllBytes(p);
			} catch (IOException e) {
				System.out.println(e.getClass().getSimpleName() + " / " + e.getMessage());
				return null;
			}
		}
	};

	@CssStyle({"image_list_item_title"})
	private final Label title = new Label();

	@CssStyle({"image_list_item_size"})
	private final Label fileSize = new Label();

	public abstract Path requestFileItem(int index);

	public SelectedFilesListItem() {
		StyleParser.parseStyles(this);
		this.getStyleClass().addAll("image_list_item");

		titleContainer.setAlignment(Pos.TOP_LEFT);
		titleContainer.getChildren().addAll(title, fileSize, fillerBox);

		imageView.setWidth(96);
		imageView.setHeight(96);

		getChildren().addAll(imageView, titleContainer);
	}

	public void setImage(int id, int pageId) {
		imageView.setImage(id, pageId);
	}
}
