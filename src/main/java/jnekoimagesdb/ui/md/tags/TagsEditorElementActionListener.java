package jnekoimagesdb.ui.md.tags;

import jnekoimagesdb.domain.DSTag;

public interface TagsEditorElementActionListener {
    public void OnDelete(DSTag t);
    public void OnEdit(DSTag old, String newName);
}
