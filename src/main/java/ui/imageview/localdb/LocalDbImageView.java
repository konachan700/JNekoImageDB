package ui.imageview.localdb;

import static model.GlobalConfig.IMAGE_VIEW__FONT;
import static model.GlobalConfig.IMAGE_VIEW__FONT_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__NON_SELECTED_COLOR;
import static model.GlobalConfig.IMAGE_VIEW__SELECTED_COLOR;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javafx.scene.canvas.Canvas;
import model.entity.ImageEntity;
import services.api.UtilService;
import utils.UiUtils;

@Component
@Scope("prototype")
public class LocalDbImageView extends Canvas implements DisposableBean {
	private static LocalDbImageViewWorker mainWorkersPool = null;

	@Autowired
	UtilService utilService;

	@Autowired
	private ApplicationContext applicationContext;

	private LocalDbImageViewEvents event = null;

	private boolean selected = false;
	private LocalDbImageViewTask lastTask = null;
	private final AtomicLong currentId = new AtomicLong(0);

    @PostConstruct
	void init() {
    	if (mainWorkersPool == null) {
			mainWorkersPool = applicationContext.getBean(LocalDbImageViewWorker.class);
		}

		setOnMouseClicked(e -> {
			if (lastTask != null && lastTask.getCachedImage() != null) {
				if (getEvent() != null) {
					getEvent().onItemClick(e, lastTask.getImageEntity(), lastTask.getPageId(), lastTask.getId(), lastTask.getPageCount());
				}
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

	public void setImage(ImageEntity image, int pageId, int id, int pageCount, int countPerPage) {
    	final long privateId = getCurrentId().incrementAndGet();
		selected = false;
		lastTask = null;
		UiUtils.pleaseWait(this, IMAGE_VIEW__NON_SELECTED_COLOR, IMAGE_VIEW__FONT_COLOR, IMAGE_VIEW__FONT);
		LocalDbImageViewTask localDbImageViewTask = new LocalDbImageViewTask(this, image, pageId, id, pageCount);
		localDbImageViewTask.setPrivateId(privateId);
		mainWorkersPool.pushTask(localDbImageViewTask, countPerPage);
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

	public LocalDbImageViewEvents getEvent() {
		return event;
	}

	public void setEvent(LocalDbImageViewEvents event) {
		this.event = event;
	}

	@Override public void destroy() throws Exception {
		if (mainWorkersPool != null) {
			mainWorkersPool.dispose();
		}
	}
}
