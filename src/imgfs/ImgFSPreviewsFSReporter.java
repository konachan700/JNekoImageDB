package imgfs;

public interface ImgFSPreviewsFSReporter {
    public void OnItemReady(ImgFSRecord fsElement, int threadID);
    public void OnAllItemsInThreadReady(int threadID, int type);
}
