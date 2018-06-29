package ui.imageview.localdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.scene.input.MouseEvent;
import model.ImageEntityWrapper;
import model.entity.ImageEntity;
import model.entity.TagEntity;
import proto.LocalDaoService;
import proto.LocalStorageService;
import proto.UseServices;
import ui.imageview.AbstractImageDashboard;

public abstract class LocalDbImageDashboard extends AbstractImageDashboard<LocalDbImageView> implements UseServices {
	private LocalStorageService localStorageService;
	private LocalDaoService localDaoService;

	private final ArrayList<LocalDbImageView> imageViews = new ArrayList<>();
	private final CopyOnWriteArrayList<ImageEntity> files = new CopyOnWriteArrayList<>();
	private final Set<ImageEntity> selectedFiles = new HashSet<>();

	public abstract void onPageChanged(int page);
	public abstract void onPageCountChanged(int pages);
	public abstract void onItemClick(MouseEvent e, ImageEntity image, int pageId, int id, int pageCount);

	private Collection<TagEntity> tagsForFilter = null;
	private final Set<TagEntity> recomendedTags = new HashSet<>();

	@Override
	protected void onResizeCompleted() {
		imageViews.forEach(LocalDbImageView::setImage);
	}

	@Override
	protected LocalDbImageView imageViewRequest(int number) {
		if (imageViews.size() == number) {
			final LocalDbImageView f = new LocalDbImageView() {
				@Override public void onClick(MouseEvent e, ImageEntity image, int pageId, int id, int pageCount) {
					if (id >= imageViews.size()) return;
					final LocalDbImageView v = imageViews.get(id);

					if (e.getButton() == getConfig().getPrimaryButton()) {
						if (e.getClickCount() > 1 || v.isSelected()) {
							onItemClick(e, image, pageId, id, pageCount);
							return;
						}
						selectedFiles.add(image);
					} else if (e.getButton() == getConfig().getSecondaryButton()) {
						if (v.isSelected()) {
							selectedFiles.remove(image);
						} else {
							selectedFiles.add(image);
						}
					}
				}
			};
			imageViews.add(f);
		}
		return imageViews.get(number);
	}

	@Override
	public void pageChanged(int page) {
		onPageChanged(page);
		reloadPage(page);
	}

	public LocalDbImageDashboard() {
		super();

		this.localStorageService = getService(LocalStorageService.class);
		this.localDaoService = getService(LocalDaoService.class);

		this.generateView(getConfig().getLocalDbPreviewsCountInRow(), getConfig().getLocalDbPreviewsCountInCol());
	}

	private void reloadPage(int page) {
		if (page < 0) return;

		final int start = page * getElementsPerPage();
		final int end = (page + 1) * getElementsPerPage();

		ImageEntityWrapper imageEntityWrapper = localDaoService.imagesFindByTags(getTagsForFilter(), start, end);

		getRecomendedTags().clear();
		imageEntityWrapper.getList().forEach(e -> getRecomendedTags().addAll(e.getTags()));

		final int imgCount = imageEntityWrapper.getCount();
		final int pagesTail = imgCount % getElementsPerPage();
		final int pagesWoTail = imgCount - pagesTail;
		final int pagesCount = (pagesWoTail / getElementsPerPage());

		//System.out.println("imgCount="+imgCount+" pagesTail="+pagesTail+" pagesWoTail="+pagesWoTail);

		files.clear();
		files.addAll(imageEntityWrapper.getList());

		onPageCountChanged(pagesCount);
		setPagesCount(pagesCount);

		for (int i=0; i<imageViews.size(); i++) {
			if (i < files.size()) {
				imageViews.get(i).setImage(files.get(i), page, i, pagesCount, getElementsPerPage());
				imageViews.get(i).setSelected(selectedFiles.contains(files.get(i)));
			} else {
				imageViews.get(i).deleteImage();
			}
		}
	}

	public void refresh(int page) {
		reloadPage(page);
		onPageChanged(page);
	}

	public void setFilterByTags(Collection<TagEntity> tags) {
		setTagsForFilter(tags);
	}

	public Collection<TagEntity> getTagsForFilter() {
		return tagsForFilter;
	}

	public void setTagsForFilter(Collection<TagEntity> tagsForFilter) {
		this.tagsForFilter = tagsForFilter;
	}

	public Set<TagEntity> getRecomendedTags() {
		return recomendedTags;
	}

	public void selectAll() {
		getSelectedFiles().addAll(files);
		pageChanged(getCurrentPage());
	}

	public void selectNone() {
		getSelectedFiles().clear();
		pageChanged(getCurrentPage());
	}

	public Set<ImageEntity> getSelectedFiles() {
		return selectedFiles;
	}
}
