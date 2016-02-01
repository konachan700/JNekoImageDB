package img;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

public interface XImgFSActionListener {
    void rootListGenerated(Set<Path> pList);
    void fileListRefreshed(Path p, ArrayList<Path> pList, long execTime);
    void onLevelUp(Path p);
    
    void onProgress(long tid, long counter);
    void onError(XImgFS.XImgFSActions act, Exception e);
    void onThreadStart(long tid);
    void onThreadPause(long tid, boolean pause);
}
