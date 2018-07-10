package ui.imageview.localfs;

import static model.GlobalConfig.IMAGE_VIEW__NON_SELECTED_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__SELECTED_COLOR;
import static model.GlobalConfig.PREVIEW_FORMAT;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import services.api.CryptographyService;
import services.api.LocalStorageService;
import services.api.UtilService;
import utils.ImageUtils;
import utils.UiUtils;
import worker.QueuedWorker;

@Component
public class LocalFsImageViewWorker extends QueuedWorker<LocalFsImageViewTask> {
	@Autowired
	LocalStorageService localStorageService;

	@Autowired
	UtilService utilService;

	@Autowired
	CryptographyService cryptographyService;

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

		final Double w, h;
		while (true) {
			Double wt = localFsImageViewTask.getLocalFsImageView().getWidth();
			Double ht = localFsImageViewTask.getLocalFsImageView().getHeight();
			if (wt >= utilService.getConfig().getMinImageSize() && ht >= utilService.getConfig().getMinImageSize()) {
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

		final byte[] previewData = localStorageService.getCacheItem(hash, w.intValue(), h.intValue());
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
