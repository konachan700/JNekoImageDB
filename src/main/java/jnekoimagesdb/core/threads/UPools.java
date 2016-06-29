package jnekoimagesdb.core.threads;

import java.util.concurrent.ConcurrentHashMap;

public class UPools {
    private static final ConcurrentHashMap<String, UThreadsGroup>
            rootPool = new ConcurrentHashMap<>();
    
    private static UThreadStatusListener
            uiStatusUpdater = null;
    
    public static UThreadsGroup createThreadsGroup(String gName, int tCount, int priority) {
        if (rootPool.containsKey(gName)) return rootPool.get(gName);
        final UThreadsGroup group = new UThreadsGroup(gName, tCount, priority, uiStatusUpdater);
        rootPool.put(gName, group);
        return group;
    }
    
    public static UThreadsGroup getGroup(String id) {
        return rootPool.get(id);
    }
    
    public static void addWorker(String id, UThreadWorker w) {
        rootPool.get(id).addWorker(w); 
    }
        
    public static void stop(String id) {
        rootPool.get(id).stop();
    }
    
    public static void stopAll() {
        rootPool.keySet().forEach(element -> {
            rootPool.get(element).stop();
        });
    }

    public static void setThreadStateListener(UThreadStatusListener al) {
        uiStatusUpdater = al;
    }
}
