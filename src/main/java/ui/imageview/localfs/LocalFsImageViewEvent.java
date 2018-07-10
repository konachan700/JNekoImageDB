package ui.imageview.localfs;

import java.nio.file.Path;

import javafx.scene.input.MouseEvent;

public interface LocalFsImageViewEvent {
	void onClick(MouseEvent e, Path imageFile, int pageId, int id, int pageCount);
	void onSelect(boolean selected, MouseEvent e, Path imageFile, int pageId, int id, int pageCount);
}
