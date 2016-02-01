package img.gui.dialogs;

import datasources.SettingsUtil;
import img.gui.PagedFileList;
import img.gui.PagedFileListActionListener;
import img.gui.ToolsPanelTop;
import img.gui.elements.EThreadStatItem;
import img.gui.elements.SEVBox;
import img.gui.elements.SFHBox;
import img.gui.elements.SFLabel;
import img.gui.elements.SFVBox;
import img.gui.elements.SScrollPane;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import jnekoimagesdb.GUITools;

public class XImageUpload extends XDialogWindow {
    public static final Image 
            IMG64_SELECT_ALL        = GUITools.loadIcon("selectall-48"), 
            IMG64_SELECT_NONE       = GUITools.loadIcon("selectnone-48"), 
            IMG64_DELETE            = GUITools.loadIcon("delete-48"), 
            IMG64_LEVELUP           = GUITools.loadIcon("lvlup-48"),
            IMG64_GOROOT            = GUITools.loadIcon("goroot-48"),
            IMG64_ADDALL            = GUITools.loadIcon("addall-48"),
            IMG64_ADDSEL            = GUITools.loadIcon("add-to-db-48");
    
    private final int 
            BTN_SELECT_ALL = 1,
            BTN_SELECT_NONE = 2,
            BTN_DELETE = 3, 
            BTN_LEVELUP = 4, 
            BTN_GOROOT = 5, 
            BTN_ADDALL = 6,
            BTN_ADDSEL = 7;
         
    private volatile long
            albumID = 0;
    
    private final SFHBox 
            windowVBox = new SFHBox(2, 100, 9999, 100, 9999, "upload_img_root");
    
    private final SFVBox
            statBox = new SFVBox(2, 100, 9999, 100, 9999);
    
    private final SEVBox
            statCont = new SEVBox(), 
            flCont = new SEVBox(2);
               
    private final Pagination
            pag = new Pagination(); 
    
    private final Map<Long, EThreadStatItem>
            thStat = new LinkedHashMap<>();
    
    private final PagedFileListActionListener
            pflal = new PagedFileListActionListener() {
                @Override
                public void onThreadStart(long tid) {
                    final EThreadStatItem t = new EThreadStatItem((int) tid);
                    thStat.put(tid, t);
                    statBox.getChildren().add(t);
                }

                @Override
                public void onThreadPause(long tid, boolean pause, long counter, Path px) {
                    final EThreadStatItem t = thStat.get(tid);
                    if (t != null) {
                        if (pause) t.setCompleted(); else t.setInProgress((int) counter);
                    }
                }

                @Override
                public void onThreadProgress(long tid, long counter) {
                    final EThreadStatItem t = thStat.get(tid);
                    if (t != null) {
                        t.setInProgress((int) counter);
                    }
                }

                @Override
                public void onDisable(boolean dis) {
                    
                }

                @Override
                public void onPageCountChange(int pageCount) {
                    pag.setCurrentPageIndex(0);
                    pag.setPageCount((pageCount == 0) ? 1 : pageCount);
                }

                @Override
                public void onPageChange(int page) {
                    pag.setCurrentPageIndex(page);
                }
            };
    
    private final PagedFileList 
            filesList = new PagedFileList(pflal);
    
    private final ToolsPanelTop 
            panelTop = new ToolsPanelTop(c -> {
                switch (c) {
                    case BTN_LEVELUP:
                        filesList.levelUp();
                        break;
                    case BTN_GOROOT:
                        filesList.navigateRoot();
                        break;
                    case BTN_SELECT_ALL:
                        filesList.selectAll();
                        break;
                    case BTN_SELECT_NONE:
                        filesList.selectNone();
                        break;
                    case BTN_ADDALL:
                        filesList.addAll();
                        break;
                    case BTN_ADDSEL:
                        filesList.addSelected();
                        break;
                }
            });
    
    public XImageUpload() {
        super();
        this.create(panelTop, null, windowVBox, XDialogWindow.COLOR_BLACK);
        
        final SScrollPane sp = new SScrollPane();
        GUITools.setMaxSize(sp, 9999, 9999);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        sp.setContent(statBox);
        
        pag.setCurrentPageIndex(0);
        pag.setPageCount(1); 
        pag.setMaxPageIndicatorCount(150);
        pag.currentPageIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (oldValue.intValue() == newValue.intValue()) return;
            if (!filesList.isBusy()) {
                filesList.pageSet(newValue.intValue());
                //System.out.println("PAG VALUE="+newValue.intValue());
            }
        });
        
        panelTop.addButton(IMG64_LEVELUP, BTN_LEVELUP);
        panelTop.addButton(IMG64_GOROOT, BTN_GOROOT);
        panelTop.addFixedSeparator();
        panelTop.addButton(IMG64_SELECT_ALL, BTN_SELECT_ALL);
        panelTop.addButton(IMG64_SELECT_NONE, BTN_SELECT_NONE);
//        panelTop.addFixedSeparator();
//        panelTop.addButton(IMG64_DELETE, BTN_DELETE);
        panelTop.addFixedSeparator();
        panelTop.addButton(IMG64_ADDSEL, BTN_ADDSEL);
        panelTop.addButton(IMG64_ADDALL, BTN_ADDALL);       
        
        GUITools.setMaxSize(statCont, 200, 9999);
        statCont.getChildren().addAll(
                new SFLabel("Статистика потоков", 200, 9999, 24, 24, "label_darkred", "XImageUpload"),
                sp
        );
        
        GUITools.setMaxSize(filesList, 9999, 9999);
        flCont.setAlignment(Pos.CENTER);
        flCont.getChildren().addAll(
                filesList,
                pag
        );
        
        windowVBox.setAlignment(Pos.CENTER);
        windowVBox.getChildren().addAll(
                flCont,
                statCont
        );
    }
    
    public void init() {
        filesList.init();
    }
    
    @Override
    public void showModal() {
        final Path p = SettingsUtil.getPath("currentPath");
        filesList.navigateTo(p.toAbsolutePath().toString());
        super.showModal();
    }
    
    @Override
    public void show() {
        final Path p = SettingsUtil.getPath("currentPath");
        filesList.navigateTo(p.toAbsolutePath().toString());
        super.show();
    }
    
    public void dispose() {
        filesList.dispose();
    }
    
    public void setAlbumID(long id) {
        albumID = id;
        filesList.setAlbumID(albumID);
    }
}
