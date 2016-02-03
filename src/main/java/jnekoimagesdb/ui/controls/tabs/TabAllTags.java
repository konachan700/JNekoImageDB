package jnekoimagesdb.ui.controls.tabs;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.elements.ETagListItem;
import jnekoimagesdb.ui.controls.elements.ETagListItemActionListener;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.STagAddInputField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabAllTags extends SEVBox {
    private final Logger 
            logger = LoggerFactory.getLogger(TabAllTags.class);
    
    private final FlowPane
            tagsContainer = new FlowPane();
    
    private final STagAddInputField
            addNewTagField = new STagAddInputField(false);
    
    
    private final ETagListItemActionListener
            elActListener = new ETagListItemActionListener() {
                @Override
                public void onClick(DSTag tag) {
                }

                @Override
                public void onAddToListBtnClick(DSTag tag, boolean isSetMinus) {
                }

                @Override
                public void onEditComplete(DSTag tag) {
                }

                @Override
                public void onDelete(DSTag tag) {
                }
            };
    
    private final Set<DSTag>
            tags = new HashSet<>();
    
    public TabAllTags() {
        super(2);

        tagsContainer.setVgap(4);
        tagsContainer.setHgap(4);
        
        
        GUITools.setMaxSize(this, 9999, 9999);
        this.getChildren().clear();
        
        tags.add(new DSTag("test"));
        tags.add(new DSTag("test deasfv ASDg eahethethetheththethb argvbwbvrea"));
        tags.add(new DSTag("test 345234"));
        tags.add(new DSTag("test_fnfhjf_fbf"));
        
        tags.forEach((c -> {
            tagsContainer.getChildren().add(new ETagListItem(c, true, true, elActListener));
            logger.debug("tag="+c.getTagName());
        }));
        
        this.getChildren().addAll(addNewTagField, tagsContainer);
        
        
    }
}
