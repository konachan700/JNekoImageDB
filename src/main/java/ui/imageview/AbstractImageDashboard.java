package ui.imageview;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public abstract class AbstractImageDashboard<T extends Canvas> extends VBox {
    private int currentPage;
    private int pagesCount;
    private int elementsPerPage;
    private boolean sizeChanged = false;

    private final StackPane rootPane = new StackPane();
    private final VBox rootVBox = new VBox();

	private final Dimension oldDim = new Dimension(0, 0);

	private static final ArrayList<Timer> timers = new ArrayList<>();
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
                    double w = getWidth();
                    double h = getHeight();
                    if (oldDim.getWidth() <= 0 || oldDim.getHeight() <= 0) {
                    	if (w > 0 && h > 0) {
							oldDim.setSize(w, h);
						}
					} else {
                    	if (oldDim.getWidth() != w || oldDim.getHeight() != h) {
                    		//System.out.println("RESIZED");
							Platform.runLater(() -> onResizeCompleted());
							oldDim.setSize(w, h);
						}
					}
                }
            }
        }
    };

    protected abstract void onResizeCompleted();
    protected abstract T imageViewRequest(int number);
    protected abstract void pageChanged(int page);

    protected AbstractImageDashboard() {
        timers.add(timer);

        getRootVBox().widthProperty().addListener((e, o, n) -> resizeProc(n));
        getRootVBox().heightProperty().addListener((e, o, n) -> resizeProc(n));
        getRootVBox().setOnScroll(sa -> scrollProc(sa));

        rootPane.getStyleClass().addAll("imglist_rootbox");
        rootVBox.getStyleClass().addAll("imglist_rootbox2");

        getRootPane().getChildren().addAll(getRootVBox());
        this.getChildren().add(getRootPane());
        timer.schedule(timerTask, 100, 25);
    }

    public void generateView(int previewsInRow, int previewsInColumn) {
        elementsPerPage = previewsInColumn * previewsInRow;

        getRootVBox().getChildren().clear();

        int counter = 0;
        for (int y=0; y<previewsInColumn; y++) {
            final HBox row = new HBox();
            row.getStyleClass().addAll("null_pane");
            row.maxWidthProperty().bind(getRootVBox().widthProperty());
            row.minWidthProperty().bind(getRootVBox().widthProperty());
            row.prefWidthProperty().bind(getRootVBox().widthProperty());
            row.maxHeightProperty().bind(getRootVBox().heightProperty().divide(previewsInColumn));
            row.minHeightProperty().bind(getRootVBox().heightProperty().divide(previewsInColumn));
            row.prefHeightProperty().bind(getRootVBox().heightProperty().divide(previewsInColumn));
            getRootVBox().getChildren().add(row);

            for (int x=0; x<previewsInRow; x++) {
                final T imageView = imageViewRequest(counter++);
                imageView.widthProperty().bind(row.widthProperty().divide(previewsInRow));
                imageView.heightProperty().bind(row.heightProperty());
                row.getChildren().add(imageView);
            }
        }
    }

    private void scrollProc(ScrollEvent sa) {
        final double yScroll = sa.getDeltaY() / sa.getMultiplierX();
        if (yScroll < 0) {
            if (getCurrentPage() < getPagesCount()) {
                currentPage++;
                pageChanged(currentPage);
            }
        } else {
            if (getCurrentPage() > 0) {
                currentPage--;
                pageChanged(currentPage);
            }
        }
    }

    private void resizeProc(Number n) {
        if (n.doubleValue() <= 128) return;
        timerCounter = 0;
        sizeChanged = true;
    }

    public StackPane getRootPane() {
        return rootPane;
    }

    public VBox getRootVBox() {
        return rootVBox;
    }

    public int getCurrentPage() {
        return currentPage;
    }

	public void resetCurrentPage() {
		currentPage = 0;
		pageChanged(0);
	}

    public int getPagesCount() {
        return pagesCount;
    }

    public int getElementsPerPage() {
        return elementsPerPage;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
        //this.currentPage = 0;
    }

    public static void disposeStatic() {
        timers.forEach(Timer::cancel);
    }

    public void setBlur(Pane p) {
        final ColorAdjust adj = new ColorAdjust(0, -0.9, 0.5, 0);
        final GaussianBlur blur = new GaussianBlur(17);
        adj.setInput(blur);
        p.setEffect(adj);
    }

    public void removeBlur(Pane p) {
        p.setEffect(null);
    }
}
