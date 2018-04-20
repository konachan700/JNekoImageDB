package ui.imagelist;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import proto.UseServices;
import proto.CryptographyService;
import proto.LocalStorageService;
import ui.imageview.BaseImageView;
import ui.imageview.FileImageView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public abstract class FileImageDashboard extends AbstractImageDashboard<FileImageView> implements UseServices {
    private CryptographyService cryptographyService;
    private LocalStorageService localStorageService;

    private final ArrayList<FileImageView> imageViews = new ArrayList<>();
    private final CopyOnWriteArrayList<Path> files = new CopyOnWriteArrayList<>();
    private final Set<Path> selectedFiles = new HashSet<>();

    private static final Set<String> allowedExtentions = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".jpe", ".png"));

    public abstract void onPageChanged(int page);
    public abstract void onPageCountChanged(int pages);

    private final ImageView fileImageView = new ImageView();

    @Override
    void onResizeCompleted() {
        //localStorageService.resetCache();
        pageChanged(getCurrentPage());
    }

    private byte[] requestImg(int index) {
        if (index >= files.size() || index < 0) return null;
        try {
            return Files.readAllBytes(files.get(index));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    FileImageView imageViewRequest(int number) {
        if (imageViews.size() == number) {
            final FileImageView f = new FileImageView() {
                @Override
                public void onClick(MouseEvent event, int index, BaseImageView object) {
                    switch (event.getButton()) {
                        case PRIMARY:
                            final byte[] binary = requestImg(index);
                            if (binary == null) return;
                            showOrHideImage(binary);
                            break;
                        case SECONDARY:
                            final Path path = files.get(index);
                            if (path == null) return;

                            inverseSelection();
                            if (isSelected()) {
                                getSelectedFiles().add(path);
                            } else {
                                getSelectedFiles().remove(path);
                            }
                            break;
                    }
                }

                @Override
                public byte[] requestItem(int index) {
                    return requestImg(index);
                }
            };
            imageViews.add(f);
        }
        return imageViews.get(number);
    }

    private void showOrHideImage(byte[] binary) {
        if (!getRootPane().getChildren().contains(fileImageView) && binary != null) {
            final Image image = new Image(new ByteArrayInputStream(binary));
            fileImageView.setImage(image);
            getRootPane().getChildren().addAll(fileImageView);
            setBlur(getRootVBox());
        } else {
            removeBlur(getRootVBox());
        }
    }

    @Override
    public void pageChanged(int page) {
        reloadPage(page);
        onPageChanged(page);
    }

    private void reloadPage(int page) {
        if (page < 0) return;
        if ((page * getElementsPerPage()) > files.size()) return;

        for (int i=0; i<imageViews.size(); i++) {
            final int globalIndex = i + (page * getElementsPerPage());
            if (globalIndex >= files.size()) {
                imageViews.get(i).setSelected(false);
                imageViews.get(i).delImage();
            } else {
                final Path p = files.get(globalIndex);
                final boolean isSelected = getSelectedFiles().contains(p);
                imageViews.get(i).setSelected(isSelected);
                imageViews.get(i).setImage(globalIndex, page);
            }
        }
    }

    public void cd(Path dir) {
        if (cryptographyService == null || localStorageService == null) throw new IllegalStateException("Call init() before use");

        if (dir == null) return;
        files.clear();

        final List<File> lfiles = Optional.ofNullable(dir)
                .map(Path::toAbsolutePath)
                .map(Path::toFile)
                .map(File::listFiles)
                .map(Arrays::asList)
                .orElse(Collections.EMPTY_LIST);

        final List<Path> imagesList = lfiles.parallelStream()
                .filter(File::isFile)
                .filter(file -> allowedExtentions.stream().filter(name -> file.getName().toLowerCase().endsWith(name)).count() > 0)
                .map(File::toPath)
                .collect(Collectors.toList());

        imagesList.sort(Comparator.comparing(a -> a.toFile().getName()));
        files.addAll(imagesList);

        final int pagesTail = files.size() % getElementsPerPage();
        final int pagesWoTail = files.size() - pagesTail;
        final int pagesCount = (pagesWoTail / getElementsPerPage());// + (pagesTail != 0 ? 1 : 0);

        setPagesCount(pagesCount);
        reloadPage(0);
        onPageCountChanged(pagesCount);
    }

    public void init() {
        this.cryptographyService = getService(CryptographyService.class);
        this.localStorageService = getService(LocalStorageService.class);
    }

    public FileImageDashboard() {
        fileImageView.getStyleClass().addAll("image_view_file_list");

        //getRootPane().setOnMouseClicked(e -> showOrHideImage(null));
        fileImageView.setOnMouseClicked(e -> showOrHideImage(null));

        fileImageView.setPreserveRatio(true);
        fileImageView.setSmooth(true);
        fileImageView.setCache(false);
        fileImageView.fitWidthProperty().bind(getRootPane().widthProperty().divide(1.25));
        fileImageView.fitHeightProperty().bind(getRootPane().heightProperty().divide(1.25));
    }

    public void setBlur(Pane p) {
        final ColorAdjust adj = new ColorAdjust(0, -0.9, 0.5, 0);
        final GaussianBlur blur = new GaussianBlur(17);
        adj.setInput(blur);
        p.setEffect(adj);
    }

    public void removeBlur(Pane p) {
        getRootPane().getChildren().remove(fileImageView);
        p.setEffect(null);
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
}
