package service.resizer;

import javafx.scene.image.Image;

import java.nio.file.Path;

public interface ImageResizeTaskCallback {
    void onImageResized(Image img, int localIndex);
    void onError(Path p, int localIndex);
}
