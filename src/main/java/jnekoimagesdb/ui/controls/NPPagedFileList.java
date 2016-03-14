package jnekoimagesdb.ui.controls;

import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgFS;
import jnekoimagesdb.core.img.XImgFSActionListener;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.controls.dialogs.DialogFSImageView;
import jnekoimagesdb.ui.controls.elements.EFileListItem;
import jnekoimagesdb.ui.controls.elements.EFileListItemActionListener;
import jnekoimagesdb.ui.controls.elements.GUIElements;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import jnekoimagesdb.ui.GUITools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NPPagedFileList extends SEVBox {
    private final Logger 
                logger = LoggerFactory.getLogger(NPPagedFileList.class);
    
    public static final int
            BTN_LELEL_UP    = 1,
            BTN_NAVTO       = 2,
            BTN_NAVTO_ROOT  = 3,
            BTN_SELALL      = 4,
            BTN_SELNONE     = 5,
            BTN_DEL         = 6, 
            BTN_ADD         = 7,
            TIMER_DELAY     = 33;
    
    public static final String
            FIELD_PATH = "np_path";
    
    public static final Image 
            IMG24_LEVEL_UP          = GUITools.loadIcon("lvlup-24"),
            IMG24_NAVIGATE_TO       = GUITools.loadIcon("navto-24"),
            IMG24_TO_ROOT           = GUITools.loadIcon("root-24");
    
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
    
    private final ToolsPanelBottom
            pathPanel = new ToolsPanelBottom();
    
    private final FlowPane
            container = new FlowPane();
    
    private final Set<EFileListItem>
            elements = new LinkedHashSet<>();
    
    private Path
            selectedElement = null;
    
    private volatile PagedFileListActionListener
            pflal = null;
    
    private final EFileListItemActionListener
            itemListener = new EFileListItemActionListener() {
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
                    }// else {
                        // TODO: for future use
                    //}
                }
            };
    
    private final XImgFS
            fileSystemParser = new XImgFS(
                    new XImgFSActionListener() {
                        @Override
                        public void rootListGenerated(Set<Path> pList) {
                            isRoot = true;
                            filesCount = pList.size();
                            currentPage = 0;
                            if (itemTotalCount > 0) {
                                pagesCount = (filesCount / itemTotalCount) + (((filesCount % itemTotalCount) == 0) ? 0 : 1);
                                pflal.onPageCountChange(pagesCount);
                                regenerateItemList();
                            }
                            
                            pathPanel.getTextField(FIELD_PATH).setText("");
                        }

                        @Override
                        public void fileListRefreshed(Path p, ArrayList<Path> pList, long execTime) {
                            isRoot = false;
                            filesCount = pList.size();
                            currentPage = 0;
                            //currentFileList = pList;
                            
                            if (itemTotalCount > 0) {
                                pagesCount = (filesCount / itemTotalCount) + (((filesCount % itemTotalCount) == 0) ? 0 : 1);
                                pflal.onPageCountChange(pagesCount); 
                                regenerateItemList();
                            }
                            
                            pathPanel.getTextField(FIELD_PATH).setText(p.toAbsolutePath().toString());
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

                        @Override
                        public void onProgress(long tid, long counter) {
                            pflal.onThreadProgress(tid, counter);
                        }

                        @Override
                        public void onThreadStart(long tid) {
                            pflal.onThreadStart(tid);
                        }

                        @Override
                        public void onThreadPause(long tid, boolean pause) {
                            pflal.onThreadPause(tid, pause, 0, null); 
                        }
                    }
            );
    
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
    
    public NPPagedFileList(PagedFileListActionListener _pflal) {
        super(0);
        pflal = _pflal;
        
        GUITools.setMaxSize(pathPanel, 9999, 24);
        pathPanel.setAL((index) -> {
            if (isNotInit) return;
            
            switch (index) {
                case BTN_NAVTO:
                    setPath(pathPanel.getTextField(FIELD_PATH).getText().trim());
                    pathPanel.getTextField(FIELD_PATH).setText(fileSystemParser.getCurrentPath().toAbsolutePath().toString());
                    break;
            }
        });
        
        this.setOnScroll((ScrollEvent event) -> {
            if (pagesCount > 0) {
                if (event.getDeltaY() > 0) {
                    pagePrev();
                    pflal.onPageChange(currentPage);
                } else {
                    pageNext();
                    pflal.onPageChange(currentPage);
                }
            }
        });
        
        pathPanel.addFixedSeparator();
        pathPanel.addTextField(FIELD_PATH);
        pathPanel.addButton(BTN_NAVTO, IMG24_NAVIGATE_TO);
            
        container.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(container, 9999, 9999);
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

        this.getChildren().addAll(
                pathPanel,
                container
        );
    }

    private void setPath(String p) {
        fileSystemParser.setPath(p);
        fileSystemParser.getFiles();
        pathPanel.getTextField(FIELD_PATH).setText(fileSystemParser.getCurrentPath().toAbsolutePath().toString());
    }
    
    private void regenerateItemList() {
        int counter = currentPage * itemTotalCount, ncounter = 0;

        final List<Path> filelist = fileSystemParser.getPage(itemTotalCount, counter);
        for (EFileListItem efl : elements) {
            Path p;
            try {
                p = filelist.get(ncounter);
                efl.setPath(p);
                efl.setName((isRoot) ? p.toString() : p.toFile().getName());
                if (Files.isDirectory(p)) {
                    efl.setImage(GUIElements.ICON_DIR);
                } else {
                    efl.setImage(GUIElements.ITEM_UNKNOWN);
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
        pflal.onPageCountChange(pagesCount); 
        
        elements.clear();
        for (int i=0; i<itemTotalCount; i++) {
            final EFileListItem efl = new EFileListItem(itemListener);
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
//        pathPanel.getTextField(FIELD_PATH).setText(fileSystemParser.getCurrentPath().toAbsolutePath().toString());
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
}
