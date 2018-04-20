package ui.imageview;

import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;
import org.imgscalr.Scalr;

import proto.UseServices;
import worker.QueuedWorker;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class BaseImageView extends Canvas implements UseServices {
    /** TODO
     *  1. Find & fix a deadlock in UI thread
     *  2. Find workaround for bug with undecorated window (canvas was paint incorrect sometimes, if window undecorated)
     * **/
    private static class Task {
        public BaseImageView view;
        public int id;
        public byte[] preview;
        public UUID currentUUID;
        public int page;
        public Image image;

        public Task(BaseImageView view, int id, byte[] preview, UUID currentUUID) {
            this.view = view;
            this.id = id;
            this.preview = preview;
            this.currentUUID = currentUUID;
        }

        public Task() {}

        public Task(BaseImageView view, int id, byte[] preview) {
            this.view = view;
            this.id = id;
            this.preview = preview;
        }
    }

    private static final Scalr.Method quality = Scalr.Method.SPEED;
    private static final Color COLOR_WHITE = new Color(255, 255, 255);
    private static final javafx.scene.paint.Color notSelectedColor = javafx.scene.paint.Color.color(0.8,0.8,0.8);
    private static final javafx.scene.paint.Color selectedColor = javafx.scene.paint.Color.web("#c33"); //color(0.3,0.8,0.3);
    private static final javafx.scene.paint.Color fontColor = javafx.scene.paint.Color.color(0.3,0.3,0.3);
    private static final javafx.scene.text.Font font = javafx.scene.text.Font.loadFont(
            BaseImageView.class.getResource("/style/fonts/FjallaOne-Regular.ttf").toExternalForm(),16);

    private static final QueuedWorker<Task> mainWorkersPool = new QueuedWorker<Task>() {
        @Override
        public void threadEvent(Task task) {
            //System.out.println("id = "+task.id);
            try {
                if (task.id < 0) {
                    toPreview(new Task(task.view, -1, null, task.currentUUID));
                    return;
                }

                final byte[] fileContent = task.view.requestItem(task.id);
                if (fileContent == null) {
                    toPreview(new Task(task.view, -1, null, task.currentUUID));
                    return;
                }

                final byte[] cachedItem = task.view.requestCacheItem(fileContent);
                if (cachedItem != null) {
                    final Task t = new Task(task.view, -1, cachedItem, task.currentUUID);
                    toPreview(t);
                } else {
                    final byte[] img = processImage(task, fileContent);
                    final Task t = new Task(task.view, -1, img, task.currentUUID);
                    toPreview(t);
                }
            } catch (IOException e) {
                System.err.println("ERROR: / " + e.getClass().getSimpleName() + " / " + e.getMessage() + "\n\n");
            } catch (Throwable e) {
                System.err.println("ERROR: / " + e.getClass().getSimpleName() + " / " + e.getMessage() + "\n\n");
                e.printStackTrace();
            }
        }

        private void toPreview(Task task) {
            try {
                if (task.preview == null) {
                    if (checkUUID(task)) task.view.delImageMT();
                } else {
                    final Image resultImage = new Image(new ByteArrayInputStream(task.preview));
                    if (checkUUID(task)) task.view.addImageMT(resultImage);
                }
            } catch (Throwable e) {
                System.err.println("ERROR: / " + e.getClass().getSimpleName() + " / " + e.getMessage() + "\n\n");
                e.printStackTrace();
            }
        }

        private boolean checkUUID(Task task) {
            if (task.currentUUID == null || task.view.lastUUID.get() == null) return false;
            return task.currentUUID.equals(task.view.lastUUID.get());
        }
    };

    public abstract void onClick(MouseEvent event, int index, BaseImageView object);
    public abstract void deleteCacheItem(byte[] file);
    public abstract byte[] requestCacheItem(byte[] file);
    public abstract void saveCacheItem(byte[] file, byte[] item);
    public abstract byte[] requestItem(int index);

    private boolean selected = false;
    private final AtomicReference<Task> lastTask = new AtomicReference<>();
    private final AtomicReference<UUID> lastUUID = new AtomicReference<>();

    public static byte[] processImage(Task task, byte[] data) throws IOException {
        final BufferedImage bufferedImageOriginal = ImageIO.read(new ByteArrayInputStream(data));
        final Double w = task.view.getWidth();
        final Double h = task.view.getHeight();
        if (w > 0 && h > 0) {
            final BufferedImage bi = task.view.resizeImage(bufferedImageOriginal, w.longValue(), h.longValue(), true);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            task.view.saveCacheItem(data, baos.toByteArray());
            return baos.toByteArray();
        }

        return null;
    }

    public boolean isSelected() {
        return selected;
    }

    public void inverseSelection() {
        setSelected(!selected);
    }

    public void setSelected(boolean selected) {
        if (lastTask.get() != null && lastTask.get().image != null) {
            this.selected = selected;
            drawImage(lastTask.get().image);
        }
    }

    public static void disposeStatic() {
        mainWorkersPool.dispose();
    }

    void addImageMT(final Image image) {
        final CountDownLatch c = new CountDownLatch(1);
            Platform.runLater(() -> {
                drawImage(image);
                c.countDown();
            });
        try {
            c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void drawImage(final Image image) {
        if (lastTask.get() != null) lastTask.get().image = image;
        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
        context.drawImage(image, 0, 0);
        context.setLineWidth(isSelected() ? 9.0 : 1.5);
        context.setStroke(isSelected() ? selectedColor : notSelectedColor);
        context.strokeRect(0, 0, getWidth(), getHeight());
    }

    synchronized void delImageMT() {
        Platform.runLater(this::clean);
    }

    private synchronized void clean() {
        if (lastTask.get() != null) lastTask.get().image = null;
        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
    }

    private synchronized void pleaseWait() {
        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
        context.setLineWidth(3.0);
        context.setStroke(notSelectedColor);
        context.strokeRect(0, 0, getWidth(), getHeight());
        context.setFill(fontColor);
        context.setTextAlign(TextAlignment.CENTER);
        context.setTextBaseline(VPos.CENTER);
        context.setFont(font);
        context.fillText("Please, wait...", getWidth()/2, getHeight()/2);
    }

    protected BaseImageView() {
        setOnMouseClicked(e -> {
            if (lastTask.get() != null) {
                onClick(e, lastTask.get().id, this);
            }
        });
    }

    public void delImage() {
        final UUID uuid = UUID.randomUUID();
        lastUUID.set(uuid);

        lastTask.set(null);
        mainWorkersPool.pushTask(new Task(this, -1, null, uuid));
    }

    public void setImage(int id, int pageId) {
        pleaseWait();

        synchronized (mainWorkersPool.getQueue()) {
            final List<Task> list = mainWorkersPool.getQueue().stream().filter(t -> t.page != pageId).collect(Collectors.toList());
            mainWorkersPool.getQueue().removeAll(list);
        }

        final UUID uuid = UUID.randomUUID();
        lastUUID.set(uuid);

        final Task task = new Task();
        task.id = id;
        task.view = this;
        task.currentUUID = uuid;
        task.page = pageId;

        lastTask.set(task);
        mainWorkersPool.pushTask(task);
    }

    BufferedImage resizeImage(BufferedImage image2, long sizeW, long sizeH, boolean crop) {
        final int
                w_size = image2.getWidth(null),
                h_size = image2.getHeight(null);

        final BufferedImage srcImage = new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = srcImage.createGraphics();
        g2d.setBackground(COLOR_WHITE);
        g2d.setColor(COLOR_WHITE);
        g2d.fillRect(0, 0, w_size, h_size);
        g2d.drawImage(image2, 0, 0, null);
        g2d.dispose();

        final BufferedImage out_img, crop_img;
        if (crop) {
            if (srcImage.getWidth() > srcImage.getHeight()) {
                final double
                        in_k  = ((double) srcImage.getWidth()) / ((double) srcImage.getHeight()),
                        out_k = ((double) sizeW) / ((double) sizeH);

                out_img = Scalr.resize(srcImage, quality, Scalr.Mode.FIT_TO_HEIGHT, (int) ((in_k < out_k) ? ((int)(sizeH * out_k)) : (sizeH)), Scalr.OP_ANTIALIAS);
                final long
                        out_x = (out_img.getWidth() - sizeW) / 2,
                        out_y = (out_img.getHeight() - sizeH) / 2;

                crop_img = Scalr.crop(out_img, (int) out_x, (int) out_y, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
            } else {
                final double
                        in_k  = ((double) srcImage.getHeight()) / ((double) srcImage.getWidth()),
                        out_k = ((double) sizeH) / ((double) sizeW);

                out_img = Scalr.resize(srcImage, quality, Scalr.Mode.FIT_TO_WIDTH, (int) ((in_k < out_k) ? ((int)(sizeW * out_k)) : (sizeW)), Scalr.OP_ANTIALIAS);
                final long
                        out_x = (out_img.getWidth() - sizeW) / 2,
                        out_y = (out_img.getHeight() - sizeH) / 2;

                crop_img = Scalr.crop(out_img, (int) out_x, (int) out_y, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
            }

            return crop_img;
        } else {
            out_img = Scalr.resize(srcImage,quality, Scalr.Mode.AUTOMATIC, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
            return out_img;
        }
    }
}
