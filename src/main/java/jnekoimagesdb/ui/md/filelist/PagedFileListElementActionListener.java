package jnekoimagesdb.ui.md.filelist;

import java.nio.file.Path;

public interface PagedFileListElementActionListener {
    public void OnSelect(boolean isSelected, Path itemPath);
    public void OnOpen(Path itemPath);
}
