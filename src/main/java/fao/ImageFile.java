package fao;

import dao.ImageId;
import service.resizer.ImageResizeTaskType;

import java.nio.file.Path;

public class ImageFile {
    private final ImageFileDimension imageFileDimension = new ImageFileDimension();
    private Path imagePath;
    private int localIndex;
    private ImageResizeTaskType type;
    private ImageId imageDatabaseId;

    public ImageFile(Path imagePath) {
        this.imagePath = imagePath;
        type = ImageResizeTaskType.LOCAL_FS;
    }

    public ImageFile(ImageId imageDatabaseId) {
        this.imageDatabaseId = imageDatabaseId;
        type = ImageResizeTaskType.INTERNAL_DATABASE;
    }

    public void setAllSizes(double realWidth, double realHeight, double previewWidth, double previewHeight) {
        imageFileDimension.setRealWidth(realWidth);
        imageFileDimension.setRealHeaight(realHeight);
        imageFileDimension.setPreviewWidth(previewWidth);
        imageFileDimension.setPreviewHeight(previewHeight);
    }

    public void setRealSize(double realWidth, double realHeight) {
        imageFileDimension.setRealWidth(realWidth);
        imageFileDimension.setRealHeaight(realHeight);
    }

    public ImageFileDimension getImageFileDimension() {
        return imageFileDimension;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public void setImagePath(Path path) {
        imagePath = path;
    }

    public void setPreviewSize(double previewWidth, double previewHeight) {
        imageFileDimension.setPreviewWidth(previewWidth);
        imageFileDimension.setPreviewHeight(previewHeight);
    }

    public int getLocalIndex() {
        return localIndex;
    }

    public void setLocalIndex(int localIndex) {
        this.localIndex = localIndex;
    }

    public ImageResizeTaskType getType() {
        return type;
    }

    public void setType(ImageResizeTaskType type) {
        this.type = type;
    }

    public ImageId getImageDatabaseId() {
        return imageDatabaseId;
    }

    public void setImageDatabaseId(ImageId imageDatabaseId) {
        this.imageDatabaseId = imageDatabaseId;
    }
}
