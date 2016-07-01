package jnekoimagesdb.ui.md.imagelist;

import jnekoimagesdb.domain.DSImage;

public interface PagedImageListElementActionListener {
    public void OnSelect(boolean isSelected, DSImage item);
    public void OnOpen(DSImage item, PagedImageListElement it);
    public void OnError(DSImage item);
}
