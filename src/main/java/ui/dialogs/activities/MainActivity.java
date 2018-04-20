package ui.dialogs.activities;

import annotation.CssStyle;
import annotation.HasStyledElements;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import ui.StyleParser;
import ui.dialogs.ImagesImportDialog;
import ui.elements.Paginator;
import ui.elements.PanelButton;

@HasStyledElements
public class MainActivity extends HBox implements Activity {
	private final ImagesImportDialog imagesImportDialog = new ImagesImportDialog();

	private final Paginator paginator = new Paginator() {
		@Override public void onPageChange(int currentPage, int pageCount) {

		}
	};

	@CssStyle({"panel_button_subheader_1"})
	private final PanelButton importImages = new PanelButton("Import images...") {
		@Override
		public void onClick(ActionEvent e) {
			imagesImportDialog.show(true);
		}
	};




	public MainActivity() {
		StyleParser.parseStyles(this);

	}

	@Override public Node[] getSubheaderElements() {
		return new Node[] { importImages, getSeparator(), paginator };
	}

	@Override public Node[] getFooterElements() {
		return null;
	}

	@Override public Node getActivity() {
		return this;
	}
}
