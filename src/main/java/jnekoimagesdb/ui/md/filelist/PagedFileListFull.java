package jnekoimagesdb.ui.md.filelist;

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
import java.util.concurrent.LinkedBlockingDeque;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.core.img.XImgFS;
import jnekoimagesdb.core.img.XImgFSActionListener;
import jnekoimagesdb.core.img.XImgImages;
import jnekoimagesdb.core.img.XImgPreviewGen;
import jnekoimagesdb.core.threads.UPools;
import jnekoimagesdb.core.threads.UThreadWorker;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.md.controls.PathTextField;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iq80.leveldb.DB;
import org.slf4j.LoggerFactory;

public class PagedFileListFull extends VBox implements PagedFileListElementActionListener, XImgFSActionListener {
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(PagedFileListFull.class);
    
    public static final String
            FIELD_PATH = "path";
    
    public static final int
            TIMER_DELAY = 33;
        
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
            pagesCount = 0;
    
    private volatile int
            busyCounter = 0,
            progressCounter = 0;
    
    private volatile boolean
            isNotInit = true, 
            isResized = false, 
            isRoot = false;
    
    private final HBox
            pathPanel = new HBox();
        
    private final PathTextField
            pathText = new PathTextField("main_window_good_input", "main_window_badly_input");
    
    private final FlowPane
            container = new FlowPane();
    
    private final Set<PagedFileListElement>
            elements = new LinkedHashSet<>();

    private final XImgImages
            imgConv = new XImgImages();
    
    private final Set<Path>
            selectedElements = new HashSet<>();
    
    private ArrayList<Path>
            currentFileList = null;
    
    private final LinkedBlockingDeque<DSImage>
            addedImages = new LinkedBlockingDeque<>();
    
    private final LinkedBlockingDeque<Path>
            prevGenDeque = new LinkedBlockingDeque<>(), 
            insertToDBDeque = new LinkedBlockingDeque<>();
    
    private PagedFileListFullActionListener
            currActionListener = null;
    
