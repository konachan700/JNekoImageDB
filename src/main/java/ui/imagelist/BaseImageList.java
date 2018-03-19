package ui.imagelist;

import fao.ImageFile;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import service.resizer.ImageResizeService;
import service.resizer.ImageResizeTaskType;
import ui.paginator.Paginator;
import ui.paginator.PaginatorActionListener;
import utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

@Deprecated
public class BaseImageList extends VBox implements PaginatorActionListener, BaseImageListItemSelectionListener {
    private final Paginator paginator = new Paginator(this, Paginator.PAGINATOR_TYPE_FULL);
    private final ImageResizeTaskType contentType;

    private final ImageResizeService imageResizeService = new ImageResizeService();

    private final ArrayList<BaseImageListItem> images = new ArrayList<>();
    private final ArrayList<ImageFile> imageFiles = new ArrayList<>();
    private final Set<ImageFile> selectedImageFiles = new HashSet<>();

    private final StackPane rootPane = new StackPane();
    private final VBox rootVBox = new VBox();

    private final StackPane viewPane = new StackPane();
    private final ImageView imageView = new ImageView();

    private int currentPage;
    private int pagesCount;
    private int elementsPerPage;
    private boolean sizeChanged = false;
    private int previewsInRow;
    private int previewsInColoumn;

