package ui.imagelist;

import fao.ImageFile;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import service.resizer.ImageResizeService;
import ui.paginator.Paginator;
import ui.paginator.PaginatorActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractImageList extends VBox implements PaginatorActionListener, BaseImageListItemSelectionListener {
    private static ImageResizeService imageResizeService = null;

    private final ArrayList<BaseImageListItem> images = new ArrayList<>();
    private final ArrayList<ImageFile> imageFiles = new ArrayList<>();

    private int currentPage;
    private int pagesCount;
    private int elementsPerPage;
    private boolean sizeChanged = false;
    private int previewsInRow;
    private int previewsInColoumn;

    private final Paginator paginator = new Paginator(this, Paginator.PAGINATOR_TYPE_FULL);
    private final StackPane rootPane = new StackPane();
    private final VBox rootVBox = new VBox();

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

    public abstract List<ImageFile> imageRequest(int page, int pages);
    public abstract Set<ImageFile> selectedRequest();

    public AbstractImageList() {
        if (Objects.isNull(imageResizeService)) imageResizeService = new ImageResizeService();

        getStyleClass().addAll("null_pane", "max_width", "max_height", "dark_color");
        getRootVBox().getStyleClass().addAll("null_pane", "max_width", "max_height");
        getPaginator().getStyleClass().addAll("null_pane", "width_370px", "max_height");
        getRootPane().getStyleClass().addAll("null_pane", "max_width", "max_height");

        getRootVBox().widthProperty().addListener((e, o, n) -> resizeProc(n));
        getRootVBox().heightProperty().addListener((e, o, n) -> resizeProc(n));
        getRootVBox().setOnScroll(sa -> scrollProc(sa));

        rootPane.getChildren().addAll(rootVBox);
        this.getChildren().add(rootPane);
        timer.schedule(timerTask, 100, 25);
    }

    public void generateView(int previewsInRow, int previewsInColoumn) {
        this.previewsInRow = previewsInRow;
        this.previewsInColoumn = previewsInColoumn;
        elementsPerPage = previewsInColoumn * previewsInRow;

        getRootVBox().getChildren().clear();
        images.clear();

        int counter = 0;
        for (int y=0; y<previewsInColoumn; y++) {
            final HBox row = new HBox();
            row.getStyleClass().addAll("null_pane");
            row.maxWidthProperty().bind(getRootVBox().widthProperty());
            row.minWidthProperty().bind(getRootVBox().widthProperty());
            row.prefWidthProperty().bind(getRootVBox().widthProperty());
            row.maxHeightProperty().bind(getRootVBox().heightProperty().divide(previewsInColoumn));
            row.minHeightProperty().bind(getRootVBox().heightProperty().divide(previewsInColoumn));
            row.prefHeightProperty().bind(getRootVBox().heightProperty().divide(previewsInColoumn));
            getRootVBox().getChildren().add(row);

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

    private void repaint() {
        images.forEach(img -> img.setNullImage());
        images.forEach(img -> img.notifyResized(imageResizeService, imageFiles));
    }

    public void fillImagesList(int page, int pages) {
        imageFiles.clear();

        final List<ImageFile> imagesReq = imageRequest(page, pages);
        final Set<ImageFile> selectedReq = selectedRequest();

        if (Objects.nonNull(imagesReq) && Objects.nonNull(selectedReq)) {
            imageFiles.addAll(imagesReq);
            for (int i=0; i<images.size(); i++) {
                images.get(i).setLocalIndex(i);
                images.get(i).setGlobalIndex((page * elementsPerPage) + i);
                images.get(i).setImageFile(imageFiles.get(Math.min(i, imageFiles.size()-1)));
                images.get(i).setSelected(selectedReq.contains(images.get(i).getImageFile()));
            }
            repaint();
        }
    }

    private void resizeProc(Number n) {
        if (n.doubleValue() <= 128) return;
        timerCounter = 0;
        sizeChanged = true;
        images.forEach(img -> img.setNullImage());
    }

    private void scrollProc(ScrollEvent sa) {
        final double yScroll = sa.getDeltaY() / sa.getMultiplierX();
        if (yScroll < 0) {
            if (getCurrentPage() < getPagesCount()) {
                currentPage++;
                fillImagesList(getCurrentPage(), getPagesCount());
                getPaginator().setCurrentPageIndex(getCurrentPage());
            }
        } else {
            if (getCurrentPage() > 0) {
                currentPage--;
                fillImagesList(getCurrentPage(), getPagesCount());
                getPaginator().setCurrentPageIndex(getCurrentPage());
            }
        }
    }

    @Override
    public void OnPageChange(int page, int pages) {
        currentPage = page;
        fillImagesList(page, pages);
    }

    public void selectAll() {
        images.forEach(img -> img.setSelected(true));
        repaint();
    }

    public void selectNone() {
        images.forEach(img -> img.setSelected(false));
        repaint();
    }

    public static void disposeStatic() {
        if (Objects.nonNull(imageResizeService)) imageResizeService.dispose();
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

    public int getPreviewsInRow() {
        return previewsInRow;
    }

    public int getPreviewsInColoumn() {
        return previewsInColoumn;
    }

    public void setPagesCount(int pagesCount) {
        getPaginator().setCurrentPageIndex(0);
        getPaginator().setPageCount(pagesCount);
        this.pagesCount = pagesCount;
        this.currentPage = 0;
        images.forEach(img -> img.setNullImage());
    }

    public VBox getRootVBox() {
        return rootVBox;
    }

    public StackPane getRootPane() {
        return rootPane;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void dispose() {
        timer.cancel();
    }
}
