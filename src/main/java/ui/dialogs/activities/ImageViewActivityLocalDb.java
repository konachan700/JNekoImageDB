package ui.dialogs.activities;

import static java.nio.file.StandardOpenOption.CREATE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Hex;

import javafx.scene.image.Image;
import jiconfont.icons.GoogleMaterialDesignIcons;
import model.ImageEntityWrapper;
import model.Metadata;
import model.entity.ImageEntity;
import model.entity.TagEntity;
import proto.LocalDaoService;
import proto.LocalStorageService;
import ui.annotation.style.HasStyledElements;
import ui.dialogs.activities.engine.ActivityHolder;
import utils.counter.MultiCounter;

@HasStyledElements
public class ImageViewActivityLocalDb extends ImageViewActivity {
	private Collection<TagEntity> tagsForFilter = null;
	private ImageEntityWrapper imageEntityWrapper = null;
	private ImageEntity currentImage = null;

	private MultiCounter pageCounter = new MultiCounter(null, null, null);
	private MultiCounter indexCounter = new MultiCounter(pageCounter, this::loadData, this::loadData);

	public ImageViewActivityLocalDb(ActivityHolder activityHolder) {
		super(activityHolder);
		getVerticalIconsPanel().addFixedSeparator();
		getVerticalIconsPanel().add("Save to exchange", GoogleMaterialDesignIcons.CLOUD_UPLOAD, (e) -> {
			getService(LocalStorageService.class).saveImageToExchangeFolder(currentImage, getConfig().getBrowserOutboxFolder());
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

		imageEntityWrapper = getService(LocalDaoService.class).imagesFindByTags(tagsForFilter, start, end);
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
		final byte[] content = getService(LocalStorageService.class).getLocalDBItem(imageEntity.getImageHash());
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
