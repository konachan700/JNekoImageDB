package jnekouilib.utils;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public interface FSParserActionListener {
    void rootListGenerated(Set<Path> pList);
    void fileListRefreshed(Path p, CopyOnWriteArrayList<Path> pList, long execTime);
    void onLevelUp(Path p);
//    void onProgress(long tid, long counter);
    void onError(FSParserActions act, Exception e);
//    void onThreadStart(long tid);
//    void onThreadPause(long tid, boolean pause);
}
