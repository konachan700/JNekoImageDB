package jnekouilib.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

public class FSParser {
    public static class FSParserBusyException extends Exception {
        public FSParserBusyException() {}
    }
    
    private class FileListGenerator implements Runnable {
        @Override
        public void run() {
//            Platform.runLater(() -> { al.onThreadStart(this.hashCode());  });
            while(true) {
                synchronized (syncObject) {
                    try { 
                        isBusy = false;
//                        Platform.runLater(() -> { al.onThreadPause(this.hashCode(), true);  });
                        syncObject.wait(); 
//                        Platform.runLater(() -> { al.onThreadPause(this.hashCode(), false);  });
                        isBusy = true;
//                        workerCounter = 0;
                        isBreak = false;
                    } catch (Exception e) {
                        isExit = true;
                        return;
                    }
                }

                if (isExit) return;

                switch (currentAction) {
                    case getRoots:
                        currentFilesList.clear();
                        final Set<Path> p = new HashSet<>();
                        FileSystems.getDefault().getRootDirectories().forEach((c) -> {
                            if (Files.exists(c) && Files.isDirectory(c) && Files.isReadable(c)) p.add(c);
//                            workerCounter++;
                        });
                        currentFilesList.addAll(p);
                        isBusy = false;
                        Platform.runLater(() -> { al.rootListGenerated(p); });
                        break;
                    case refreshFileList:
                        final long tmr = System.currentTimeMillis();
                        currentFilesList.clear();
                        try { 
                            final Set<Path> 
                                    files = new HashSet<>(2000), 
                                    dirs = new HashSet<>(100);
                            final DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath);
                            for (Path el : stream) {
//                                workerCounter++;
                                if (isBreak) { isBreak = false; break; }

                                if (Files.isRegularFile(el)) {
                                    if (!dirsOnly) files.add(el);
                                } else {
                                    if (Files.isDirectory(el)) dirs.add(el);
                                }
                            }
                            currentFilesList.addAll(dirs);
                            if (!dirsOnly) currentFilesList.addAll(files);
                            stream.close();
                            isBusy = false;
                            Platform.runLater(() -> { al.fileListRefreshed(currentPath, currentFilesList, System.currentTimeMillis() - tmr); });
                        } catch (IOException e) {
                            Logger.getLogger(FSParser.class.getName()).log(Level.SEVERE, null, e);
                            isBusy = false;
                            Platform.runLater(() -> { al.onError(currentAction, e);  });
                        }
                    
                
                        break;                                                        
                }

                currentAction = FSParserActions.NOTHING;
            }
        }
    }
    
    private final Object
            syncObject = new Object();
    
    private boolean 
            dirsOnly = false;
    
//    private volatile long
//            workerCounter = 0;
    
    private volatile FSParserActions
            currentAction = FSParserActions.NOTHING;
    
    private volatile FSParserActionListener
            al = null;
    
    private Path 
            currentPath = null;
    
    private final CopyOnWriteArrayList<Path>
            currentFilesList = new CopyOnWriteArrayList<>();
        
    private volatile boolean
            isBusy = false,
            isNotInit = true, 
            isExit = false, 
            isBreak = false;
    
    private final FileListGenerator 
            fileListGenerator = new FileListGenerator();
    
//    private final Timeline stateTimer = new Timeline(new KeyFrame(Duration.millis(1), ae -> {
//        if (isBusy) {
////            al.onProgress(fileListGenerator.hashCode(), workerCounter);
//        }
//    }));
    
    public FSParser(FSParserActionListener _al) {
        al = _al;
    }
    
    public void initDir() {
        dirsOnly = true;
        init();
    }
    
    public void init() {
        if (!isNotInit) return;
        
//        stateTimer.setCycleCount(Animation.INDEFINITE);
//        stateTimer.play();
        new Thread(fileListGenerator).start();
        isNotInit = false;
    }

    public void setPath(Path p) {
        if (isNotInit) return;
        if (isBusy) { 
            al.onError(FSParserActions.levelUp, new FSParserBusyException());
            return;
        }
        currentPath = p;
    }
    
    public void setPath(String s) {
        if (isNotInit) return;
        if (isBusy) { 
            al.onError(FSParserActions.levelUp, new FSParserBusyException());
            return;
        }
        
        currentPath = FileSystems.getDefault().getPath(s);
    }
    
    public List<Path> getPage(int pageSize, int offset) {
        int toIndex = ((offset + pageSize) > currentFilesList.size()) ? currentFilesList.size() : (offset + pageSize);
        return currentFilesList.subList(offset, toIndex);
    }
    
    public CopyOnWriteArrayList<Path> getFullList() {
        return currentFilesList;
    }
    
    public Path getCurrentPath() {
        return currentPath;
    }
    
    public void levelUp() {
        if (isNotInit) return;
        if (isBusy) { 
            al.onError(FSParserActions.levelUp, new FSParserBusyException());
            return;
        }
        
        if (currentPath == null) {
            al.onError(FSParserActions.levelUp, new NullPointerException());
            return;
        }
        
        final Path p = currentPath.toAbsolutePath().getParent();
        if (p == null) {
            al.onError(FSParserActions.levelUp, new NullPointerException());
            return;
        }
        
        currentPath = p;
        al.onLevelUp(p);
    }
    
    public void getRoots() {
        if (isNotInit) return;
        runCmd(FSParserActions.getRoots);
    }
        
    public void getFiles() {
        if (isNotInit) return;
        runCmd(FSParserActions.refreshFileList);
    }
 
    private void runCmd(FSParserActions c) {
        synchronized (syncObject) {
            if (isBusy) al.onError(c, new FSParserBusyException());
            if (currentPath == null) al.onError(c, new NullPointerException());
            
            currentAction = c;
            syncObject.notify();
        }
    }
    
    public void breakCurrentOperation() {
        isBreak = true;
    }
    
    public void dispose() {
//        stateTimer.stop();
        isExit = true;
        isNotInit = true;
        synchronized (syncObject) {
            syncObject.notify();
        }
    }
}
