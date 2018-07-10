package ui.imageview.localfs;

import static model.GlobalConfig.IMAGE_VIEW__FONT;
import static model.GlobalConfig.IMAGE_VIEW__FONT_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__NON_SELECTED_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__SELECTED_COLOR;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import services.api.UtilService;
import utils.UiUtils;

@Component
@Scope("prototype")
public class LocalFsImageView extends Canvas implements DisposableBean {
	private static LocalFsImageViewWorker mainWorkersPool = null;

	private boolean selected = false;
	private LocalFsImageViewTask lastTask = null;
	private final AtomicLong currentId = new AtomicLong(0);

	@Autowired
	UtilService utilService;

	private LocalFsImageViewEvent event = null;

	public LocalFsImageView() {
		setOnMouseClicked(e -> {
			if (getEvent() == null) return;
			if (lastTask != null && lastTask.getCachedImage() != null) {
				if (e.getButton() == MouseButton.PRIMARY) {
					if (e.getClickCount() >= 2) {
						getEvent().onClick(e, lastTask.getFile(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
					} else {
						setSelected(!isSelected());
						getEvent().onSelect(isSelected(), e, lastTask.getFile(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
					}
				} else {
					getEvent().onClick(e, lastTask.getFile(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
				}
			}
		});
	}

	private void refreshImage() {
		Double w, h;
		w = this.getWidth();
		h = this.getHeight();
		if (w >= utilService.getConfig().getMinImageSize() && h >= utilService.getConfig().getMinImageSize()) {
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

	public LocalFsImageViewTask getLastTask() {
		return lastTask;
	}

	public void setLastTask(LocalFsImageViewTask lastTask) {
		this.lastTask = lastTask;
	}

	public AtomicLong getCurrentId() {
		return currentId;
	}

	public LocalFsImageViewEvent getEvent() {
		return event;
	}

	public void setEvent(LocalFsImageViewEvent event) {
		this.event = event;
	}

	@Override
	public void destroy() throws Exception {
		mainWorkersPool.dispose();
	}
}
