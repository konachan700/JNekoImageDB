package ui.imageview.localdb;

import javafx.scene.input.MouseEvent;
import model.entity.ImageEntity;

public interface LocalDbImageViewEvents {
	void onItemClick(MouseEvent e, ImageEntity image, int pageId, int id, int pageCount);
}
