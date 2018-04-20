package ui.imagelist;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import annotation.CssStyle;
import annotation.HasStyledElements;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ui.StyleParser;
import ui.imageview.SelectedFilesListItem;

@HasStyledElements
public class SelectedFilesList extends VBox {
	@CssStyle({"tags_scroll_pane"})
	private final ScrollPane scrollPane = new ScrollPane();

	@CssStyle({"tags_null_pane"})
	private final VBox vBox = new VBox();

	private final List<Path> list = new ArrayList<>();

	public SelectedFilesList() {
		StyleParser.parseStyles(this);

		scrollPane.setContent(vBox);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

		super.getChildren().addAll(scrollPane);
	}

	public void refresh(Collection<Path> items) {
		final AtomicInteger counter = new AtomicInteger(0);
		vBox.getChildren().clear();

		list.clear();
		list.addAll(items);
		list.sort(Comparator.comparing(a -> a.toFile().getName()));
		list.forEach(e -> {
			final SelectedFilesListItem f = new SelectedFilesListItem() {
				@Override public Path requestFileItem(int index) {
					return list.get(Math.min(index, list.size() - 1));
				}
			};
			f.setImage(counter.getAndIncrement(), 0);
			vBox.getChildren().add(f);
		});
	}
}
