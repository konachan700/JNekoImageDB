package ui.imagelist;

import fao.ImageFile;

public interface BaseImageListItemSelectionListener {
    void onSelect(ImageFile imageFile, int index, boolean selected);
    void OnRightClick(ImageFile imageFile, int index);
}
