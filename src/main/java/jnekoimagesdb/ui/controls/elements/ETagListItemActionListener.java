package jnekoimagesdb.ui.controls.elements;

import jnekoimagesdb.domain.DSTag;

public interface ETagListItemActionListener {
    void onClick(DSTag tag);
    void onAddToListBtnClick(DSTag tag, boolean isSetMinus);
    void onEditComplete(DSTag tag);
    void onDelete(DSTag tag);
}
