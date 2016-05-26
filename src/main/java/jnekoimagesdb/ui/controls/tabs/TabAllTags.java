package jnekoimagesdb.ui.controls.tabs;

import com.google.gson.Gson;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.PanelButtonCodes;
import jnekoimagesdb.ui.controls.ToolsPanelTop;
import jnekoimagesdb.ui.controls.dialogs.XDialogOpenDirectory.XDialogODBoxResult;
import jnekoimagesdb.ui.controls.elements.ETagListItem;
import jnekoimagesdb.ui.controls.elements.ETagListItemActionListener;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SButton;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SFHBox;
import jnekoimagesdb.ui.controls.elements.SScrollPane;
import jnekoimagesdb.ui.controls.elements.STagAddInputField;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabAllTags extends SEVBox {
    private final Logger 
            logger = LoggerFactory.getLogger(TabAllTags.class);
    
    public static final int
            TAGS_PER_PAGE   = 500;
    
    private final FlowPane
            tagsContainer = new FlowPane();
    
    private final STagAddInputField
            addNewTagField = new STagAddInputField(false);
    
    private final Pagination
            pag = new Pagination();
    
    private int
            tagsCount = 0;
    
    private TabAllTagsActionListener
            extActListener = null;
    
    private final ToolsPanelTop.SPanelPopupMenuButton 
            menuBtn = new ToolsPanelTop.SPanelPopupMenuButton();
    
    private final ETagListItemActionListener
            elActListener = new ETagListItemActionListener() {
                @Override
                public void onClick(DSTag tag) {
                    final List<DSTag> t = new ArrayList<>();
                    t.add(tag);
                    if (extActListener != null)
                        extActListener.onTagsView(t, null);
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
    
    private final ToolsPanelTop panelTop = new ToolsPanelTop((index) -> { });
    
    private void toText() {
        XImg.openDir().showDialog();
        if (XImg.openDir().getResult() == XDialogODBoxResult.dUnknown) {
            return;
        }
        
        final Path path = XImg.openDir().getSelected().toAbsolutePath();
        
        if (!Files.exists(path))
            try {
                Files.createDirectory(path); 
            } catch (Exception e) {
                XImg.msgbox("Не могу создать папку логов, сохранение невозможно.");
                return;
            }
        
        final StringBuilder sb = new StringBuilder();
        final List<DSTag> list = HibernateUtil.getCurrentSession()
                .createCriteria(DSTag.class)
                .setFirstResult(0)
                .list();
        
        list.forEach((el -> {
            sb.append(el.getTagName()).append("\r\n");
        }));

        try {
            final Date dt = new Date();
            final SimpleDateFormat df = new SimpleDateFormat("HH-mm_dd-MM-yyyy");
            Files.write(FileSystems.getDefault().getPath(path.toString(), "tags_list_"+df.format(dt)+".txt"), sb.toString().getBytes());
            XImg.msgbox("Резервная копия создана успешно!");
        } catch (Exception e) {
            XImg.msgbox("Папка логов недоступна на запись, сохранение невозможно.");
        }
    }
    
    private void toJSON() {
        XImg.openDir().showDialog();
        if (XImg.openDir().getResult() == XDialogODBoxResult.dUnknown) {
            return;
        }
        
        final Path path = XImg.openDir().getSelected().toAbsolutePath();
        
        if (!Files.exists(path))
            try {
                Files.createDirectory(path); 
            } catch (Exception e) {
                XImg.msgbox("Не могу создать папку логов, сохранение невозможно.");
                return;
            }

        final List<DSTag> list = HibernateUtil.getCurrentSession()
                .createCriteria(DSTag.class)
                .setFirstResult(0)
                .list();
        final Gson gson = new Gson();
        final String data = gson.toJson(list);
        try {
            final Date dt = new Date();
            final SimpleDateFormat df = new SimpleDateFormat("HH-mm_dd-MM-yyyy");
            Files.write(FileSystems.getDefault().getPath(path.toString(), "tags_backup_"+df.format(dt)+".json"), data.getBytes());
            XImg.msgbox("Резервная копия создана успешно!");
        } catch (Exception e) {
            XImg.msgbox("Папка логов недоступна на запись, сохранение невозможно.");
        }
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TabAllTags() {
        super(4);
        GUITools.setMaxSize(this, 9999, 9999);
        this.getChildren().clear();
        
        tagsContainer.setAlignment(Pos.TOP_CENTER);
        GUITools.setStyle(tagsContainer, "TabAllTags", "flow_pane");
        
        pag.setMaxSize(9999, 24);
        pag.setMinSize(128, 24);
        pag.setPrefSize(9999, 24);
        
        pag.currentPageIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refresh();
        });
        
        final SFHBox 
                addTags = new SFHBox(4, 128, 9999, 32, 32);
        addTags.setMinHeight(Region.USE_PREF_SIZE);
        addTags.setPrefHeight(Region.USE_COMPUTED_SIZE);
        addTags.setMaxHeight(Double.MAX_VALUE);
        addTags.getChildren().addAll(addNewTagField, new SButton(GUITools.loadIcon("plus-32"), ElementsIDCodes.buttonUnknown, 32, (c, d) -> {
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
        
        
        menuBtn.addMenuItem("Сохранить список как текст...", (c) -> {
            toText();
        });
        menuBtn.addMenuItem("Сделать полную резервную копию (JSON)...", (c) -> {
            toJSON();
        });
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Очистить список...", (c) -> {
            
        });
        
        panelTop.addSeparator();
        panelTop.addMenuButton(menuBtn);

        final SScrollPane
                tagSP = new SScrollPane();
        tagSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        tagSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tagSP.setFitToHeight(true);
        tagSP.setFitToWidth(true);
        tagSP.setContent(tagsContainer);
        
        tagsContainer.setVgap(4);
        tagsContainer.setHgap(4);

        this.getChildren().addAll(tagSP, addTags);
    }
    
    public void setActionListener(TabAllTagsActionListener li) {
        extActListener = li;
    }
    
    private boolean notExist(DSTag t) {
        return HibernateUtil.getCurrentSession().createCriteria(DSTag.class)
                .add(Restrictions.eq("tagName", t.getTagName()))
                .uniqueResult() == null;
    }
    
    public final ToolsPanelTop getTopPanel() {
        return panelTop;
    }
    
    public final Pagination getPaginator() {
        return pag;
    }
    
    public final void refresh() {
        final Number tagsCountN = ((Number) HibernateUtil.getCurrentSession().createCriteria(DSTag.class).setProjection(Projections.rowCount()).uniqueResult());
        if (tagsCountN == null) return;
        tagsCount = tagsCountN.intValue();
        pag.setPageCount(((tagsCount % TAGS_PER_PAGE)==0) ? (tagsCount / TAGS_PER_PAGE) : ((tagsCount / TAGS_PER_PAGE)+1));
        
        List<DSTag> list = HibernateUtil.getCurrentSession()
                            .createCriteria(DSTag.class)
                            .setMaxResults(TAGS_PER_PAGE)
                            .setFirstResult(pag.getCurrentPageIndex() * TAGS_PER_PAGE)
                            .list();
        
        tagsContainer.getChildren().clear();
        list.forEach(c -> {
            tagsContainer.getChildren().add(new ETagListItem(c, false, false, elActListener));
        });
    }
}
