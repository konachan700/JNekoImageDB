package img.gui;

import java.nio.file.Path;

public interface PagedFileListActionListener {
    void onThreadStart(long tid);
    void onThreadPause(long tid, boolean pause, long counter, Path el);
    void onThreadProgress(long tid, long counter);
    
    void onDisable(boolean dis);
    void onPageCountChange(int pageCount);
    void onPageChange(int page);
}
