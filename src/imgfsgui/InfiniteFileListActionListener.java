package imgfsgui;

import java.nio.file.Path;
import javafx.scene.image.Image;

public interface InfiniteFileListActionListener {
    public Image OnImageNedded(Path p);
    public void OnLeftClick(Path itemPath);
}
