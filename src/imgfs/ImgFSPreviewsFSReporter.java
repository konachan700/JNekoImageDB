package imgfs;

import javafx.scene.image.Image;

public interface ImgFSPreviewsFSReporter {
    public void OnItemReady(ImgFSRecord fsElement, int threadID);
    public void OnAllItemsInThreadReady(int threadID, int type);
    public void OnPreviewGenerateComplete(Image im, String path);
}
