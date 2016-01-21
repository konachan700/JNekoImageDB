package img.gui;

import img.XImg;
import img.XImgFS;
import img.XImgFSActionListener;
import img.gui.dialogs.DialogFSImageView;
import img.gui.elements.EFileListItem;
import img.gui.elements.EFileListItemActionListener;
import img.gui.elements.SEVBox;
import img.gui.elements.SScrollPane;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import jnekoimagesdb.GUITools;

public class PagedFileList extends SEVBox {
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
            FIELD_PATH = "path";
    
    public static final Image 
            IMG24_LEVEL_UP          = GUITools.loadIcon("lvlup-24"),
            IMG24_NAVIGATE_TO       = GUITools.loadIcon("navto-24"),
            IMG24_TO_ROOT           = GUITools.loadIcon("root-24");
    
    private int 
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
            currentPage = 0,
            pagesCount = 0;
    
    private volatile boolean
            isNotInit = true, 
            isResized = false, 
            isRoot = false;
    
    private final ToolsPanelBottom
            pathPanel = new ToolsPanelBottom();
    
    private final FlowPane
            container = new FlowPane();
    
    private final SScrollPane
            scrollableContainer = new SScrollPane();
    
    private final Set<EFileListItem>
            elements = new HashSet<>();
    
    private final Set<Path>
            selectedElements = new HashSet<>();
    
    private ArrayList<Path>
            currentFileList = null;
    
    private final DialogFSImageView
            imageViewer = new DialogFSImageView();
    
    private final LinkedBlockingDeque<Path>
            prevGenDeque = new LinkedBlockingDeque<>();
    
    private final EFileListItemActionListener
            itemListener = new EFileListItemActionListener() {
                @Override
                public void OnSelect(boolean isSelected, Path itemPath) {
                    if (isSelected) selectedElements.add(itemPath); else selectedElements.remove(itemPath);
                }

                @Override
                public void OnOpen(Path itemPath) {
                    int counter, listSize = currentFileList.size();
                    for (counter=0; counter<listSize; counter++) {
                        if (Files.isRegularFile(currentFileList.get(counter))) break;
                    }
                    
                    imageViewer.setFiles(new ArrayList<>(currentFileList.subList(counter, listSize)));
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
                            pagesCount = (filesCount / itemTotalCount) + (((filesCount % itemTotalCount) == 0) ? 0 : 1);
                            regenerateItemList();
                        }

                        @Override
                        public void fileListRefreshed(ArrayList<Path> pList, long execTime) {
                            isRoot = false;
                            filesCount = pList.size();
                            currentPage = 0;
                            pagesCount = (filesCount / itemTotalCount) + (((filesCount % itemTotalCount) == 0) ? 0 : 1);
                            currentFileList = pList;
                            regenerateItemList();
                        }

                        @Override
                        public void onLevelUp(Path p) {
                            fileSystemParser.getFiles();
                        }

                        @Override
                        public void onProgress(long counter) {
                            
                        }

                        @Override
                        public void onError(XImgFS.XImgFSActions act, Exception e) {
                            if ((act == XImgFS.XImgFSActions.levelUp) && (!isRoot)) {
                                fileSystemParser.getRoots();
                            }
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

    public PagedFileList() {
        super(0);
        pathPanel.setAL((index) -> {
            if (isNotInit) return;
            
            pathPanel.addTextField(FIELD_PATH);
            pathPanel.addButton(BTN_NAVTO, IMG24_NAVIGATE_TO);
            switch (index) {
                case BTN_NAVTO:
                    setPath(pathPanel.getTextField(FIELD_PATH).getText().trim());
                    break;
            }
        });
        
        container.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(container, 9999, 9999);
        container.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            myHeight = newValue.intValue();
            container.getChildren().clear();
            timerCounter = TIMER_DELAY;
        });
        container.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            myWidth = newValue.intValue();
            container.getChildren().clear();
            timerCounter = TIMER_DELAY;
        });
        
        scrollableContainer.setFitToHeight(true);
        scrollableContainer.setFitToWidth(true);
        scrollableContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollableContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollableContainer.setContent(container);
        
        this.getChildren().addAll(
                pathPanel,
                scrollableContainer
        );
    }
    
    private void setPath(String p) {
        fileSystemParser.setPath(p);
        fileSystemParser.getFiles();
    }
    
    private void regenerateItemList() {
        int counter = currentPage * itemTotalCount, ncounter = 0;
        if (isRoot) {
            
        } else {
            final List<Path> filelist = fileSystemParser.getPage(itemTotalCount, counter);
            for (EFileListItem efl : elements) {
                Path p;
                try {
                    p = filelist.get(ncounter);
                    efl.setName(p.toFile().getName());
                } catch (IndexOutOfBoundsException e) {
                    efl.setNullImage();
                }

                counter++;
                ncounter++;
            }
        }
    }
    
    public final void resize() {
        if (isNotInit) return;
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        
        itemWidth = (int) XImg.getPSizes().getPrimaryPreviewSize().getWidth();
        itemHeight = (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight();
        
        container.setVgap(spacerSize);
        container.setHgap(spacerSize);
        
        itemCountOnRow = myWidth / (itemWidth + spacerSize);
        itemCountOnColoumn = myHeight / (itemHeight + spacerSize);
        itemTotalCount = itemCountOnRow * itemCountOnColoumn;
        
        elements.clear();
        for (int i=0; i<itemTotalCount; i++) {
            final EFileListItem efl = new EFileListItem(itemListener);
            efl.setSize();
            efl.setNullImage();
            elements.add(efl);
            container.getChildren().add(efl);
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
    
    public void levelUp() {
        if (isNotInit) return;
        fileSystemParser.levelUp();
    }
    
    public void pageNext() {
        if (isNotInit) return;
        if (currentPage < pagesCount) {
            currentPage++;
            regenerateItemList();
        }
    }
    
    public void pagePrev() {
        if (isNotInit) return;
        if (currentPage > 0) {
            currentPage--;
            regenerateItemList();
        }
    }
    
    public void pageSet(int page) {
        if (isNotInit) return;
        if ((currentPage >= 0) && (currentPage <= pagesCount)) {
            currentPage = page;
            regenerateItemList();
        }
    }
    
    public void init() {
        if (!isNotInit) return;
        fileSystemParser.init();
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
