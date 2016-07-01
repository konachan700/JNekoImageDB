package jnekoimagesdb.ui.controls.dialogs;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.GUITools;


public class XImageUpload {
//    private volatile long
//            albumID = 0;
//    
//    private final SFHBox 
//            windowVBox = new SFHBox(2, 100, 9999, 100, 9999, "upload_img_root");
//    
//    private final SFVBox
//            statBox = new SFVBox(2, 100, 9999, 100, 9999);
//    
//    private final SEVBox
//            statCont = new SEVBox(), 
//            flCont = new SEVBox(2);
//               
//    private final Pagination
//            pag = new Pagination(); 
//    
//    private final Map<Long, EThreadStatItem>
//            thStat = new LinkedHashMap<>();
//    
//    private final PagedFileListActionListener
//            pflal = new PagedFileListActionListener() {
//                @Override
//                public void onThreadStart(long tid) {
//                    final EThreadStatItem t = new EThreadStatItem((int) tid);
//                    thStat.put(tid, t);
//                    statBox.getChildren().add(t);
//                }
//
//                @Override
//                public void onThreadPause(long tid, boolean pause, long counter, Path px) {
//                    final EThreadStatItem t = thStat.get(tid);
//                    if (t != null) {
//                        if (pause) t.setCompleted(); else t.setInProgress((int) counter);
//                    }
//                }
//
//                @Override
//                public void onThreadProgress(long tid, long counter) {
//                    final EThreadStatItem t = thStat.get(tid);
//                    if (t != null) {
//                        t.setInProgress((int) counter);
//                    }
//                }
//
//                @Override
//                public void onDisable(boolean dis) {
//                    
//                }
//
//                @Override
//                public void onPageCountChange(int pageCount) {
//                    pag.setCurrentPageIndex(0);
//                    pag.setPageCount((pageCount == 0) ? 1 : pageCount);
//                }
//
//                @Override
//                public void onPageChange(int page) {
//                    pag.setCurrentPageIndex(page);
//                }
//            };
//    
//    private final PagedFileList 
//            filesList = new PagedFileList(pflal);
//    
//    private final ToolsPanelTop
//            panelTop = new ToolsPanelTop(c -> {
//                switch (c) {
//                    case buttonOneLevelUp:
//                        filesList.levelUp();
//                        break;
//                    case buttonGoToRootDirectory:
//                        filesList.navigateRoot();
//                        break;
//                    case buttonSelectAll:
//                        filesList.selectAll();
//                        break;
//                    case buttonClearSelection:
//                        filesList.selectNone();
//                        break;
//                    case buttonAddAll:
//                        filesList.addAll();
//                        break;
//                    case buttonAddSelected:
//                        filesList.addSelected();
//                        break;
//                }
//            });
//    
//    public XImageUpload() {
//        super();
//        this.create(panelTop, null, windowVBox, XDialogWindow.COLOR_BLACK);
//        
//        final SScrollPane sp = new SScrollPane();
//        GUITools.setMaxSize(sp, 9999, 9999);
//        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
//        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        sp.setFitToHeight(true);
//        sp.setFitToWidth(true);
//        sp.setContent(statBox);
//        
//        pag.setCurrentPageIndex(0);
//        pag.setPageCount(1); 
//        pag.setMaxPageIndicatorCount(150);
//        pag.currentPageIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
//            if (oldValue.intValue() == newValue.intValue()) return;
//            if (!filesList.isBusy()) {
//                filesList.pageSet(newValue.intValue());
//                //System.out.println("PAG VALUE="+newValue.intValue());
//            }
//        });
//        
//        panelTop.addButton(GUITools.loadIcon("lvlup-48"), PanelButtonCodes.buttonOneLevelUp);
//        panelTop.addButton(GUITools.loadIcon("goroot-48"), PanelButtonCodes.buttonGoToRootDirectory);
//        panelTop.addFixedSeparator();
//        panelTop.addButton(GUITools.loadIcon("selectall-48"), PanelButtonCodes.buttonSelectAll);
//        panelTop.addButton(GUITools.loadIcon("selectnone-48"), PanelButtonCodes.buttonClearSelection);
//        panelTop.addFixedSeparator();
//        panelTop.addButton(GUITools.loadIcon("add-to-db-48"), PanelButtonCodes.buttonAddSelected);
//        panelTop.addButton(GUITools.loadIcon("addall-48"), PanelButtonCodes.buttonAddAll);       
//        
//        GUITools.setMaxSize(statCont, 200, 9999);
//        statCont.getChildren().addAll(
//                new SFLabel("Статистика потоков", 200, 9999, 24, 24, "label_darkred", "XImageUpload"),
//                sp
//        );
//        
//        GUITools.setMaxSize(filesList, 9999, 9999);
//        flCont.setAlignment(Pos.CENTER);
//        flCont.getChildren().addAll(
//                filesList,
//                pag
//        );
//        
//        windowVBox.setAlignment(Pos.CENTER);
//        windowVBox.getChildren().addAll(
//                flCont,
//                statCont
//        );
//    }
//    
//    public void init() {
//        filesList.init();
//    }
//    
//    @Override
//    public void showModal() {
//        final Path p = SettingsUtil.getPath("currentPath");
//        filesList.navigateTo(p.toAbsolutePath().toString());
//        super.showModal(); 
//    }
//    
//    @Override
//    public void show() {
//        final Path p = SettingsUtil.getPath("currentPath");
//        filesList.navigateTo(p.toAbsolutePath().toString());
//        super.show();
//    }
//    
//    public void dispose() {
//        filesList.dispose();
//    }
//    
//    public void setAlbumID(long id) {
//        albumID = id;
//        filesList.setAlbumID(albumID);
//    }
}
