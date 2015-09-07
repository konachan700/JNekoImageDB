package fsimagelist;

import java.io.File;

public interface FSImageListActionListener {
    void OnClick(FSImageListItem item);
    void OnDblFolderClick(FSImageListItem item, File f);
    void OnDblImageClick(FSImageListItem item, File f);
//    void OnProgress(int total, int current);
//    void OnAllImagesLoaded();
}
