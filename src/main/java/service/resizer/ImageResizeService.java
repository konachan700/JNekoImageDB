package service.resizer;

import dao.ImageId;
import fao.ImageFile;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import service.RootService;
import utils.ImageUtils;
import utils.Loggable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

public class ImageResizeService implements Loggable {
    private final ExecutorService executor;
    private final LinkedBlockingDeque<ImageResizeTask> queue = new LinkedBlockingDeque<>();

    private final Runnable thread = () -> {
        while(true) {
            try {
                final ImageResizeTask task = queue.pollLast(9999, TimeUnit.DAYS);
                final Image img = Optional.ofNullable(RootService.getCacheService().readCacheElement(task.getImageFile()))
                        .map(b -> new Image(new ByteArrayInputStream(b)))
                        .orElse(null);
                if (Objects.nonNull(img)) {
                    Platform.runLater(() -> task.getTaskCallback().onImageResized(img, task.getImageFile().getLocalIndex()));
                } else {
                    switch (task.getImageFile().getType()) {
                        case LOCAL_FS:
                            processLocalFsImage(task);
                            break;
                        case INTERNAL_DATABASE:
                            processInternalDBImage(task);
                            L("processInternalDBImage(task)");
                            break;
                        case HTTP:

                            break;
                    }
                }
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getClass().getSimpleName() + " || " + e.getMessage());
            }
        }
    };

    public ImageResizeService() {
        int cores = Runtime.getRuntime().availableProcessors();
        if (cores <= 0) throw new IllegalStateException("cannot get CPUs count");

        executor = Executors.newFixedThreadPool(cores);
        for (int i=0; i<cores; i++) executor.submit(thread);
    }

    public void dispose() {
        executor.shutdownNow();
    }

    public void loadImages(CopyOnWriteArrayList<ImageResizeTask> images) {
        images.forEach(e -> {
            try {
                if (!queue.contains(e)) queue.putLast(e);
            } catch (InterruptedException e1) { }
        });
    }

    public void loadImage(ImageResizeTask image) {
        try {
            if (!queue.contains(image)) queue.putLast(image);
        } catch (InterruptedException e1) { }
    }

    public boolean isFree() {
        return queue.isEmpty();
    }

    private void processInternalDBImage(ImageResizeTask task) throws IOException {
        final ImageId imageId = task.getImageFile().getImageDatabaseId();
        if (Objects.isNull(imageId)) {
            Platform.runLater(() -> task.getTaskCallback().onError(null, task.getImageFile().getLocalIndex()));
            return;
        }

        final byte[] file = RootService.getFileService().readDBFile(imageId.getImgId());
        final BufferedImage bufferedImageOriginal = ImageIO.read(new ByteArrayInputStream(file));
        if (Objects.isNull(bufferedImageOriginal)){
            Platform.runLater(() -> task.getTaskCallback().onError(null, task.getImageFile().getLocalIndex()));
            return;
        }

        final BufferedImage bi = ImageUtils.resizeImage(bufferedImageOriginal,
                Math.round(task.getImageFile().getImageFileDimension().getPreviewWidth()),
                Math.round(task.getImageFile().getImageFileDimension().getPreviewHeight()), true);

        final Image image = SwingFXUtils.toFXImage(bi, null);
        Platform.runLater(() -> task.getTaskCallback().onImageResized(image, task.getImageFile().getLocalIndex()));

        writePreviewToCache(bi, task.getImageFile());
    }

    private void processLocalFsImage(ImageResizeTask task) throws IOException {
        final ImageFile imageFile = task.getImageFile();
        final BufferedImage bi = Optional.ofNullable(imageFile.getImagePath().toAbsolutePath().toFile())
                .map(file -> ImageUtils.resizeImage(file,
                        Math.round(imageFile.getImageFileDimension().getPreviewWidth()),
                        Math.round(imageFile.getImageFileDimension().getPreviewHeight()), true)).orElse(null);
        if (Objects.isNull(bi)) {
            Platform.runLater(() -> task.getTaskCallback().onError(imageFile.getImagePath().toAbsolutePath(), imageFile.getLocalIndex()));
            return;
        }

        final Image image = SwingFXUtils.toFXImage(bi, null);
        Platform.runLater(() -> task.getTaskCallback().onImageResized(image, task.getImageFile().getLocalIndex()));

        writePreviewToCache(bi, task.getImageFile());
    }

    private void writePreviewToCache(BufferedImage bi, ImageFile imageFile) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", baos);
        final byte retVal[] = baos.toByteArray();
        RootService.getCacheService().writeCacheElement(imageFile, retVal);
    }
}
