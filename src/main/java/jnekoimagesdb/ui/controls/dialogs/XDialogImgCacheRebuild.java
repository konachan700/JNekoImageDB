package jnekoimagesdb.ui.controls.dialogs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.util.Duration;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.ui.GUITools;
import static jnekoimagesdb.ui.controls.dialogs.XDialogWindow.COLOR_BLACK;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SElementPair;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.STabTextButton;
import org.hibernate.Session;
import org.iq80.leveldb.DB;
import org.slf4j.LoggerFactory;

public class XDialogImgCacheRebuild extends XDialogWindow {  
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(XDialogImgCacheRebuild.class);
    
    private static final XDialogImgCacheRebuild me = new XDialogImgCacheRebuild();
    
    private final SFLabel
            outText = new SFLabel("", 64, 9999, 64, 64, "label_darkgreen_small", "TypesListItem");
    
    private volatile boolean 
            isNotInit   = true,
            isPause     = true,
            isStop      = false;
    
    private final Object
            syncStopObject = new Object();
    
    private ExecutorService 
            previewGenService = null;
    
    private volatile int 
            currentCursorPosition = 0,
            imgCount = 0;
    
    private final Timeline TMR = new Timeline(new KeyFrame(Duration.millis(222), ae -> {
        if (isPause)
            outText.setText("Перестройка кэша успешно завершена.");
        else 
            outText.setText("Обработано "+currentCursorPosition+" картинок из "+imgCount+". Процесс обработки можно прерывать без потери текущего прогресса.");
    }));
    
    private class ElementRebuilder implements Runnable {
        private final Session hibSession;
        private final DB db = XImg.getDB(XImg.PreviewType.previews);
        
        public ElementRebuilder() {
            hibSession = HibernateUtil.getNewSession();
        }
        
        @Override
        @SuppressWarnings("UseSpecificCatch")
        public void run() {
            DSImage img;
            long currentVal;
            
            while(true) {
                try {
                    if (isPause) {
                        try {
                            synchronized (syncStopObject) {
                                syncStopObject.wait();
                            }
                        } catch (InterruptedException ex) { 
                            logger.error("Thread stopped by user: "+ex.getMessage());
                            try { hibSession.close(); } catch (Exception ex2) {}
                            return;
                        }
                        isPause = false;
                    }

                    if (isStop) {
                        try { hibSession.close(); } catch (Exception ex) {}
                        return;
                    }

                    synchronized (syncStopObject) {
                        currentVal = DSImageIDListCache.getAll().getID(currentCursorPosition);
                        currentCursorPosition++;
                    }

                    if (currentCursorPosition >= imgCount) {
                        logger.error("Thread #"+this.hashCode() + " paused; currentCursorPosition="+currentCursorPosition+"; imgCount="+imgCount+"; currentVal="+currentVal);
                        isPause = true;
                    } else {
                        img = (DSImage) hibSession
                                .createQuery("SELECT r FROM DSImage r WHERE r.imageID=:ids")
                                .setParameter("ids", currentVal)
                                .uniqueResult();

                        if (img != null) {
                            byte tmp[];
                            synchronized (db) {
                                tmp = db.get(img.getMD5());
                            }

                            if (tmp == null) XImgDatastore.createPreviewEntryFromExistDBFile(img.getMD5(), XImg.PreviewType.previews);
                        } else {
                            logger.error("Image ID error: ID#"+currentVal+" not exist in DB;");
                        }
                    }
                } catch (Exception ex) { 
                        logger.error("Thread error: "+ex.getMessage());
                }
            }  
        }
    }

    private XDialogImgCacheRebuild() {
        super();

        final SEVBox mtOptions = new SEVBox("svbox_sett_container_green");
        mtOptions.setAlignment(Pos.CENTER_RIGHT);
        
        mtOptions.getChildren().addAll(
                outText,
                GUITools.getHSeparator(32),
                new SElementPair(
                        GUITools.getSeparator(), 
                        4, 32, 32,
                        GUITools.getSeparator(),
                        new STabTextButton("Приостановить обработку", ElementsIDCodes.buttonUnknown, 190, 32, (x, y) -> {
                            pause();
                        })
                ).setAlign(Pos.CENTER_RIGHT)
        );
        
        super.create(null, null, mtOptions, COLOR_BLACK, 550, 200);
        super.setClosingEnable(false);
    }
    
    public void startRebuild() {
        imgCount = DSImageIDListCache.getAll().getCount();
        
        if (isNotInit) {
            TMR.setCycleCount(Animation.INDEFINITE);
            TMR.play();
        
            final ThreadFactory factory = new ThreadFactoryBuilder()
                    .setPriority(Thread.MAX_PRIORITY)
                    .setNameFormat("CacheRebuilder-%d") 
                    .setDaemon(true)
                    .build();
            
            final int procCount = Runtime.getRuntime().availableProcessors();
            previewGenService = Executors.newFixedThreadPool(procCount <= 0 ? 1 : procCount, factory);
            for (int i=0; i<procCount; i++) {
                previewGenService.submit(new ElementRebuilder());
            }
            //previewGenService.
            isNotInit = false;
        }

        synchronized (syncStopObject) {
            syncStopObject.notifyAll();
        }
        this.showModal();
    }
    
    public final void pause() {
        isPause = true;
        this.hide();
    }
    
    public final void dispose() {
        isStop = true;
        synchronized (syncStopObject) {
            syncStopObject.notifyAll();
        }
        this.hide();
    }
    
    public static XDialogImgCacheRebuild get() {
        return me;
    }
}
