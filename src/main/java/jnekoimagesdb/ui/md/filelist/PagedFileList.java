package jnekoimagesdb.ui.md.filelist;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgFS;
import jnekoimagesdb.core.img.XImgFSActionListener;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.md.controls.LabeledBox;
import jnekoimagesdb.ui.md.controls.PathTextField;

public class PagedFileList extends VBox implements PagedFileListElementActionListener, XImgFSActionListener {
    private final org.slf4j.Logger 
            logger = org.slf4j.LoggerFactory.getLogger(PagedFileList.class);

    public static final String
            FIELD_PATH = "np_path";
    
    public static final int
            TIMER_DELAY = 33;
    
    private int 
            currentPage = 0;
    
    private volatile int 
            myWidth = 0,
            myHeight = 0,
            timerCounter = 0,
            itemCountOnRow = 0, 
            itemCountOnColoumn = 0,
            itemTotalCount = 0,
            itemHeight = 0,
            itemWidth = 0,
            spacerSize = 4,
            filesCount = 0,
            pagesCount = 0;
    
    private volatile boolean
            isNotInit = true, 
            isResized = false, 
            isRoot = false,
            multiSelect = false;
    
    private final ScrollPane
            fContainer = new ScrollPane();
    
    private final FlowPane
            container = new FlowPane();
    
    private final Set<PagedFileListElementDirFile>
            elements = new LinkedHashSet<>();
    
    private Path
            selectedElement = null;
    
    private final Set<Path>
            selectedElements = new HashSet<>();
    
    private PagedFileListFullActionListener
            currActionListener = null;
    
    private final XImgFS
            fileSystemParser = new XImgFS(this);
    
    private final HBox
            pathPanel = new HBox(),
            nullStylePanel = new HBox();
        
    private final PathTextField
            pathText = new PathTextField("main_window_good_input", "main_window_badly_input");
    
    private final TextField
            fileNameField = new TextField();
    
    private final LabeledBox 
            tfNameBox;
    
    private final Timeline resizeTimer = new Timeline(new KeyFrame(Duration.millis(10), ae -> {
        if (timerCounter > 0) {
            timerCounter--;
            isResized = true;
        } else {
            if (isResized) {
                resize();
                isResized = false;
            }
        }
    }));
    
