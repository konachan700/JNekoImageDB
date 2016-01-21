package img;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
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
import javafx.application.Platform;
import javafx.util.Duration;

public class XImgFS {
    public static class XImgFSWorkerBusyException extends Exception {
        public XImgFSWorkerBusyException() {}
    }
    
    public static enum XImgFSActions {
        NOTHING, getRoots, refreshFileList, levelUp, setPath
    }
    
    private final Object
            syncObject = new Object();
    
    private volatile long
            workerCounter = 0;
    
    private volatile XImgFSActions
            currentAction = XImgFSActions.NOTHING;
    
    private volatile XImgFSActionListener
            al = null;
    
    private Path 
            currentPath = null;
    
    private final ArrayList<Path>
            currentFilesList = new ArrayList<>();
        
    private volatile boolean
            isBusy = false,
            isNotInit = true, 
            isExit = false, 
            isBreak = false;
    
    private final Timeline stateTimer = new Timeline(new KeyFrame(Duration.millis(1), ae -> {
        if (isBusy) {
            al.onProgress(workerCounter);
        }
    }));
    
    private final Runnable 
            fileListGenerator = () -> {
                while(true) {
                    synchronized (syncObject) {
                        try { 
                            isBusy = false;
                            syncObject.wait(); 
                            isBusy = true;
                            workerCounter = 0;
                            isBreak = false;
                        } catch (Exception e) {
                            isExit = true;
                            return;
                        }
                    }
                    
                    if (isExit) return;
                
                    switch (currentAction) {
                        case getRoots:
                            final Set<Path> p = new HashSet<>();
                            FileSystems.getDefault().getRootDirectories().forEach((c) -> {
                                if (Files.exists(c) && Files.isDirectory(c) && Files.isReadable(c)) p.add(c);
                                workerCounter++;
                            });
                            Platform.runLater(() -> { al.rootListGenerated(p); });
                            break;
                        case refreshFileList:
                            final long tmr = System.currentTimeMillis();
                            currentFilesList.clear();
                            try { 
                                final Set<Path> 
                                        files = new HashSet<>(10000), 
                                        dirs = new HashSet<>(500);
                                final DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath);
                                for (Path el : stream) {
                                    workerCounter++;
                                    if (isBreak) { isBreak = false; break; }
                                    
                                    if (Files.isRegularFile(el)) {
                                        final String fn = el.getFileName().toString().trim().toLowerCase();
                                        if ((fn.endsWith(".jpg")) || (fn.endsWith(".jpeg")) || (fn.endsWith(".png")) || (fn.endsWith(".gif"))) {
                                            files.add(el);
                                        }
                                    } else {
                                        if (Files.isDirectory(el)) dirs.add(el);
                                    }
                                }
                                currentFilesList.addAll(dirs);
                                currentFilesList.addAll(files);
                                stream.close();
                                Platform.runLater(() -> { al.fileListRefreshed(currentFilesList, System.currentTimeMillis() - tmr); });
                            } catch (Exception e) {
                                Platform.runLater(() -> { al.onError(currentAction, e);  });
                            }
                            break;                            
                    }
                
                    currentAction = XImgFSActions.NOTHING;
                }
            };

    public XImgFS(XImgFSActionListener _al) {
        al = _al;
    }
    
    public void init() {
        if (!isNotInit) return;
        
        stateTimer.setCycleCount(Animation.INDEFINITE);
        stateTimer.play();
        new Thread(fileListGenerator).start();
        isNotInit = false;
    }

    public void setPath(Path p) {
        if (isNotInit) return;
        if (isBusy) { 
            al.onError(XImgFSActions.levelUp, new XImgFSWorkerBusyException());
            return;
        }
        currentPath = p;
    }
    
    public void setPath(String s) {
        if (isNotInit) return;
        if (isBusy) { 
            al.onError(XImgFSActions.levelUp, new XImgFSWorkerBusyException());
            return;
        }
        
        currentPath = FileSystems.getDefault().getPath(s);
    }
    
    public List<Path> getPage(int pageSize, int offset) {
        return currentFilesList.subList(offset, offset + pageSize);
    }
    
    public void levelUp() {
        if (isNotInit) return;
        if (isBusy) { 
            al.onError(XImgFSActions.levelUp, new XImgFSWorkerBusyException());
            return;
        }
        
        if (currentPath == null) {
            al.onError(XImgFSActions.levelUp, new NullPointerException());
            return;
        }
        
        final Path p = currentPath.getParent();
        if (p == null) {
            al.onError(XImgFSActions.levelUp, new NullPointerException());
            return;
        }
        
        currentPath = p;
        al.onLevelUp(p);
    }
    
    public void getRoots() {
        if (isNotInit) return;
        runCmd(XImgFSActions.getRoots);
    }
        
    public void getFiles() {
        if (isNotInit) return;
        runCmd(XImgFSActions.refreshFileList);
    }
 
    private void runCmd(XImgFSActions c) {
        synchronized (syncObject) {
            if (isBusy) al.onError(c, new XImgFSWorkerBusyException());
            if (currentPath == null) al.onError(c, new NullPointerException());
            
            currentAction = c;
            syncObject.notify();
        }
    }
    
    public void breakCurrentOperation() {
        isBreak = true;
    }
    
    public void dispose() {
        stateTimer.stop();
        isExit = true;
        isNotInit = true;
    }
}
