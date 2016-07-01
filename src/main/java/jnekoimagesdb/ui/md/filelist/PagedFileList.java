package jnekoimagesdb.ui.md.filelist;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
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
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.md.controls.PathTextField;
import org.slf4j.LoggerFactory;

public class PagedFileList extends VBox implements PagedFileListElementActionListener, XImgFSActionListener {
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(PagedFileList.class);

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
            isRoot = false;
    
    private final FlowPane
            container = new FlowPane();
    
    private final Set<PagedFileListElement>
            elements = new LinkedHashSet<>();
    
    private Path
            selectedElement = null;
    
    private PagedFileListFullActionListener
            currActionListener = null;
    
    private final XImgFS
            fileSystemParser = new XImgFS(this);
    
    private final HBox
            pathPanel = new HBox();
        
    private final PathTextField
            pathText = new PathTextField("main_window_good_input", "main_window_badly_input");
    
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

        this.getStyleClass().addAll("pil_root_sp_pane");
        
        container.getStyleClass().addAll("pil_root_pane", "pil_max_width", "pil_max_height");
        container.setAlignment(Pos.CENTER);
        container.setHgap(spacerSize);
        container.setVgap(spacerSize);
        
        this.getChildren().addAll(
                pathPanel, 
                container
        );
    }

    private void setPath(String p) {
        fileSystemParser.setPath(p);
        fileSystemParser.getFiles();
        pathText.setText(fileSystemParser.getCurrentPath().toAbsolutePath().toString());
    }
    
    private void regenerateItemList() {
        int counter = currentPage * itemTotalCount, ncounter = 0;

        final List<Path> filelist = fileSystemParser.getPage(itemTotalCount, counter);
        for (PagedFileListElement efl : elements) {
            Path p;
            try {
                p = filelist.get(ncounter);
                efl.setPath(p);
                efl.setName((isRoot) ? p.toString() : p.toFile().getName());
                if (Files.isDirectory(p)) {
                    efl.setImage(GUITools.loadIcon("dir-normal-128"));
                } else {
                    efl.setImage(GUITools.loadIcon("unknown-file-128"));
                    if (selectedElement != null) 
                        efl.setSelected(selectedElement.equals(p));
                    else 
                        efl.setSelected(false);
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
            final PagedFileListElement efl = new PagedFileListElement(this);
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
        if (isSelected) selectedElement = itemPath; else selectedElement = null;
    }

    @Override
    public void OnOpen(Path itemPath) {
        if (Files.isDirectory(itemPath)) {
            fileSystemParser.setPath(itemPath);
            SettingsUtil.setPath("currentNPPath", itemPath);
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
        SettingsUtil.setPath("currentNPPath", p);
    }

    @Override
    public void onError(XImgFS.XImgFSActions act, Exception e) {
        if ((act == XImgFS.XImgFSActions.levelUp) && (!isRoot)) {
            fileSystemParser.getRoots();
        }

        System.out.println("ZZ ERROR: "+e.getClass().getName());
    }
}