    public PagedFileList(PagedFileListFullActionListener al) {
        super();
        currActionListener = al;
        
        this.setOnScroll((ScrollEvent event) -> {
            if (pagesCount > 0) {
                if (event.getDeltaY() > 0) {
                    pagePrev();
                    currActionListener.onPageChange(currentPage);
                } else {
                    pageNext();
                    currActionListener.onPageChange(currentPage);
                }
            }
        });
        
        container.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            myHeight = newValue.intValue();
            // В этом месте выкидывает ошибку в консоль, почему, до конца не разобрался, вероятнее всего из-за иземения объектов внутри родителя и переопределенного equals() у оных.
            // Сделал заглушку, чтобы не гадило в консоль ошибкой.
            try { container.getChildren().clear(); } catch (java.lang.IllegalArgumentException e) {}
            timerCounter = TIMER_DELAY;
        });
        container.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            myWidth = newValue.intValue();
            try { container.getChildren().clear(); } catch (java.lang.IllegalArgumentException e) {}
            timerCounter = TIMER_DELAY;
        });
        
        fileNameField.setAlignment(Pos.CENTER_LEFT);
        fileNameField.getStyleClass().addAll("pil_path_panel", "pil_max_width", "path_panel_height");
        
        tfNameBox = new LabeledBox("Имя файла", fileNameField, "main_window_max_width", "main_window_null_pane", "main_window_labeled_box_filename_in_dialog");
        
        pathPanel.setAlignment(Pos.CENTER_LEFT);
        pathPanel.getStyleClass().addAll("pil_item_null_pane", "pil_max_width", "path_panel_height");
        pathPanel.getChildren().add(pathText);
        
        pathText.setAlignment(Pos.CENTER_LEFT);
        pathText.getStyleClass().addAll("pil_path_panel", "pil_max_width", "path_panel_height");
        pathText.setOnKeyPressed((KeyEvent key) -> {
            if (key.getCode() == KeyCode.ENTER) {
                setPath(pathText.getText().trim());
                pathText.setText(fileSystemParser.getCurrentPath().toAbsolutePath().toString());
            }
        });

        this.getStyleClass().addAll("pil_item_root_vbox_pane", "pil_max_width", "pil_max_height");
        nullStylePanel.getStyleClass().addAll("pil_item_icon_colors");
        
        container.getStyleClass().addAll("pil_root_pane", "pil_max_width", "pil_max_height");
        container.setAlignment(Pos.CENTER);
        container.setHgap(spacerSize);
        container.setVgap(spacerSize);
        
        fContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        fContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        fContainer.setFitToHeight(true);
        fContainer.setFitToWidth(true);
        fContainer.setContent(container);
        this.getStyleClass().addAll("pil_root_sp_pane");
        fContainer.setContent(container);
        
        this.getChildren().addAll(
                pathPanel, 
                fContainer,
                tfNameBox
        );
    }

    public void enableMultiSelect(boolean enable) {
        multiSelect = enable;
    }
    
    public void enableFileNameInput(boolean enable) {
        if (enable) {
            if (!this.getChildren().contains(tfNameBox)) this.getChildren().add(tfNameBox);
        } else {
            if (this.getChildren().contains(tfNameBox)) this.getChildren().remove(tfNameBox);
        }
    }
    
    public void setDefaultFileName(String name) {
        fileNameField.setText(name);
    }
    
    public String getFileName() {
        return fileNameField.getText();
    }
    
    private void setPath(String p) {
        fileSystemParser.setPath(p);
        fileSystemParser.getFiles();
        pathText.setText(fileSystemParser.getCurrentPath().toAbsolutePath().toString());
    }
    
    private void regenerateItemList() {
        int counter = currentPage * itemTotalCount, ncounter = 0;

        final List<Path> filelist = fileSystemParser.getPage(itemTotalCount, counter);
        for (PagedFileListElementDirFile efl : elements) {
            Path p;
            try {
                p = filelist.get(ncounter);
                efl.setPath(p);
                efl.setName((isRoot) ? p.toString() : p.toFile().getName());
                if (Files.isDirectory(p)) {
                    efl.setDir();
                } else {
                    efl.setFile();
                    if (multiSelect) {
                        selectedElements.forEach(el -> {
                            efl.setSelected(efl.equals(el));
                        });
                    } else {
                        if (selectedElement != null) 
                            efl.setSelected(selectedElement.equals(p));
                        else 
                            efl.setSelected(false);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                efl.setNullImage();
            }

            counter++;
            ncounter++;
        }
    }
    
    public final synchronized void resize() {
        if (isNotInit) return;
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        
        itemWidth = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
        itemHeight = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
        
        container.setVgap(spacerSize);
        container.setHgap(spacerSize);
        
        itemCountOnRow = (myWidth + spacerSize) / (itemWidth + spacerSize);
        itemCountOnColoumn = (myHeight + spacerSize) / (itemHeight + spacerSize);
        itemTotalCount = itemCountOnRow * itemCountOnColoumn;
        
        logger.info("itemCountOnRow="+itemCountOnRow+"; itemCountOnColoumn="+itemCountOnColoumn+"; itemTotalCount="+itemTotalCount+";");
        
        if (itemTotalCount <= 0) return;
        pagesCount = (filesCount / itemTotalCount) + (((filesCount % itemTotalCount) == 0) ? 0 : 1);
        currActionListener.onPageCountChange(pagesCount); 
        
        elements.clear();
        for (int i=0; i<itemTotalCount; i++) {
            final PagedFileListElementDirFile efl = new PagedFileListElementDirFile(this);
            efl.setSize();
            efl.setNullImage();
            elements.add(efl);
            container.getChildren().addAll(efl);
        }
        
        regenerateItemList();
    }
    
    public void breakCurrentOperation() {
        if (isNotInit) return;
        fileSystemParser.breakCurrentOperation();
    }
    
    public void navigateTo(String path) {
        if (isNotInit) return;
        fileSystemParser.setPath(path);
        fileSystemParser.getFiles();
    }
    
    public void navigateRoot() {
        if (isNotInit) return;
        fileSystemParser.getRoots();
    }
    
    public void levelUp() {
        if (isNotInit) return;
        fileSystemParser.levelUp();
    }
    
    public final void pageNext() {
        if (isNotInit) return;
        if (currentPage < (pagesCount-1)) {
            currentPage++;
            regenerateItemList();
        }
    }
    
    public final void pagePrev() {
        if (isNotInit) return;
        if (currentPage > 0) {
            currentPage--;
            regenerateItemList();
        }
    }
    
    public Path getSelected() {
        return selectedElement;
    }
    
    public void clearSelection() {
        elements.stream().forEach((efl) -> {
            efl.setSelected(false);
        });
        selectedElements.clear();
    }
    
    public void clearSelection(Path sel) {
        elements.stream().forEach((efl) -> {
            if (sel.equals(efl.getPath())) efl.setSelected(true); else efl.setSelected(false);
        });
        selectedElements.clear();
    }
    
    public Set<Path> getSelectedElements() {
        return selectedElements;
    }
    
    public Path getPath() {
        return fileSystemParser.getCurrentPath();
    }
    
    public void pageSet(int page) {
        if (isNotInit) return;
        if ((currentPage >= 0) && (currentPage <= pagesCount)) {
            currentPage = page;
            regenerateItemList();
        }
    }
    
    public void init(boolean dir) {
        if (!isNotInit) {
            logger.warn("Double class init.");
            return;
        }
        
        if (dir) fileSystemParser.initDir(); else fileSystemParser.init();
        
        resizeTimer.setCycleCount(Animation.INDEFINITE);
        resizeTimer.play();
        
        isNotInit = false;
    }
    
    public void dispose() {
        if (isNotInit) return;
        fileSystemParser.dispose();
        isNotInit = true;
    }
    
    @Override
    public void OnSelect(boolean isSelected, Path itemPath) {
        if (!multiSelect) 
            clearSelection(itemPath);
        else {
            if (isSelected) 
                selectedElements.add(itemPath); 
            else 
                selectedElements.remove(itemPath);
        }
        
        if (isSelected) 
            selectedElement = itemPath; 
        else 
            selectedElement = null;
        
        if (isSelected) fileNameField.setText(itemPath.getFileName().toString());
    }

    @Override
    public void OnOpen(Path itemPath) {
        if (Files.isDirectory(itemPath)) {
            fileSystemParser.setPath(itemPath);
            SettingsUtil.setPath(FIELD_PATH, itemPath);
            fileSystemParser.getFiles();
        }
    }

    @Override
    public void rootListGenerated(Set<Path> pList) {
        isRoot = true;
        filesCount = pList.size();
        currentPage = 0;
        if (itemTotalCount > 0) {
            pagesCount = (filesCount / itemTotalCount) + (((filesCount % itemTotalCount) == 0) ? 0 : 1);
            currActionListener.onPageCountChange(pagesCount);
            regenerateItemList();
        }

        pathText.setText("");
    }

    @Override
    public void fileListRefreshed(Path p, ArrayList<Path> pList, long execTime) {
        isRoot = false;
        filesCount = pList.size();
        currentPage = 0;

        if (itemTotalCount > 0) {
            pagesCount = (filesCount / itemTotalCount) + (((filesCount % itemTotalCount) == 0) ? 0 : 1);
            currActionListener.onPageCountChange(pagesCount); 
            regenerateItemList();
        }

        pathText.setText(p.toAbsolutePath().toString());
    }

    @Override
    public void onLevelUp(Path p) {
        fileSystemParser.getFiles();
        SettingsUtil.setPath(FIELD_PATH, p);
    }

    @Override
    public void onError(XImgFS.XImgFSActions act, Exception e) {
        if ((act == XImgFS.XImgFSActions.levelUp) && (!isRoot)) {
            fileSystemParser.getRoots();
        }

        logger.error("onError(): "+e.getClass().getName());
    }
}
