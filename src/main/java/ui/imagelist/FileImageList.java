package ui.imagelist;

import fao.ImageFile;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileImageList extends AbstractImageList {
    private final ArrayList<ImageFile> imageFiles = new ArrayList<>();
    private final Set<ImageFile> selectedImageFiles = new HashSet<>();

    private final StackPane viewPane = new StackPane();
    private final ImageView imageView = new ImageView();

    public FileImageList() {
        super();

        viewPane.getStyleClass().addAll("null_pane", "max_width", "max_height", "semi_transparent");
        imageView.getStyleClass().addAll("null_pane", "max_width", "max_height");
        viewPane.setOnMouseClicked(e -> removeBlur());
        imageView.setOnMouseClicked(e -> removeBlur());

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(false);
        imageView.fitWidthProperty().bind(viewPane.widthProperty().divide(1.25));
        imageView.fitHeightProperty().bind(viewPane.heightProperty().divide(1.25));

        viewPane.getChildren().add(imageView);
    }

    private void removeBlur() {
        getRootPane().getChildren().remove(viewPane);
        Utils.removeBlur(getRootVBox());
    }

    public void setImages(Collection<ImageFile> images) {
        imageFiles.clear();
        imageFiles.addAll(images);

        Collections.sort(imageFiles, Comparator.comparing(a -> a.getImagePath().toFile().getName()));

        final int lastPageCount = imageFiles.size() % getElementsPerPage();
        final int inFullPageCount = imageFiles.size() - lastPageCount;
        final int pagesCount1 = inFullPageCount / getElementsPerPage();
        final int pagesCount2 = (lastPageCount > 0) ? pagesCount1 : pagesCount1 - 1;

        setPagesCount(pagesCount2);
        fillImagesList(0, pagesCount2);
    }

    @Override
    public List<ImageFile> imageRequest(int page, int pages) {
        final int imagesPerPage = getElementsPerPage();
        final int from = (imagesPerPage * page);
        if (from >= imageFiles.size()) return null;
        final int to = (imagesPerPage * (page + 1));

        return imageFiles.subList(from, Math.min(to, imageFiles.size()));
    }

    @Override
    public Set<ImageFile> selectedRequest() {
        return selectedImageFiles;
    }

    @Override
    public void onSelect(ImageFile imageFile, int index, boolean selected) {
        if (Objects.isNull(imageFile)) return;
        if (selected) {
            getSelectedImageFiles().add(imageFile);
        } else {
            getSelectedImageFiles().remove(imageFile);
        }
    }

    @Override
    public void OnRightClick(ImageFile imageFile, int index) {
        if (Objects.isNull(imageFile)) return;
        if (Objects.isNull(imageFile.getImagePath())) return;

        getRootPane().getChildren().add(viewPane);
        try (InputStream is = Files.newInputStream(imageFile.getImagePath())) {
            imageView.setImage(new Image(is));
            Utils.setBlur(getRootVBox());
        } catch (IOException e) {
            getRootPane().getChildren().remove(viewPane);
        }
    }

    public Set<ImageFile> getSelectedImageFiles() {
        return selectedImageFiles;
    }

    @Override
    public void selectAll() {
        super.selectAll();
        selectedImageFiles.addAll(imageFiles);
    }

    @Override
    public void selectNone() {
        super.selectNone();
        selectedImageFiles.clear();
    }
}
