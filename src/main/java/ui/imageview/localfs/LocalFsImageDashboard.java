package ui.imageview.localfs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import services.api.CryptographyService;
import services.api.LocalStorageService;
import services.api.UtilService;
import ui.imageview.AbstractImageDashboard;
import ui.imageview.PageStateAction;

@Component
public class LocalFsImageDashboard extends AbstractImageDashboard<LocalFsImageView> implements LocalFsImageViewEvent, DisposableBean {
	@Autowired
	CryptographyService cryptographyService;

	@Autowired
	LocalStorageService localStorageService;

	@Autowired
	UtilService utilService;

	@Autowired
	private ApplicationContext applicationContext;

	private ImageView fileImageView = new ImageView();

	private PageStateAction onPageChangedEvent = null;
	private PageStateAction onPageCountChangedEvent = null;

	private final ArrayList<LocalFsImageView> imageViews = new ArrayList<>();
	private final CopyOnWriteArrayList<Path> files = new CopyOnWriteArrayList<>();
	private final Set<Path> selectedFiles = new HashSet<>();

	@PostConstruct
	void init() {
		fileImageView.getStyleClass().addAll("image_view_file_list");
		fileImageView.setOnMouseClicked(e -> showOrHideImage(null));
		fileImageView.setPreserveRatio(true);
		fileImageView.setSmooth(true);
		fileImageView.setCache(false);
		fileImageView.fitWidthProperty().bind(getRootPane().widthProperty().divide(1.25));
		fileImageView.fitHeightProperty().bind(getRootPane().heightProperty().divide(1.25));

		generateView(utilService.getConfig().getLocalFsPreviewsCountInRow(), utilService.getConfig().getLocalFsPreviewsCountInCol());
	}

	@Override protected void onResizeCompleted() {
		pageChanged(getCurrentPage());
	}

	@Override protected LocalFsImageView imageViewRequest(int number) {
		if (imageViews.size() == number) {
			LocalFsImageView localFsImageView = applicationContext.getBean(LocalFsImageView.class);
			localFsImageView.setEvent(this);
			imageViews.add(localFsImageView);
		}
		return imageViews.get(number);
	}

	private void clearView() {
		getRootPane().getChildren().remove(fileImageView);
		removeBlur(getRootVBox());
	}

	private void showOrHideImage(String url) {
		if (getRootPane().getChildren().contains(fileImageView)) {
			clearView();
			return;
		}

		if (url != null && !url.isEmpty()) {
			final File img = new File(url);
			if (img.exists() && img.length() > 0) {
				try {
					byte[] content = Files.readAllBytes(img.getAbsoluteFile().toPath());
					final Image image = new Image(new ByteArrayInputStream(content));
					fileImageView.setImage(image);
					getRootPane().getChildren().addAll(fileImageView);
					setBlur(getRootVBox());
				} catch (IOException e) {
					e.printStackTrace();
					clearView();
				}
			} else {
				clearView();
			}
		} else {
			clearView();
		}
	}

	@Override
	public void pageChanged(int page) {
		reloadPage(page);
		if (onPageChangedEvent != null) {
			onPageChangedEvent.onChange(page);
		}
	}

	private void reloadPage(int page) {
		if (page < 0) return;

		final int start = page * getElementsPerPage();
		final int end = (page + 1) * getElementsPerPage();

		final int imgCount = files.size();
		final int pagesTail = imgCount % getElementsPerPage();
		final int pagesWoTail = imgCount - pagesTail;
		final int pagesCount = (pagesWoTail / getElementsPerPage());

		final List<Path> subList = files.subList(start, (end >= files.size()) ? files.size() : end);

		for (int i=0; i<imageViews.size(); i++) {
			if (i < subList.size()) {
				imageViews.get(i).setImage(subList.get(i), page, i, pagesCount, getElementsPerPage());
				imageViews.get(i).setSelected(selectedFiles.contains(subList.get(i)));
			} else {
				imageViews.get(i).deleteImage();
			}
		}
	}

	public void cd(Path dir) {
		if (cryptographyService == null || localStorageService == null) throw new IllegalStateException("Call init() before use");
		files.clear();
		final List<File> lfiles = Optional.ofNullable(dir)
				.map(Path::toAbsolutePath)
				.map(Path::toFile)
				.map(File::listFiles)
				.map(Arrays::asList)
				.orElse(new ArrayList<>());
		if (lfiles.isEmpty()) return;

		final List<Path> imagesList = lfiles.parallelStream()
				.filter(File::isFile)
				.filter(file -> utilService.getConfig().getLocalFsAllowedFileTypes()
						.stream().filter(name -> file.getName().toLowerCase().endsWith(name)).count() > 0)
				.map(File::toPath)
				.collect(Collectors.toList());

		imagesList.sort(Comparator.comparing(a -> a.toFile().getName()));
		files.addAll(imagesList);

		final int pagesTail = files.size() % getElementsPerPage();
		final int pagesWoTail = files.size() - pagesTail;
		final int pagesCount = (pagesWoTail / getElementsPerPage());

		resetCurrentPage();
		setPagesCount(pagesCount);
		reloadPage(0);
		if (onPageCountChangedEvent != null) {
			onPageCountChangedEvent.onChange(pagesCount);
		}
	}

	public void selectAll() {
		getSelectedFiles().addAll(files);
		pageChanged(getCurrentPage());
	}

	public void selectNone() {
		getSelectedFiles().clear();
		pageChanged(getCurrentPage());
	}

	public Set<Path> getSelectedFiles() {
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

	@Override
	public void onClick(MouseEvent e, Path imageFile, int pageId, int id, int pageCount) {
		showOrHideImage(imageFile.toFile().getAbsolutePath());
	}

	@Override
	public void onSelect(boolean selected, MouseEvent e, Path imageFile, int pageId, int id, int pageCount) {
		if (imageFile == null) return;
		if (selected) {
			selectedFiles.add(imageFile);
		} else {
			selectedFiles.remove(imageFile);
		}
	}

	@Override public void destroy() throws Exception {
		disposeStatic();
	}
}
