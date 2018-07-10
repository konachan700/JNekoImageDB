package ui.imageview.localdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.scene.input.MouseEvent;
import model.ImageEntityWrapper;
import model.entity.ImageEntity;
import model.entity.TagEntity;
import services.api.LocalDaoService;
import services.api.LocalStorageService;
import services.api.UtilService;
import ui.imageview.AbstractImageDashboard;
import ui.imageview.PageStateAction;

@Component
public class LocalDbImageDashboard extends AbstractImageDashboard<LocalDbImageView> implements LocalDbImageViewEvents, DisposableBean {
	@Autowired
	LocalStorageService localStorageService;

	@Autowired
	LocalDaoService localDaoService;

	@Autowired
	UtilService utilService;

	@Autowired
	private ApplicationContext applicationContext;

	private PageStateAction onPageChangedEvent = null;
	private PageStateAction onPageCountChangedEvent = null;
	private LocalDbImageViewEvents event = null;

	private final ArrayList<LocalDbImageView> imageViews = new ArrayList<>();
	private final CopyOnWriteArrayList<ImageEntity> files = new CopyOnWriteArrayList<>();
	private final Set<ImageEntity> selectedFiles = new HashSet<>();


	private Collection<TagEntity> tagsForFilter = null;
	private final Set<TagEntity> recomendedTags = new HashSet<>();

	@Override
	protected void onResizeCompleted() {
		imageViews.forEach(LocalDbImageView::setImage);
	}

	@Override
	protected LocalDbImageView imageViewRequest(int number) {
		if (imageViews.size() == number) {
			final LocalDbImageView f = applicationContext.getBean(LocalDbImageView.class);
			f.setEvent(this);
			imageViews.add(f);
		}
		return imageViews.get(number);
	}

	@Override
	public void pageChanged(int page) {
		if (onPageChangedEvent!= null) {
			onPageChangedEvent.onChange(page);
		}
		reloadPage(page);
	}

	@PostConstruct
	void init() {
		this.generateView(utilService.getConfig().getLocalDbPreviewsCountInRow(), utilService.getConfig().getLocalDbPreviewsCountInCol());
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

		if (onPageCountChangedEvent != null) {
			onPageCountChangedEvent.onChange(pagesCount);
		}
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
		if (onPageChangedEvent!= null) {
			onPageChangedEvent.onChange(page);
		}
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

	public PageStateAction getOnPageChangedEvent() {
		return onPageChangedEvent;
	}

	public void setOnPageChangedEvent(PageStateAction onPageChangedEvent) {
		this.onPageChangedEvent = onPageChangedEvent;
	}

	public PageStateAction getOnPageCountChangedEvent() {
		return onPageCountChangedEvent;
	}

	public void setOnPageCountChangedEvent(PageStateAction onPageCountChangedEvent) {
		this.onPageCountChangedEvent = onPageCountChangedEvent;
	}

	public LocalDbImageViewEvents getEvent() {
		return event;
	}

	public void setEvent(LocalDbImageViewEvents event) {
		this.event = event;
	}

	@Override public void onItemClick(MouseEvent e, ImageEntity image, int pageId, int id, int pageCount) {
		if (id >= imageViews.size()) return;
		final LocalDbImageView v = imageViews.get(id);

		if (e.getButton() == utilService.getConfig().getPrimaryButton()) {
			if (e.getClickCount() > 1 || v.isSelected()) {
				if (event != null) {
					event.onItemClick(e, image, pageId, id, pageCount);
				}
				return;
			}
			selectedFiles.add(image);
		} else if (e.getButton() == utilService.getConfig().getSecondaryButton()) {
			if (v.isSelected()) {
				selectedFiles.remove(image);
			} else {
				selectedFiles.add(image);
			}
		}
	}

	@Override public void destroy() throws Exception {
		disposeStatic();
	}
}
