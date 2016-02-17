package jnekoimagesdb.ui.controls.tabs;

import java.util.List;
import jnekoimagesdb.domain.DSTag;

public interface TabAllTagsActionListener {
    void onTagsView(List<DSTag> tags, List<DSTag> tagsNot);
}
