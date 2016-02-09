package jnekoimagesdb.ui.controls.tabs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.ui.GUITools;
import static jnekoimagesdb.ui.controls.PreviewTypesList.IMG32_ADD;
import jnekoimagesdb.ui.controls.elements.ETagListItem;
import jnekoimagesdb.ui.controls.elements.ETagListItemActionListener;
import jnekoimagesdb.ui.controls.elements.SButton;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SFHBox;
import jnekoimagesdb.ui.controls.elements.SScrollPane;
import jnekoimagesdb.ui.controls.elements.STagAddInputField;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
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
                    final Transaction t = HibernateUtil.getCurrentSession().beginTransaction();
                    HibernateUtil.getCurrentSession().delete(tag);
                    t.commit();
                    refresh();
                }
            };
    
//    private final Set<DSTag>
//            tags = new HashSet<>();
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TabAllTags() {
        super(4);
        GUITools.setMaxSize(this, 9999, 9999);
        this.getChildren().clear();
        tagsContainer.setAlignment(Pos.TOP_CENTER);
        
        final SFHBox 
                addTags = new SFHBox(4, 128, 9999, 32, 32);
        addTags.setMinHeight(Region.USE_PREF_SIZE);
        addTags.setPrefHeight(Region.USE_COMPUTED_SIZE);
        addTags.setMaxHeight(Double.MAX_VALUE);
        addTags.getChildren().addAll(addNewTagField, new SButton(IMG32_ADD, 0, 32, (c, d) -> {
            final ArrayList<DSTag> tagsA = addNewTagField.getTags();
            final Transaction t = HibernateUtil.getCurrentSession().beginTransaction();
            tagsA.forEach(tag -> {
                if (notExist(tag)) {
                    HibernateUtil.getCurrentSession().save(tag);
                } else 
                    logger.info("Element ["+tag.getTagName()+"] already exist.");
            });
            t.commit();
            refresh();
            addNewTagField.clear();
        }, "button_pts_add"));
        
        final SScrollPane
                tagSP = new SScrollPane();
        tagSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        tagSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tagSP.setFitToHeight(true);
        tagSP.setFitToWidth(true);
        tagSP.setContent(tagsContainer);
        
        tagsContainer.setVgap(4);
        tagsContainer.setHgap(4);

        this.getChildren().addAll(addTags, tagSP);
    }
    
    private boolean notExist(DSTag t) {
        return HibernateUtil.getCurrentSession().createCriteria(DSTag.class)
                .add(Restrictions.eq("tagName", t.getTagName()))
                .uniqueResult() == null;
    }
    
    
    public final void refresh() {
        List<DSTag> list = HibernateUtil.getCurrentSession()
                            .createCriteria(DSTag.class)
                            .setMaxResults(500)
                            .setFirstResult(0)
                            .list();
        
        tagsContainer.getChildren().clear();
        list.forEach(c -> {
            tagsContainer.getChildren().add(new ETagListItem(c, true, false, elActListener));
        });
    }
}