    private final UThreadWorker
            workerFSPreviewGen = (thread) -> {
                imgConv.setPreviewSize((int) XImg.getPSizes().getPrimaryPreviewSize().getWidth(), 
                        (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight(), XImg.getPSizes().getPrimaryPreviewSize().isSquared());

                final Path element = prevGenDeque.pollLast();
                if (element != null) {
                    progressCounter++;
                    byte[] md5e = null;
                    XImgPreviewGen.PreviewElement peDB;
                    try {
                            md5e = XImgDatastore.getFilePartMD5(element.toAbsolutePath().toString());
                            peDB = XImgDatastore.readCacheEntry(md5e);
                            final Image im = peDB.getImage(XImg.getCrypt(), XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
                            if (im != null) 
                                Platform.runLater(() -> { 
                                    elements.forEach(c -> {
                                        if (c.equals(element)) { 
                                            c.setImage(im);
                                            busyCounter--;
                                        }
                                    });
                                });
                    } catch (IOException | ClassNotFoundException ex) {
                        if ((md5e != null) && (imgConv.isImage(element.toAbsolutePath().toString()))) {
                            try {
                                final byte preview[] = imgConv.getPreviewFS(element.toAbsolutePath().toString());
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
                                    Platform.runLater(() -> { 
                                        elements.forEach(c -> {
                                            if (c.equals(element)) { 
                                                c.setImage(im);
                                                busyCounter--;
                                            }
                                        });
                                    });
                            } catch (IOException ex1) {
                                busyCounter--;
                            }
                        } else
                            busyCounter--;
                    }
                    return true;
                } else {
                    return false;
                }
            },
    
            workerDownloader = (thread) -> {
                final Path fileToMove = insertToDBDeque.pollLast();
                if (fileToMove != null) {
                    try {
                        progressCounter++;
                        if (imgConv.isImage(fileToMove.toAbsolutePath().toString())) {
                            try {
                                final byte[] md5 = XImgDatastore.pushFile(fileToMove.toAbsolutePath());
                                DSImage dsi = new DSImage(md5);
                                dsi.setImageFileName(fileToMove.toFile().getName());
                                addedImages.add(dsi);
                            } catch (Exception ex) {
                                logger.warn("File ["+fileToMove.getFileName().toString()+"] already exist;");
                            }
                        }
                    } catch(Error er) {
                        logger.error("Image ["+fileToMove.getFileName().toString()+"] push failed! Error. TID:"+this.hashCode()+"; Message: "+er.getMessage());
                    }
                    return true;
                } else {
                    if (!addedImages.isEmpty()) {
                        logger.debug("Save to DB started. TID:"+this.hashCode());
                        
                        try {
                            DSAlbum ds;
                            final Session hibSession = HibernateUtil.getNewSession();
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
                            final ArrayList<DSImage> tempAll = new ArrayList<>();
                            
                            while (!addedImages.isEmpty()) {
                                final DSImage s = addedImages.pollFirst();
                                if (s != null) {
                                    tempAll.add(s);
                                    hibSession.save(s);
                                    progressCounter++;
                                }
                            }

                            if (ds != null) {
                                if (ds.getImages() != null)
                                    ds.getImages().addAll(tempAll);
                                else 
                                    ds.setImages(new HashSet<>(tempAll));
                                hibSession.save(ds);
                            }

                            HibernateUtil.commitTransaction(hibSession);
                            hibSession.close();

                            System.gc(); // Это тут действительно нужно, так как при сохранении большого объема данных 
                            //  сборщик вовремя не вычищает мусор и процесс "раздувает" до максимального размера, от чего в винде иногда вылетает окошко нехватки памяти. 
                            //  Актуально для старых ПК и виртуалок. При объеме памяти 6гб и более можно отключить.

                            logger.debug("Images saving completed! TID:"+this.hashCode());
                        } catch (HibernateException he) {
                            logger.error("Images saving failed! Hibernate error. TID:"+this.hashCode()+"; Message: "+he.getMessage());
                        } catch (Error er) {
                            logger.error("Images saving failed! Error. TID:"+this.hashCode()+"; Message: "+er.getMessage());
                        }
                        
                        return true;
                    } else {
                        return false;
                    }
                }
            };
    
    private final XImgFS
            fileSystemParser = new XImgFS(this);
    
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
    
    
    public PagedFileListFull(PagedFileListFullActionListener al) {
        super();
        currActionListener = al;
        
        this.setOnScroll((ScrollEvent event) -> {
            if (busyCounter != 0) return;
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

    public void init() {
        if (!isNotInit) {
            logger.warn("Double class init.");
            return;
        }
        
        fileSystemParser.init();
        
        UPools.addWorker(UPools.PREVIEW_POOL, workerFSPreviewGen);
        UPools.addWorker(UPools.PREVIEW_POOL, workerDownloader);

        resizeTimer.setCycleCount(Animation.INDEFINITE);
        resizeTimer.play();

        isNotInit = false;
        
        UPools.getGroup(UPools.PREVIEW_POOL).resume();
    }
    
    public void dispose() {
        if (isNotInit) return;
        fileSystemParser.dispose();
        isNotInit = true;
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
            final PagedFileListElement efl = new PagedFileListElement(this, false);
            efl.setSize();
            efl.setNullImage();
            elements.add(efl);
            container.getChildren().addAll(efl);
        }
        
        regenerateItemList();
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
                    efl.setImage(GUITools.loadIcon("loading-128"));
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
    
    private void setPath(String p) {
        fileSystemParser.setPath(p);
        fileSystemParser.getFiles();
        pathText.setText(fileSystemParser.getCurrentPath().toAbsolutePath().toString());
    }
    
    public boolean isBusy() {
        return (busyCounter != 0);
    }
    
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

//            imageViewer.setFiles(new ArrayList<>(currentFileList.subList(counter, listSize)));
//            imageViewer.show();
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
        currentFileList = pList;

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
        SettingsUtil.setPath("currentPath", p);
    }

    @Override
    public void onError(XImgFS.XImgFSActions act, Exception e) {
        if ((act == XImgFS.XImgFSActions.levelUp) && (!isRoot)) {
            fileSystemParser.getRoots();
        }

        System.out.println("ZZ ERROR: "+e.getClass().getName());
    }
}
