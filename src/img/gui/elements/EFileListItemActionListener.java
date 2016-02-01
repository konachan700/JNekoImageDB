package img.gui.elements;

import java.nio.file.Path;

public interface EFileListItemActionListener {
        public void OnSelect(boolean isSelected, Path itemPath);
        public void OnOpen(Path itemPath);
}
