package img;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

public interface XImgFSActionListener {
    void rootListGenerated(Set<Path> pList);
    void fileListRefreshed(ArrayList<Path> pList, long execTime);
    void onLevelUp(Path p);
    
    void onProgress(long counter);
    void onError(XImgFS.XImgFSActions act, Exception e);
}
