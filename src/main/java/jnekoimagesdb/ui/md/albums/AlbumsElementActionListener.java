package jnekoimagesdb.ui.md.albums;

import javafx.scene.input.MouseEvent;
import jnekoimagesdb.domain.DSAlbum;

public interface AlbumsElementActionListener {
    public void OnClick(AlbumsElement element, DSAlbum album, MouseEvent value);
    public void OnDoubleClick(AlbumsElement element, DSAlbum album, MouseEvent value);
    public void OnEdit(DSAlbum album, String title, String text);
    public void OnDelete(DSAlbum album);
    public void OnToImagesButtonClick(DSAlbum album, MouseEvent value);
}
