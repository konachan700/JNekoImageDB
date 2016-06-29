package jnekoimagesdb.core.threads;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UThread implements Runnable {
    private final Logger 
            logger = LoggerFactory.getLogger(UThread.class);
    
    private final Object
            syncObjectPause = new Object();
    
    private String 
            threadName;
    
    private volatile boolean 
            stop = false,
            pause = false,
            error = false,
            goToSleep = false;
    
    private volatile long
            lastExecuteTime = 0,
            currentTime = 0,
            startTime = 0,
            statusUpdateTime = 1000,
            statusTimer = 0;
    
    private final CopyOnWriteArraySet<UThreadWorker>
            basicWorkers = new CopyOnWriteArraySet<>(); 
    
    private UThreadStatusListener
            actListener = null;
    
    protected UThread() {
        threadName = "ID#" + this.hashCode();
        currentTime = System.currentTimeMillis();
    }
    
    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        currentTime = System.currentTimeMillis();
        if (actListener != null) 
                        Platform.runLater(() -> { actListener.OnThreadCreated(threadName); }); 
        
        while (true) {
            try {
                if (pause) {
                    if (actListener != null) 
                        Platform.runLater(() -> { actListener.OnThreadPaused(threadName); });
                    synchronized (syncObjectPause) {
                        syncObjectPause.wait();
                    }
                    pause = false;
                    if (actListener != null) 
                        Platform.runLater(() -> { actListener.OnThreadStarted(threadName); });
                }
                
                if (stop) {
                    if (actListener != null) 
                        Platform.runLater(() -> { actListener.OnThreadStop(threadName); });
                    basicWorkers.clear();
                    return;
                }
                
                if (!basicWorkers.isEmpty()) {
                    goToSleep = true;
                    basicWorkers.forEach(element -> {
                        currentTime = System.currentTimeMillis();
                        if (element.Worker(this)) goToSleep = false;
                        lastExecuteTime = System.currentTimeMillis() - currentTime; 
                        if ((System.currentTimeMillis() - statusTimer) > statusUpdateTime) {
                            if (actListener != null) 
                                Platform.runLater(() -> { actListener.OnThreadStatusReport(threadName, lastExecuteTime, startTime); });
                            statusTimer = System.currentTimeMillis();
                        } 
                    });
                    if (goToSleep) pause = true;
                } else 
                    pause = true;
            } catch (Exception e) {
                if (actListener != null) 
                    Platform.runLater(() -> { actListener.OnThreadError(threadName); });
                logger.error(e.getMessage() + " ## "+e.toString()+" ## name: "+threadName + "\n");
//                e.printStackTrace();
            }  catch (Error e) {
                if (actListener != null) 
                    Platform.runLater(() -> { actListener.OnThreadBroken(threadName); });
                logger.error(e.getMessage() + " ## BROKEN ## name: "+threadName);
                error = true;
                return;
            } 
        }
    }

    public boolean isPaused() {
        return pause;
    }
    
    public boolean isStopped() {
        return stop;
    }
    
    public boolean isError() {
        return error;
    }
    
    public void setStatusUpdateTime(long time) {
        statusUpdateTime = time;
    }
    
    public void addWorker(UThreadWorker utw) {
        basicWorkers.add(utw);
    }
    
    public void addWorkers(Collection utw) {
        basicWorkers.addAll(utw);
    }

    public void setActionListener(UThreadStatusListener al) {
        actListener = al;
    }
    
    public void setThreadName(String name) {
        threadName = name;
    }
    
    public String getThreadName() {
        return threadName;
    }
    
    public void pause() {
        pause = true;
    }
    
    public void resume() {
        pause = false;
        synchronized (syncObjectPause) {
            syncObjectPause.notifyAll();
        }
    }
    
    public void stop() {
        stop = true;
        synchronized (syncObjectPause) {
            syncObjectPause.notifyAll();
        }
    }
    
    protected long getCurrDeadTime() {
        return currentTime;
    }
    
    public long getLastExecuteTime() {
        return lastExecuteTime;
    }
    
    public long getUptime() {
        return (System.currentTimeMillis() - startTime);
    }
}
