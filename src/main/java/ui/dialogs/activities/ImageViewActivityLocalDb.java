package ui.dialogs.activities;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.scene.image.Image;
import jiconfont.icons.GoogleMaterialDesignIcons;
import model.ImageEntityWrapper;
import model.entity.ImageEntity;
import model.entity.TagEntity;
import services.api.LocalDaoService;
import services.api.LocalStorageService;
import services.api.UtilService;
import ui.annotation.style.HasStyledElements;
import utils.counter.MultiCounter;

@Component
@HasStyledElements
public class ImageViewActivityLocalDb extends ImageViewActivity {
	private Collection<TagEntity> tagsForFilter = null;
	private ImageEntityWrapper imageEntityWrapper = null;
	private ImageEntity currentImage = null;

	private MultiCounter pageCounter = new MultiCounter(null, null, null);
	private MultiCounter indexCounter = new MultiCounter(pageCounter, this::loadData, this::loadData);

	@Autowired
	LocalDaoService localDaoService;

	@Autowired
	LocalStorageService localStorageService;

	@Autowired
	UtilService utilService;

	public ImageViewActivityLocalDb() {
		super();
		getVerticalIconsPanel().addFixedSeparator();
		getVerticalIconsPanel().add("Save to exchange", GoogleMaterialDesignIcons.CLOUD_UPLOAD, (e) -> {
			localStorageService.saveImageToExchangeFolder(currentImage, utilService.getConfig().getBrowserOutboxFolder());
			popup("Save to exchange", "File saved!");
		});
	}

	@Override
	public void PrevKey() {
		indexCounter.dec();
		nextImage();
	}

	@Override
	public void NextKey() {
		indexCounter.inc();
		nextImage();
	}

	private void loadData() {
		int start = pageCounter.getCounter() * indexCounter.getMax();
		int end = (pageCounter.getCounter() + 1) * indexCounter.getMax();

		imageEntityWrapper = localDaoService.imagesFindByTags(tagsForFilter, start, end);
		if (imageEntityWrapper.getCount() <= 0) return;
		final List<ImageEntity> list = imageEntityWrapper.getList();

		setContent(list.get(indexCounter.getCounter()));
	}

	private void nextImage() {
		if (imageEntityWrapper == null) {
			loadData();
		}
		final List<ImageEntity> list = imageEntityWrapper.getList();
		setContent(list.get(indexCounter.getCounter()));
	}

	private void setContent(ImageEntity imageEntity) {
		this.currentImage = imageEntity;
		final byte[] content = localStorageService.getLocalDBItem(imageEntity.getImageHash());
		final Image img = new Image(new ByteArrayInputStream(content));
		setImage(img);
	}

	public void showNext(Collection<TagEntity> tagsForFilter, ImageEntity image, int pageId, int id, int countPerPage, int pages) {
		super.showNext();

		indexCounter.setMax(countPerPage);
		indexCounter.setCounter(id);
		pageCounter.setMax(pages);
		pageCounter.setCounter(pageId);

		this.imageEntityWrapper = null;
		this.tagsForFilter = tagsForFilter;
		this.currentImage = image;

		setContent(image);
	}
}
