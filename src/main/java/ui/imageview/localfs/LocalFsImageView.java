package ui.imageview.localfs;


import static model.GlobalConfig.IMAGE_VIEW__FONT;
import static model.GlobalConfig.IMAGE_VIEW__FONT_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__NON_SELECTED_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__SELECTED_COLOR;
import static model.GlobalConfig.PREVIEW_FORMAT;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import proto.CryptographyService;
import proto.LocalStorageService;
import proto.UseServices;
import utils.ImageUtils;
import utils.UiUtils;
import worker.QueuedWorker;

public abstract class LocalFsImageView extends Canvas implements UseServices {
	private static final QueuedWorker<LocalFsImageViewTask> mainWorkersPool = new QueuedWorker<LocalFsImageViewTask>() {
		private LocalStorageService storageService = null;
		private CryptographyService cryptographyService = null;
		final AtomicBoolean lock = new AtomicBoolean(true);

		@Override
		public void threadEvent(LocalFsImageViewTask localFsImageViewTask) {
			if (localFsImageViewTask == null) return;

			final LocalFsImageView canvas = localFsImageViewTask.getLocalFsImageView();
			if (canvas == null) return;
			canvas.setLastTask(localFsImageViewTask);

			if (localFsImageViewTask.getFile() == null) {
				Platform.runLater(() -> UiUtils.clearCanvas(canvas));
				return;
			}

			if (storageService == null) {
				storageService = canvas.getService(LocalStorageService.class);
			}

			if (cryptographyService == null) {
				cryptographyService = canvas.getService(CryptographyService.class);
			}

			final Double w, h;
			while (true) {
				Double wt = localFsImageViewTask.getLocalFsImageView().getWidth();
				Double ht = localFsImageViewTask.getLocalFsImageView().getHeight();
				if (wt >= canvas.getConfig().getMinImageSize() && ht >= canvas.getConfig().getMinImageSize()) {
					w = wt;
					h = ht;
					break;
				} else {
					Thread.yield();
				}
			}

			final File file = localFsImageViewTask.getFile().toFile();
			final String cacheId = file.getName() + "/:" + file.length();
			final byte[] hash = cryptographyService.hash(cacheId.getBytes());

			final byte[] previewData = storageService.getCacheItem(hash, w.intValue(), h.intValue());
			if (previewData != null) {
				localFsImageViewTask.setCachedImage(previewData);
				lock.set(true);
				Platform.runLater(() -> {
					if (canvas.getCurrentId().get() == localFsImageViewTask.getPrivateId()) {
						UiUtils.drawBinaryImage(canvas, previewData, w.intValue(), h.intValue(),
								(canvas.isSelected() ? IMAGE_VIEW__SELECTED_COLOR : IMAGE_VIEW__NON_SELECTED_COLOR), canvas.isSelected());
					}
					lock.set(false);
				});
				while (lock.get()) Thread.yield();
			} else {
				try {
					final byte[] fullFile = Files.readAllBytes(localFsImageViewTask.getFile());

					final BufferedImage bufferedImageOriginal = ImageIO.read(new ByteArrayInputStream(fullFile));
					final BufferedImage bufferedImageResized = ImageUtils.resizeImage(bufferedImageOriginal, w.longValue(), h.longValue(), true);
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(bufferedImageResized, PREVIEW_FORMAT, baos);

					final byte[] binaryPreview = baos.toByteArray();
					localFsImageViewTask.setCachedImage(binaryPreview);
					lock.set(true);
					Platform.runLater(() -> {
						if (canvas.getCurrentId().get() == localFsImageViewTask.getPrivateId()) {
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
	private LocalFsImageViewTask lastTask = null;
	private final AtomicLong currentId = new AtomicLong(0);

	public abstract void onClick(MouseEvent e, Path imageFile, int pageId, int id, int pageCount);
	public abstract void onSelect(boolean selected, MouseEvent e, Path imageFile, int pageId, int id, int pageCount);

	public LocalFsImageView() {
		setOnMouseClicked(e -> {
			if (lastTask != null && lastTask.getCachedImage() != null) {
				if (e.getButton() == MouseButton.PRIMARY) {
					if (e.getClickCount() >= 2) {
						onClick(e, lastTask.getFile(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
					} else {
						setSelected(!isSelected());
						onSelect(isSelected(), e, lastTask.getFile(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
					}
				} else {
					onClick(e, lastTask.getFile(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
				}
			}
		});
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

	public void setImage(Path image, int pageId, int id, int pageCount, int countPerPage) {
		final long privateId = getCurrentId().incrementAndGet();
		selected = false;
		lastTask = null;
		UiUtils.pleaseWait(this, IMAGE_VIEW__NON_SELECTED_COLOR, IMAGE_VIEW__FONT_COLOR, IMAGE_VIEW__FONT);
		LocalFsImageViewTask localFsImageViewTask = new LocalFsImageViewTask(image, this, pageId, id, pageCount);
		localFsImageViewTask.setPrivateId(privateId);
		mainWorkersPool.pushTask(localFsImageViewTask, countPerPage);
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

	public static void disposeStatic() {
		mainWorkersPool.dispose();
	}

	public LocalFsImageViewTask getLastTask() {
		return lastTask;
	}

	public void setLastTask(LocalFsImageViewTask lastTask) {
		this.lastTask = lastTask;
	}

	public AtomicLong getCurrentId() {
		return currentId;
	}
}
