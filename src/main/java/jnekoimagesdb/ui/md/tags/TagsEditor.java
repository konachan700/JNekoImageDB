package jnekoimagesdb.ui.md.tags;

import com.google.gson.Gson;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.ui.controls.dialogs.XDialogOpenDirectory;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;
import jnekoimagesdb.ui.md.toppanel.TopPanelSearch;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class TagsEditor extends ScrollPane {
    public final static String
            CSS_FILE = new File("./style/style-gmd-tags.css").toURI().toString();
    
    public static final int
            TAGS_PER_PAGE = 500;
    
    private final FlowPane
            tagsContainer = new FlowPane();
    
    private final TopPanel
            panelTop;
        
    private final TopPanelMenuButton 
            menuBtn = new TopPanelMenuButton();
    
    private String 
            searchTxt = "";
    
    private final Label
            bottomPanelForAlbums = new Label("Статистика альбома");
    
    private final TagsEditorElementActionListener 
            tagElementAL = new TagsEditorElementActionListener() {
                @Override
                public void OnDelete(DSTag tag) {
                    final Transaction t = HibernateUtil.getCurrentSession().beginTransaction();
                    HibernateUtil.getCurrentSession().delete(tag);
                    t.commit();
                    refresh();
                }

                @Override
                public void OnEdit(DSTag old, String newName) {
                    if (newName.trim().length() < 1) return;
                    final Transaction t = HibernateUtil.getCurrentSession().beginTransaction();
                    old.setTagName(newName);
                    HibernateUtil.getCurrentSession().save(old);
                    t.commit();
                    refresh();
                }   
            };
    
    public TagsEditor() {
        super();
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setFitToHeight(true);
        this.setFitToWidth(true);
        this.setContent(tagsContainer);
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("tags_list_max_width", "tags_list_max_height", "tags_list_sp");
        
        tagsContainer.setAlignment(Pos.TOP_LEFT);
        tagsContainer.getStyleClass().addAll("tags_list_max_width", "tags_list_max_height", "tags_list_container_pane", "tags_list_space");
        
        bottomPanelForAlbums.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "tags_list_bottom_panel");
        
        panelTop = new TopPanel(); 
        panelTop.addNode(new TopPanelSearch("", c -> {
            searchTxt = c;
            refresh();
        }));
        
        menuBtn.addMenuItem("Добавить теги...", (c) -> {
            tagsContainer.getChildren().clear();
//            tagsContainer.getChildren().add(addTags);
        });
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Сохранить список как текст...", (c) -> {
            toText();
        });
        menuBtn.addMenuItem("Сделать полную резервную копию (JSON)...", (c) -> {
            toJSON();
        });
        menuBtn.addSeparator();
        menuBtn.addMenuItem("Очистить список...", (c) -> {
            
        });
        
        panelTop.addNode(menuBtn);
        
        
        
    }
    
    public final void refresh() {
        List<DSTag> list;
        if (searchTxt.trim().length() < 1) {
            list = HibernateUtil.getCurrentSession()
                    .createCriteria(DSTag.class)
                    .addOrder(Order.desc("tagID"))
                    .setMaxResults(TAGS_PER_PAGE)
                    .setFirstResult(0)
                    .list();
        } else {
            list = HibernateUtil.getCurrentSession()
                    .createCriteria(DSTag.class)
                    .add(Restrictions.like("tagName", searchTxt+"%"))
                    .addOrder(Order.desc("tagID"))
                    .setMaxResults(TAGS_PER_PAGE)
                    .setFirstResult(0)
                    .list();
        }

        tagsContainer.getChildren().clear();
        list.forEach(c -> {
            tagsContainer.getChildren().add(new TagsEditorElement(c, tagElementAL));
        });
        
        final Number tagsCountN = ((Number) HibernateUtil.getCurrentSession().createCriteria(DSTag.class).setProjection(Projections.rowCount()).uniqueResult());
        bottomPanelForAlbums.setText("Всего "+tagsCountN.toString()+" тегов в БД, показаны последние "+TAGS_PER_PAGE+" добавленных.");
    }
    
    public final Node getTopPanel() {
        return panelTop;
    }
    
    public final Node getPaginator() {
        return bottomPanelForAlbums;
    }
    
    private void toText() {
        XImg.openDir().showDialog();
        if (XImg.openDir().getResult() == XDialogOpenDirectory.XDialogODBoxResult.dUnknown) {
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
        if (XImg.openDir().getResult() == XDialogOpenDirectory.XDialogODBoxResult.dUnknown) {
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
}
