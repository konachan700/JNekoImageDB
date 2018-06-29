package ui.imageview.localdb;

import static model.GlobalConfig.IMAGE_VIEW__FONT;
import static model.GlobalConfig.IMAGE_VIEW__FONT_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__NON_SELECTED_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__SELECTED_COLOR;
import static model.GlobalConfig.PREVIEW_FORMAT;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.entity.ImageEntity;
import proto.LocalStorageService;
import proto.UseServices;
import utils.ImageUtils;
import utils.UiUtils;
import worker.QueuedWorker;

public abstract class LocalDbImageView extends Canvas implements UseServices {
	private static final QueuedWorker<LocalDbImageViewTask> mainWorkersPool = new QueuedWorker<LocalDbImageViewTask>() {
		private LocalStorageService storageService = null;
		final AtomicBoolean lock = new AtomicBoolean(true);

		@Override
		public void threadEvent(LocalDbImageViewTask baseImageViewTask) {
			if (baseImageViewTask == null) return;

			final LocalDbImageView canvas = baseImageViewTask.getLocalDbImageView();
			if (canvas == null) return;
			canvas.setLastTask(baseImageViewTask);

			if (baseImageViewTask.getImageEntity() == null) {
				Platform.runLater(() -> UiUtils.clearCanvas(canvas));
				return;
			}

			if (storageService == null) {
				storageService = canvas.getService(LocalStorageService.class);
			}

			final Double w, h;
			while (true) {
				Double wt = baseImageViewTask.getLocalDbImageView().getWidth();
				Double ht = baseImageViewTask.getLocalDbImageView().getHeight();
				if (wt >= canvas.getConfig().getMinImageSize() && ht >= canvas.getConfig().getMinImageSize()) {
					w = wt;
					h = ht;
					break;
				} else {
					Thread.yield();
				}
			}

			final byte[] hash = baseImageViewTask.getImageEntity().getImageHash();
			final byte[] contentFromCache = storageService.getCacheItem(hash, w.intValue(), h.intValue());
			if (contentFromCache != null) {
				baseImageViewTask.setCachedImage(contentFromCache);
				lock.set(true);
				Platform.runLater(() -> {
					if (canvas.getCurrentId().get() == baseImageViewTask.getPrivateId()) {
						UiUtils.drawBinaryImage(canvas, contentFromCache, w.intValue(), h.intValue(),
								(canvas.isSelected() ? IMAGE_VIEW__SELECTED_COLOR : IMAGE_VIEW__NON_SELECTED_COLOR), canvas.isSelected());
					}
					lock.set(false);
				});
				while (lock.get()) Thread.yield();
			} else {
				final byte[] fullFile = storageService.getLocalDBItem(hash);
				try {
					final BufferedImage bufferedImageOriginal = ImageIO.read(new ByteArrayInputStream(fullFile));
					final BufferedImage bufferedImageResized = ImageUtils.resizeImage(bufferedImageOriginal, w.longValue(), h.longValue(), true);
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(bufferedImageResized, PREVIEW_FORMAT, baos);

					final byte[] binaryPreview = baos.toByteArray();
					baseImageViewTask.setCachedImage(binaryPreview);
					lock.set(true);
					Platform.runLater(() -> {
						if (canvas.getCurrentId().get() == baseImageViewTask.getPrivateId()) {
							UiUtils.drawBinaryImage(canvas, binaryPreview, w.intValue(), h.intValue(),
									(canvas.isSelected() ? IMAGE_VIEW__SELECTED_COLOR : IMAGE_VIEW__NON_SELECTED_COLOR),
									canvas.isSelected());
						}
						lock.set(false);
					});

					storageService.storeCacheItem(hash, binaryPreview, w.intValue(), h.intValue());
					while (lock.get()) Thread.yield();
				} catch (IOException e) {
					e.printStackTrace();
					Platform.runLater(() -> UiUtils.clearCanvas(canvas));
					return;
				}
			}
		}
	};

	private boolean selected = false;
	private LocalDbImageViewTask lastTask = null;
	private final AtomicLong currentId = new AtomicLong(0);

	public abstract void onClick(MouseEvent e, ImageEntity image, int pageId, int id, int pageCount);

    public LocalDbImageView() {
		setOnMouseClicked(e -> {
			if (lastTask != null && lastTask.getCachedImage() != null) {
				onClick(e, lastTask.getImageEntity(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
				setSelected(!isSelected());
			}
		});
    }

    public boolean isSelected() {
    	return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
		refreshImage();
	}

	public void deleteImage() {
		getCurrentId().incrementAndGet();
		selected = false;
		lastTask = null;
		UiUtils.clearCanvas(this);
	}

	private void refreshImage() {
		Double w, h;
		w = this.getWidth();
		h = this.getHeight();
		if (w >= getConfig().getMinImageSize() && h >= getConfig().getMinImageSize()) {
			if (lastTask == null || lastTask.getCachedImage() == null) {
				UiUtils.clearCanvas(this);
			} else {
				UiUtils.drawBinaryImage(this, lastTask.getCachedImage(), w.intValue(), h.intValue(),
						(isSelected() ? IMAGE_VIEW__SELECTED_COLOR : IMAGE_VIEW__NON_SELECTED_COLOR), isSelected());
			}
		}
	}

	public void setImage() {
    	if (lastTask == null) {
			deleteImage();
		} else {
			UiUtils.pleaseWait(this, IMAGE_VIEW__NON_SELECTED_COLOR, IMAGE_VIEW__FONT_COLOR, IMAGE_VIEW__FONT);
			mainWorkersPool.pushTask(lastTask);
		}
	}

	public void setImage(ImageEntity image, int pageId, int id, int pageCount, int countPerPage) {
    	final long privateId = getCurrentId().incrementAndGet();
		selected = false;
		lastTask = null;
		UiUtils.pleaseWait(this, IMAGE_VIEW__NON_SELECTED_COLOR, IMAGE_VIEW__FONT_COLOR, IMAGE_VIEW__FONT);
		LocalDbImageViewTask localDbImageViewTask = new LocalDbImageViewTask(this, image, pageId, id, pageCount);
		localDbImageViewTask.setPrivateId(privateId);
		mainWorkersPool.pushTask(localDbImageViewTask, countPerPage);
	}

	public static void disposeStatic() {
		mainWorkersPool.dispose();
	}

	public LocalDbImageViewTask getLastTask() {
		return lastTask;
	}

	public void setLastTask(LocalDbImageViewTask lastTask) {
		this.lastTask = lastTask;
	}

	public AtomicLong getCurrentId() {
		return currentId;
	}
}