    private int timerCounter = 0;
    private final Timer timer = new Timer();
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            timerCounter++;
            if (timerCounter >= 10) {
                timerCounter = 0;
                if (sizeChanged) {
                    sizeChanged = false;
                    images.forEach(img -> img.notifyResized(imageResizeService, imageFiles));
                }
            }
        }
    };

    public BaseImageList(ImageResizeTaskType contentType) {
        this.contentType = contentType;

        getStyleClass().addAll("null_pane", "max_width", "max_height");
        rootVBox.getStyleClass().addAll("null_pane", "max_width", "max_height");
        //paginator.getStyleClass().addAll("null_pane", "max_width", "max_height");
        rootPane.getStyleClass().addAll("null_pane", "max_width", "max_height");
        viewPane.getStyleClass().addAll("null_pane", "max_width", "max_height", "semi_transparent");
        imageView.getStyleClass().addAll("null_pane", "max_width", "max_height");
        viewPane.setOnMouseClicked(e -> {
            rootPane.getChildren().remove(viewPane);
            Utils.removeBlur(rootVBox);
        });
        imageView.setOnMouseClicked(e -> {
            rootPane.getChildren().remove(viewPane);
            Utils.removeBlur(rootVBox);
        });

        rootVBox.widthProperty().addListener((e, o, n) -> {
            if (n.doubleValue() <= 128) {
                //rootVBox.setWidth(128);
                return;
            }
            timerCounter = 0;
            sizeChanged = true;
            images.forEach(img -> img.setNullImage());
        });
        rootVBox.heightProperty().addListener((e, o, n) -> {
            if (n.doubleValue() <= 128) {
                //rootVBox.setHeight(128);
                return;
            }
            timerCounter = 0;
            sizeChanged = true;
            images.forEach(img -> img.setNullImage());
        });
        rootVBox.setOnScroll(sa -> {
            final double yScroll = sa.getDeltaY() / sa.getMultiplierX();
            if (yScroll < 0) {
                if (currentPage < pagesCount) {
                    currentPage++;
                    paginator.setCurrentPageIndex(currentPage);
                    repaint();
                }
            } else {
                if (currentPage > 0) {
                    currentPage--;
                    paginator.setCurrentPageIndex(currentPage);
                    repaint();
                }
            }
        });

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(false);
        imageView.fitWidthProperty().bind(viewPane.widthProperty().divide(1.25));
        imageView.fitHeightProperty().bind(viewPane.heightProperty().divide(1.25));

        viewPane.getChildren().add(imageView);
        rootPane.getChildren().addAll(rootVBox);

        this.getChildren().add(rootPane);
        timer.schedule(timerTask, 100, 25);
    }

    private void repaint() {
        displayPage(currentPage);
        images.forEach(img -> img.setNullImage());
        images.get(elementsPerPage - 1).drawPageIndicator(currentPage);
        images.forEach(img -> {
            img.notifyResized(imageResizeService, imageFiles);
        });
    }

    public void generateView(int previewsInRow, int previewsInColoumn) {
        this.previewsInRow = previewsInRow;
        this.previewsInColoumn = previewsInColoumn;
        elementsPerPage = previewsInColoumn * previewsInRow;

        rootVBox.getChildren().clear();
        images.clear();

        int counter = 0;
        for (int y=0; y<previewsInColoumn; y++) {
            final HBox row = new HBox();
            row.getStyleClass().addAll("null_pane");
            row.maxWidthProperty().bind(rootVBox.widthProperty());
            row.minWidthProperty().bind(rootVBox.widthProperty());
            row.prefWidthProperty().bind(rootVBox.widthProperty());
            row.maxHeightProperty().bind(rootVBox.heightProperty().divide(previewsInColoumn));
            row.minHeightProperty().bind(rootVBox.heightProperty().divide(previewsInColoumn));
            row.prefHeightProperty().bind(rootVBox.heightProperty().divide(previewsInColoumn));
            rootVBox.getChildren().add(row);

            for (int x=0; x<previewsInRow; x++) {
                final BaseImageListItem canvas = new BaseImageListItem(this);
                canvas.widthProperty().bind(row.widthProperty().divide(previewsInRow));
                canvas.heightProperty().bind(row.heightProperty());
                canvas.setLocalIndex(counter++);
                row.getChildren().add(canvas);
                images.add(canvas);
            }
        }
    }

    public void setImages(Collection<ImageFile> images) {
        if (contentType != ImageResizeTaskType.LOCAL_FS) return;

        imageFiles.clear();
        imageFiles.addAll(images);

        Collections.sort(imageFiles, Comparator.comparing(a -> a.getImagePath().toFile().getName()));

        currentPage = 0;
        pagesCount = imageFiles.size() / (getElementsPerPage() + 1) + 1;
        paginator.setPageCount(pagesCount);
        paginator.setCurrentPageIndex(0);

        displayPage(0);
        repaint();
    }

    private void displayPage(int page) {
        final int from = page * getElementsPerPage();
        for (int i=0; i<images.size(); i++) {
            images.get(i).setLocalIndex(from + i);
            if (imageFiles.size() > (from + i)) {
                final ImageFile imageFile = imageFiles.get(from + i);
                images.get(i).setImageFile(imageFile);
                images.get(i).setSelected(getSelectedImageFiles().contains(imageFile));
            } else {
                images.get(i).setImageFile(null);
                images.get(i).setSelected(false);
            }
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public int getElementsPerPage() {
        return elementsPerPage;
    }

    public void dispose() {
        timer.cancel();
        imageResizeService.dispose();
    }

    public Paginator getPaginator() {
        return paginator;
    }

    @Override
    public void OnPageChange(int page, int pages) {
        currentPage = page;
        repaint();
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

        rootPane.getChildren().add(viewPane);
        try (InputStream is = Files.newInputStream(imageFile.getImagePath())) {
            imageView.setImage(new Image(is));
            Utils.setBlur(rootVBox);
        } catch (IOException e) {
            rootPane.getChildren().remove(viewPane);
        }
    }

    public void selectAll() {
        getSelectedImageFiles().addAll(imageFiles);
        images.forEach(img -> img.setSelected(true));
        repaint();
    }

    public void selectNone() {
        getSelectedImageFiles().clear();
        images.forEach(img -> img.setSelected(false));
        repaint();
    }

    public Set<ImageFile> getSelectedImageFiles() {
        return selectedImageFiles;
    }
}
