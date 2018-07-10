package ui.imageview.localdb;

import static model.GlobalConfig.IMAGE_VIEW__NON_SELECTED_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__SELECTED_COLOR;
import static model.GlobalConfig.PREVIEW_FORMAT;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import services.api.LocalStorageService;
import services.api.UtilService;
import utils.ImageUtils;
import utils.UiUtils;
import worker.QueuedWorker;

@Component
@Scope("prototype")
public class LocalDbImageViewWorker extends QueuedWorker<LocalDbImageViewTask> {
	@Autowired
	LocalStorageService localStorageService;

	@Autowired
	UtilService utilService;

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

		final Double w, h;
		while (true) {
			Double wt = baseImageViewTask.getLocalDbImageView().getWidth();
			Double ht = baseImageViewTask.getLocalDbImageView().getHeight();
			if (wt >= utilService.getConfig().getMinImageSize() && ht >= utilService.getConfig().getMinImageSize()) {
				w = wt;
				h = ht;
				break;
			} else {
				Thread.yield();
			}
		}

		final byte[] hash = baseImageViewTask.getImageEntity().getImageHash();
		final byte[] contentFromCache = localStorageService.getCacheItem(hash, w.intValue(), h.intValue());
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
			final byte[] fullFile = localStorageService.getLocalDBItem(hash);
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

				localStorageService.storeCacheItem(hash, binaryPreview, w.intValue(), h.intValue());
				while (lock.get()) Thread.yield();
			} catch (IOException e) {
				e.printStackTrace();
				Platform.runLater(() -> UiUtils.clearCanvas(canvas));
				return;
			}
		}
	}
}
