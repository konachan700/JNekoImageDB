package jnekoimagesdb.ui.controls;

import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.core.img.XImgFS;
import jnekoimagesdb.core.img.XImgFSActionListener;
import jnekoimagesdb.core.img.XImgImages;
import jnekoimagesdb.core.img.XImgPreviewGen;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.controls.dialogs.DialogFSImageView;
import jnekoimagesdb.ui.controls.elements.EFileListItem;
import jnekoimagesdb.ui.controls.elements.EFileListItemActionListener;
import jnekoimagesdb.ui.controls.elements.GUIElements;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import jnekoimagesdb.ui.GUITools;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagedFileList extends SEVBox {
    private final Logger 
                logger = LoggerFactory.getLogger(FileListGenerator.class);
    
    private final Object
            syncObject = new Object();
    
    private volatile int
            busyCounter = 0;
    
    private class FileListGenerator implements Runnable {
        private final Logger 
                logger2 = LoggerFactory.getLogger(FileListGenerator.class);

        private final XImgImages
                imgConv = new XImgImages();
        
        private final ArrayList<DSImage>
                addedImages = new ArrayList<>();
        
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            Platform.runLater(() -> { pflal.onThreadStart(this.hashCode()); });
            if (XImg.getPSizes().getPrimaryPreviewSize() == null) {
                while (XImg.getPSizes().getPrimaryPreviewSize() == null) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ex) {
                        logger2.info(ex.getMessage());
                        return;
                    }
                }
            }
            
            imgConv.setPreviewSize((int) XImg.getPSizes().getPrimaryPreviewSize().getWidth(), 
                    (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight(), XImg.getPSizes().getPrimaryPreviewSize().isSquared());
            
            while(true) {
                    Platform.runLater(() -> { pflal.onThreadPause(this.hashCode(), true, 0, null); });
                    
                    final Path element = prevGenDeque.pollLast();
                    if (isExit) return;
                    
                    if (element != null) {
                        progressCounter++;
                        Platform.runLater(() -> { pflal.onThreadPause(this.hashCode(), false, progressCounter, element); });
                        
                        byte[] md5e = null;
                        XImgPreviewGen.PreviewElement peDB;
                        try {
                                md5e = XImgDatastore.getFilePartMD5(element.toAbsolutePath().toString());
                                peDB = XImgDatastore.readCacheEntry(md5e);
                                final Image im = peDB.getImage(XImg.getCrypt(), XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
                                if (im != null) 
                                    setImage (im, element); 
                        } catch (IOException | ClassNotFoundException ex) {
                            if ((md5e != null) && (imgConv.isImage(element.toAbsolutePath().toString()))) {
                                final byte preview[];
                                try {
                                    preview = imgConv.getPreviewFS(element.toAbsolutePath().toString());
                                    final byte previewCrypted[] = XImg.getCrypt().crypt(preview);
                                    peDB = new XImgPreviewGen.PreviewElement();
                                    peDB.setPath(element);
                                    peDB.setMD5(md5e);
                                    peDB.setCryptedImageBytes(previewCrypted, XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
                                    
                                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    final ObjectOutputStream oos = new ObjectOutputStream(baos);
                                    oos.writeObject(peDB);
                                    oos.flush();

                                    final byte[] crypted = XImg.getCrypt().crypt(baos.toByteArray());
                                    if (crypted == null) throw new IOException("Crypt() return null;");
                                    
                                    synchronized(XImg.getDB(XImg.PreviewType.cache)) {
                                        final DB db = XImg.getDB(XImg.PreviewType.cache);
                                        db.put(md5e, crypted);
                                    }
                                    
                                    final Image im = peDB.getImage(XImg.getCrypt(), XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
                                    if (im != null) 
                                        setImage (im, element); 
                                } catch (IOException ex1) {
                                    busyCounter--;
                                    logger2.info(ex1.getMessage());
                                }
                            } else
                                busyCounter--;
                        }
                    } else {
                        final Path fileToMove = insertToDBDeque.pollLast();
                        if (fileToMove != null) {
                            Platform.runLater(() -> { pflal.onThreadPause(this.hashCode(), false, progressCounter, fileToMove); });
                            progressCounter++;
                            if (imgConv.isImage(fileToMove.toAbsolutePath().toString())) {
                                try {
                                    final byte[] md5 = XImgDatastore.pushFile(fileToMove.toAbsolutePath());
                                    DSImage dsi = new DSImage(md5);
                                    dsi.setImageFileName(fileToMove.toFile().getName());
                                    addedImages.add(dsi);
                                    logger2.debug("WRITE TO STORE: "+fileToMove.toAbsolutePath().toString());
                                } catch (Exception ex) {
                                    logger2.warn("File ["+fileToMove.getFileName().toString()+"] already exist;");
                                }
                            }
                        } else {
                            if (!addedImages.isEmpty()) {
                                Platform.runLater(() -> { pflal.onThreadPause(this.hashCode(), false, 0, null); });
                                
                                final DSAlbum ds;
                                Session hibSession = HibernateUtil.getNewSession();
                                if (curentAlbumID > 0) {
                                    List<DSAlbum> list = hibSession
                                            .createCriteria(DSAlbum.class)
                                            .add(Restrictions.eq("albumID", curentAlbumID))
                                            .list();

                                    if (list.size() > 0) 
                                        ds = list.get(0);
                                    else 
                                        ds = null;
                                } else {
                                   ds = null;
                                }
                                
                                HibernateUtil.beginTransaction(hibSession);
                                addedImages.forEach(c -> {
                                    hibSession.save(c);
                                    progressCounter++;
                                });

                                if (ds != null) {
                                    if (ds.getImages() != null)
                                        ds.getImages().addAll(addedImages);
                                    else 
                                        ds.setImages(new HashSet<>(addedImages));
                                    hibSession.save(ds);
                                }
                                
                                HibernateUtil.commitTransaction(hibSession);
                                hibSession.close();
                                addedImages.clear();
                                System.gc();
                                logger2.debug("Image saving completed!");
                                Platform.runLater(() -> { pflal.onThreadPause(this.hashCode(), true, 0, null); });
                            } else {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException ex) { logger2.info(ex.getMessage()); }
                            }
                        }
                    }
            }
        }
        
        private void setImage(Image img, Path p) {
            Platform.runLater(() -> { 
                elements.forEach(c -> {
                        if (c.equals(p)) { 
                            c.setImage(img);
                            busyCounter--;
                        }
                });
            });
        }
    }
    
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
    
    private volatile long 
            curentAlbumID = 0;
    
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
//            currentPage = 0,
            pagesCount = 0,
            threadsCount = 3;
    
    private volatile boolean
            isNotInit = true, 
            isResized = false, 
            isRoot = false, 
            isExit = false;
    
    private final ToolsPanelBottom
            pathPanel = new ToolsPanelBottom();
    
    private final FlowPane
            container = new FlowPane();
    
    private final Set<EFileListItem>
            elements = new LinkedHashSet<>();
    
    private final Set<Path>
            selectedElements = new HashSet<>();
    
    private ExecutorService 
            previewGenService = null;
    
    private volatile PagedFileListActionListener
            pflal = null;
        
    private ArrayList<Path>
            currentFileList = null;
    
    private final DialogFSImageView
            imageViewer = new DialogFSImageView();
    
    private final LinkedBlockingDeque<Path>
            prevGenDeque = new LinkedBlockingDeque<>(), 
            insertToDBDeque = new LinkedBlockingDeque<>();
    
    private final EFileListItemActionListener
            itemListener = new EFileListItemActionListener() {
                @Override
                public void OnSelect(boolean isSelected, Path itemPath) {
                    if (isSelected) selectedElements.add(itemPath); else selectedElements.remove(itemPath);
                }

                @Override
                public void OnOpen(Path itemPath) {
                    if (Files.isDirectory(itemPath)) {
                        fileSystemParser.setPath(itemPath);
                        SettingsUtil.setPath("currentPath", itemPath);
                        fileSystemParser.getFiles();
                    } else {
                        int counter, listSize = currentFileList.size();
                        for (counter=0; counter<listSize; counter++) {
                            if (Files.isRegularFile(currentFileList.get(counter))) break;
                        }

                        imageViewer.setFiles(new ArrayList<>(currentFileList.subList(counter, listSize)));
                        imageViewer.show();
                    }
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
                            currentFileList = pList;
                            
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
                            SettingsUtil.setPath("currentPath", p);
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
    
    private final Set<Future<FileListGenerator>> 
            threads = new HashSet<>();
    
    private volatile int 
            progressCounter = 0;
    
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
    
    public PagedFileList(PagedFileListActionListener _pflal) {
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
            if (busyCounter != 0) return;
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
    
    public boolean isBusy() {
        return (busyCounter != 0);
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
                    efl.setImage(GUIElements.ITEM_LOADING);
                    efl.setSelected(selectedElements.contains(p));
                    prevGenDeque.addLast(p); 
                    busyCounter++;
                    if (prevGenDeque.size() > (itemTotalCount * 2)) {
                        prevGenDeque.removeFirst();
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
    
    public void addAll() {
        insertToDBDeque.addAll(fileSystemParser.getFullList());
    }
    
    public void addSelected() {
        insertToDBDeque.addAll(selectedElements);
    }
    
    public void selectNone() {
        selectedElements.clear();
        regenerateItemList();
    }
    
    public void selectAll() {
        selectedElements.addAll(fileSystemParser.getFullList());
        regenerateItemList();
    }
    
    public void setAlbumID(long _curentAlbumID) {
        curentAlbumID = _curentAlbumID;
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
    
    public void pageSet(int page) {
        if (isNotInit) return;
        if ((currentPage >= 0) && (currentPage <= pagesCount)) {
            currentPage = page;
            regenerateItemList();
        }
    }
    
    public void init() {
        if (!isNotInit) {
            logger.warn("Double class init.");
            return;
        }
        
        fileSystemParser.init();
        threadsCount = SettingsUtil.getInt("previewFSCacheThreadsCount.value", 3);
        
        resizeTimer.setCycleCount(Animation.INDEFINITE);
        resizeTimer.play();
        
        previewGenService = Executors.newFixedThreadPool(threadsCount);
        
        for (int i=0; i<threadsCount; i++) {
           final Future f = previewGenService.submit(new FileListGenerator());
           threads.add(f);
        }
        
        isNotInit = false;
    }
    
    public void dispose() {
        if (isNotInit) return;
        fileSystemParser.dispose();
        isExit = true;
        previewGenService.shutdownNow();
        isNotInit = true;
    }
}
