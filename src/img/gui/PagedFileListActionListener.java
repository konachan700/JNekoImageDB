package img.gui;

public interface PagedFileListActionListener {
    void onThreadStart(long tid);
    void onThreadPause(long tid, boolean pause);
    void onThreadProgress(long tid, long counter);
    
    void onDisable(boolean dis);
    void onPageCountChange(int pageCount);
    void onPageChange(int page);
}
