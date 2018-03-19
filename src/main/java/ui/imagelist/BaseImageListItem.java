package ui.imagelist;

import fao.ImageFile;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import service.resizer.ImageResizeService;
import service.resizer.ImageResizeTask;
import service.resizer.ImageResizeTaskCallback;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class BaseImageListItem extends Canvas implements ImageResizeTaskCallback {
    private static final Image badImage = new Image("/dummy/badimage.png");
    private static final Color notSelectedColor = Color.color(0.8,0.8,0.8);
    private static final Color selectedColor = Color.color(0.3,0.8,0.3);
    private static final Color grayColor = Color.color(0.3,0.3,0.3);
    private static final javafx.scene.text.Font font = javafx.scene.text.Font.loadFont(
            BaseImageListItem.class.getResource("/style/fonts/QUILLC.TTF").toExternalForm(),56);
    private static final Image selectedIcon = IconFontFX.buildImage(GoogleMaterialDesignIcons.DONE, 64, selectedColor, selectedColor);

    private int localIndex;
    private int globalIndex;
    private Image image;
    private boolean selected = false;

    private final BaseImageListItemSelectionListener listener;
    private ImageFile imageFile = null;

    public BaseImageListItem(BaseImageListItemSelectionListener listItemSelectionListener) {
        listener = listItemSelectionListener;
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                setSelected(!isSelected());
                listener.onSelect(imageFile, getLocalIndex(), isSelected());
                drawImage();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                listener.OnRightClick(imageFile, localIndex);
            }
        });
    }

    public void drawImage() {
        if (Objects.isNull(image)) {
            setNullImage();
            return;
        }

        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
        context.drawImage(image, 0, 0);
        context.setLineWidth(isSelected() ? 9.0 : 1.5);
        context.setStroke(isSelected() ? selectedColor : notSelectedColor);
        context.strokeRect(0, 0, getWidth(), getHeight());
        if (isSelected()) context.drawImage(selectedIcon, getWidth() - 74, 10);
    }

    public void drawPageIndicator(int i) {
        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
        context.setStroke(grayColor);
        context.setFont(font);
        context.setTextAlign(TextAlignment.CENTER);
        context.setTextBaseline(VPos.CENTER);
        context.strokeText("" + i, getWidth()/2,getHeight()/2);
    }

    public void setNullImage() {
        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void onImageResized(Image img, int globalIndex) {
        if (this.globalIndex != globalIndex) return;

        image = img;
        drawImage();
    }

    @Override
    public void onError(Path p, int globalIndex) {
        if (this.globalIndex != globalIndex) return;

        final GraphicsContext context = getGraphicsContext2D();
        double x = (getWidth() - badImage.getWidth()) / 2;
        double y = (getHeight() - badImage.getHeight()) / 2;
        context.clearRect(0, 0, getWidth(), getHeight());
        context.drawImage(badImage, (x <= 0) ? 0 : x, (y <= 0) ? 0 : y);
    }

    public int getLocalIndex() {
        return localIndex;
    }

    public void setLocalIndex(int localIndex) {
        this.localIndex = localIndex;
    }

    public void notifyResized(ImageResizeService svc, List<ImageFile> imageFiles) {
        if (Objects.isNull(imageFiles)) return;
        if ((getWidth() < 16) || (getHeight() < 16)) return;
        if ((localIndex >= imageFiles.size()) || (localIndex < 0)) return;

        imageFiles.get(localIndex).setLocalIndex(globalIndex);
        imageFiles.get(localIndex).setPreviewSize(getWidth(), getHeight());
        final ImageResizeTask imageResizeTask = new ImageResizeTask(imageFiles.get(localIndex), this);
        svc.loadImage(imageResizeTask);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (Objects.isNull(image)) return;
        this.selected = selected;
        drawImage();
    }

    public ImageFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(ImageFile imageFile) {
        this.imageFile = imageFile;
    }

    public int getGlobalIndex() {
        return globalIndex;
    }

    public void setGlobalIndex(int globalIndex) {
        this.globalIndex = globalIndex;
    }
}
