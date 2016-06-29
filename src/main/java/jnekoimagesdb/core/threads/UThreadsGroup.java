package jnekoimagesdb.core.threads;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class UThreadsGroup {
    private final ConcurrentHashMap<String, UThread>
            rootPool = new ConcurrentHashMap<>();
    
    private final ConcurrentHashMap<String, Future>
            futures = new ConcurrentHashMap<>();
    
    private ExecutorService 
            poolSvc = null;
    
    private boolean 
            running = false,
            stop = false;
    
    private final int 
            threadsCount;
    
    private int
            deadTime = 10;
    
    private final String 
            groupName;
    
    private final UThreadStatusListener
            stListener;
    
    private final Timeline 
            controlTimer;

    protected UThreadsGroup(String gName, int tCount, int priority, UThreadStatusListener al) {
        groupName = gName;
        threadsCount = tCount;
        stListener = al;
        
        final ThreadFactory factory = new ThreadFactoryBuilder()
                .setPriority(priority)
                .setNameFormat(gName + "-%d") 
                .setDaemon(true)
                .build();
                    
        poolSvc = Executors.newFixedThreadPool(threadsCount, factory);
        
        controlTimer = new Timeline(new KeyFrame(Duration.millis(100), ae -> {
            rootPool.keySet().forEach(element -> {
                if (stop) return;
                
                if ((!rootPool.get(element).isStopped()) && (!rootPool.get(element).isPaused()) && (!rootPool.get(element).isError())) {
                    final long ttl = rootPool.get(element).getCurrDeadTime() + (deadTime * 1000);
                    if (ttl < System.currentTimeMillis()) {
                        stListener.OnThreadLETDetected(element);
                        futures.get(element).cancel(true);
                    }
                }
                
                if (futures.get(element).isDone() || futures.get(element).isCancelled()) {
                    if (!stop) {
                        final String tName = rootPool.get(element).getThreadName();
                        final UThread t = genElement(tName);
                        rootPool.replace(tName, t);
                        final Future f = poolSvc.submit(t);
                        futures.put(tName, f);
                    }
                }
            });
        }));
    }
    
    private UThread genElement(String tName) {
        final UThread thread = new UThread();
        thread.setThreadName(tName);
        thread.setActionListener(stListener); 
        return thread;
    }
    
    public void run() {
        if (running) return;
        for (int i=0; i<threadsCount; i++) {
            final String tName = groupName + "." + i;
            final UThread thread = genElement(tName);
            rootPool.put(tName, thread);            
            final Future f = poolSvc.submit(thread);
            futures.put(tName, f);
        }
        
        controlTimer.setCycleCount(Animation.INDEFINITE);
        controlTimer.play();
        
        running = true;
    }
    
    public void addWorker(UThreadWorker w) {
        rootPool.keySet().forEach(element -> {
            rootPool.get(element).addWorker(w); 
        });
    }
    
    public int getTotalThreadsCount() {
        return rootPool.size();
    }
    
    public void pause() {
        rootPool.keySet().forEach(element -> {
            rootPool.get(element).pause();
        });
    }
    
    public void resume() {
        rootPool.keySet().forEach(element -> {
            rootPool.get(element).resume();
        });
    }
    
    public void stop() {
        stop = true;
        controlTimer.stop();
        rootPool.keySet().forEach(element -> {
            rootPool.get(element).stop();
        });
        poolSvc.shutdown();
    }
    
    public void setDeadTime(int seconds) {
        deadTime = seconds;
    }
}
