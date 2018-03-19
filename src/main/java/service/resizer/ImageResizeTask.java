package service.resizer;

import fao.ImageFile;

public class ImageResizeTask {
    private final ImageFile imageFile;
    private final ImageResizeTaskCallback taskCallback;

    public ImageResizeTask(ImageFile imageFile, ImageResizeTaskCallback taskCallback) {
        this.imageFile = imageFile;
        this.taskCallback = taskCallback;
    }

    public ImageResizeTaskCallback getTaskCallback() {
        return taskCallback;
    }

    public ImageFile getImageFile() {
        return imageFile;
    }
}
