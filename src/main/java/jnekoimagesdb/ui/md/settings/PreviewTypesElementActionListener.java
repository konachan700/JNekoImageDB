package jnekoimagesdb.ui.md.settings;

import jnekoimagesdb.domain.DSPreviewSize;

public interface PreviewTypesElementActionListener {
    public void OnSetDefault(PreviewTypesElement element, DSPreviewSize size);
    public void OnDelete(PreviewTypesElement element, DSPreviewSize size);
}
