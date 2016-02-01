package img.gui;

import img.gui.elements.GUIElements;
import datasources.DSAlbum;
import datasources.HibernateUtil;
import jnekoimagesdb.Lang;
import img.gui.elements.SEVBox;
import img.gui.elements.SFHBox;
import img.gui.elements.SFVBox;
import img.gui.elements.SScrollPane;
import img.gui.elements.STextArea;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import jnekoimagesdb.GUITools;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class AlbumList extends SEVBox {
    public static final int
            ADD_NEW_ELEMENT_VSIZE = 32;
    
    private DSAlbum
            currentAlbum;
    
    private long  
            albumCountInThis = 0;
    
    private Session 
            hibSession = null;
    
    public static final Image 
            ALBUM_DEFAULT = GUITools.loadIcon("dir-normal-128"),
            SELECTED_16 = GUITools.loadIcon("selected-16"),
            UNSELECTED_16 = GUITools.loadIcon("unselected2-16"),
            SAVE_16 = GUITools.loadIcon("save-16"),
            EDIT_16 = GUITools.loadIcon("edit-16");
    
    private final SEVBox
            container = new SEVBox();
    
    private final SScrollPane
            albumList = new SScrollPane();
    
    private volatile boolean
            globalDialogMode = false;
    
    private final ArrayList<DSAlbum>
            selectedItems = new ArrayList<>();
    
    public static interface AlbumListActionListener {
        void OnAlbumChange(String newAlbumName, DSAlbum d);
        void OnListCompleted(long count, DSAlbum d);
    }
    
    private final AlbumListActionListener
            myAL;

    public static interface AddNewAlbumActionListener {
        void OnNew(long parent, String title);
    }
    
    public static class AddNewAlbumElement extends SFHBox {
        private final Button
                addBtn = new Button(Lang.NullString, new ImageView(GUITools.loadIcon("plus-32")));

        private final TextField
                newItemName = new TextField();

        private final Label       
                newTitle = new Label(Lang.ASDNewElement_newTitle);

        private final AddNewAlbumActionListener
                elAL;

        private final long
                parent_id;

        public AddNewAlbumElement(AddNewAlbumActionListener al, long pid) {
            super(4, 128, 9999, 48, 48, "AddNewAlbumElement");
            this.setAlignment(Pos.CENTER);

            elAL = al;
            parent_id = pid;
            
            GUITools.setStyle(newTitle, "AddNewAlbumElement", "newTitle");
            GUITools.setFixedSize(newTitle, 196, ADD_NEW_ELEMENT_VSIZE);
            newTitle.setAlignment(Pos.CENTER_LEFT);

            GUITools.setStyle(addBtn, "AddNewAlbumElement", "addBtn");
            GUITools.setFixedSize(addBtn, ADD_NEW_ELEMENT_VSIZE, ADD_NEW_ELEMENT_VSIZE);
            addBtn.setOnMouseClicked((MouseEvent event) -> {
                if (newItemName.getText().trim().length() > 0) elAL.OnNew(parent_id, newItemName.getText().trim());
                newItemName.setText("");
            });

            GUITools.setStyle(newItemName, "AddNewAlbumElement", "newItemName");
            GUITools.setMaxSize(newItemName, 9999, ADD_NEW_ELEMENT_VSIZE);

            this.getChildren().addAll(newTitle, newItemName, addBtn);
        }
    }
    
    public static interface AlbumListElementActionListener {
        void OnItemClick(DSAlbum a, AlbumsListElement e);
        void OnSave(DSAlbum a, AlbumsListElement e, String newTitle, String newText); 
        void OnCheck(DSAlbum a, boolean state);
    }

    public static class AlbumsListElement extends SFHBox {
        private final DSAlbum
                thisAlbum;

        private final TextField       
                title = new TextField();

        private final Label       
                titleLabel = new Label();

        private final AlbumListElementActionListener
                elementAL;

        private boolean 
                editMode = false, 
                checked = false;
        
        private boolean
                dialogMode = false;
        
        private final SFVBox 
                elementContainer = new SFVBox(0, 128, 9999, 128, 128);
        
        private final SFHBox 
                titleContainer = new SFHBox(4, 128, 9999, 24, 24);
        
        private final STextArea
                albumText = new STextArea(128, 9999, 90, 90, "albumText");

        private final ImageView
                icon_d = new ImageView(ALBUM_DEFAULT),
                selected_i = new ImageView(SELECTED_16),
                unselected_i = new ImageView(UNSELECTED_16),
                save_i = new ImageView(SAVE_16),
                edit_i = new ImageView(EDIT_16);

        private final Button
                saveBtn = new Button(Lang.NullString, edit_i),
                selectBtn = new Button(Lang.NullString, unselected_i);

        public AlbumsListElement(DSAlbum a, AlbumListElementActionListener al, boolean dm) {
            super(16, 128, 9999, 128, 128);
            this.getStyleClass().add("AlbumsListElement_rootPane");

            thisAlbum = a;
            elementAL = al;
            dialogMode = dm;

            title.setText(thisAlbum.getAlbumName());
            titleLabel.setText(thisAlbum.getAlbumName());
            albumText.setText(thisAlbum.getAlbumText());

            _init();
        }
        
        public String getText() {
            return title.getText();
        }
        
        public DSAlbum getAlbum() {
            return thisAlbum;
        }

        private void _editModeOff() {
            saveBtn.setGraphic(edit_i);
            editMode = false;
            titleContainer.getChildren().clear();
            titleContainer.getChildren().addAll(titleLabel, saveBtn);
            albumText.setEditable(editMode);
            albumText.getStyleClass().remove("GUIElements_albumText_EditMode");
        }

        private void _editModeOn() {
            saveBtn.setGraphic(save_i);
            editMode = true;
            titleContainer.getChildren().clear();
            titleContainer.getChildren().addAll(title, saveBtn);
            albumText.setEditable(editMode);
            albumText.getStyleClass().add("GUIElements_albumText_EditMode");
        }

        public void setSelected(boolean b) {
            checked = b;
            if (checked) 
                selectBtn.setGraphic(selected_i);
             else 
                selectBtn.setGraphic(unselected_i);
        }
        
        private void _init() {
            GUITools.setFixedSize(saveBtn, 16, 16);
            GUITools.setStyle(saveBtn, "AlbumsListElement", "btn");
            saveBtn.setOnMouseClicked((MouseEvent event) -> {
                if (editMode) {
                    if (title.getText().trim().length() > 0) {
                        elementAL.OnSave(thisAlbum, this, title.getText().trim(), albumText.getText());
                        titleLabel.setText(title.getText().trim());
                    }
                    _editModeOff();
                } else {
                    _editModeOn();
                }
            });
            
            GUITools.setFixedSize(selectBtn, 16, 16);
            GUITools.setStyle(selectBtn, "AlbumsListElement", "btn");
            selectBtn.setOnMouseClicked((MouseEvent event) -> {
                if (checked) {
                    selectBtn.setGraphic(unselected_i);
                    checked = false;
                } else {
                    selectBtn.setGraphic(selected_i);
                    checked = true;
                }
                elementAL.OnCheck(thisAlbum, checked);
            });
            
            GUITools.setMaxSize(titleLabel, 9999, 16);
            GUITools.setStyle(titleLabel, "AlbumsListElement", "titleLabel");
            titleLabel.setAlignment(Pos.CENTER_LEFT);
            this.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    elementAL.OnItemClick(thisAlbum, this);
                    event.consume();
                }
            });
            
            albumText.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    elementAL.OnItemClick(thisAlbum, this);
                    event.consume();
                }
            });

            GUITools.setMaxSize(title, 9999, 16);
            GUITools.setStyle(title, "AlbumsListElement", "title");
            title.setAlignment(Pos.CENTER_LEFT);
            
            albumText.setEditable(false);
            albumText.setWrapText(true);
            
            this.getChildren().addAll(icon_d, elementContainer);
            titleContainer.getChildren().add(titleLabel);
            titleContainer.getChildren().add(saveBtn);
            if (dialogMode) titleContainer.getChildren().add(selectBtn);
            elementContainer.getChildren().addAll(titleContainer, albumText);
        }
    }
    
    private final AddNewAlbumElement
            addAlbum = new AddNewAlbumElement((long parent, String title) -> {
                if (title.trim().length() < 2) return;
                if (hibSession != null) {
                    HibernateUtil.beginTransaction(hibSession);
                    DSAlbum da = new DSAlbum(title, "", (currentAlbum == null) ? 0 : currentAlbum.getAlbumID());
                    hibSession.save(da);
                    HibernateUtil.commitTransaction(hibSession);
                }
                refresh();
            }, 0);
    
    private final AlbumListElementActionListener
            elementsListener = new AlbumListElementActionListener() {
                @Override
                public void OnItemClick(DSAlbum id, AlbumsListElement e) {
                    currentAlbum = id;
                    myAL.OnAlbumChange(((e.getText().length() > 32) ? e.getText().substring(0, 29) + "..." : e.getText()), currentAlbum);
                    refresh();
                }

                @Override
                public void OnSave(DSAlbum id, AlbumsListElement e, String newTitlee, String newText) {
                    if (newTitlee.trim().length() < 2) return;
                    
                    List<DSAlbum> list = hibSession
                            .createCriteria(DSAlbum.class)
                            .add(Restrictions.eq("albumID", id.getAlbumID()))
                            .list();
                    
                    if (list.size() > 0) {
                        DSAlbum ds = list.get(0);
                        HibernateUtil.beginTransaction(hibSession);
                        ds.setAlbumName(newTitlee);
                        ds.setAlbumText(newText);
                        hibSession.save(ds);
                        HibernateUtil.commitTransaction(hibSession);
                    }

                    refresh();
                }

                @Override
                public void OnCheck(DSAlbum id, boolean s) { 
                    if (s) selectedItems.add(id); else selectedItems.remove(id);
                }
            };
    
    public void initDB() {
        hibSession = HibernateUtil.getCurrentSession();
    }
    
//    private void _fatalError(String text) {
//        this.getChildren().clear();
//        final GUIElements.SEVBox errBox = new GUIElements.SEVBox(0);
//        final GUIElements.SFLabel errLabel = new GUIElements.SFLabel(text, 1, 9999, 1, 9999, "albumName", "TabAlbumImageList");
//        errBox.setAlignment(Pos.CENTER);
//        errBox.getChildren().add(errLabel);
//        this.getChildren().add(errBox);
//    }

    @SuppressWarnings("LeakingThisInConstructor")
    public AlbumList(AlbumListActionListener al) {
        super();
        
        myAL = al;
        
        GUITools.setStyle(this, "TabAlbumImageList", "container");
        GUITools.setMaxSize(this, 9999, 9999);
        
        albumList.getStyleClass().add("TabAlbumImageList_rootPane_line");
        albumList.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        albumList.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        albumList.setFitToHeight(true);
        albumList.setFitToWidth(true);
        albumList.setContent(container);
        
        this.getChildren().addAll(addAlbum, albumList);
    }
    
    public long getAlbumID() {
        return currentAlbum.getAlbumID();
    }
    
    public long getParentAlbumID() {
        return currentAlbum.getParentAlbumID();
    }
    
    public DSAlbum getAlbum() {
        return currentAlbum;
    }
    
    public long getAlbumsCount() {
        return albumCountInThis;
    }
    
    public final void refresh() {
        container.getChildren().clear();
        albumCountInThis = 0;
        long albumID = (currentAlbum == null) ? 0 : currentAlbum.getAlbumID();
        
        List<DSAlbum> list = hibSession
                .createCriteria(DSAlbum.class)
                .add(Restrictions.eq("parentAlbumID", albumID))
                .list();
        
        for (DSAlbum ds : list) {
            final AlbumsListElement ale = new AlbumsListElement(ds, elementsListener, globalDialogMode);
            if (globalDialogMode)
                ale.setSelected(selectedItems.contains(ds));
            container.getChildren().add(ale);
            albumCountInThis++;
        }
        
        myAL.OnListCompleted(albumCountInThis, currentAlbum);
    }
    
    public final ArrayList<DSAlbum> getSelected() {
        return selectedItems;
    }
    
    public final void clearSelected() {
        selectedItems.clear();
    }
    
    public final void setDialogMode(boolean b) {
        globalDialogMode = b;
    }
    
    public final void levelUp() {
        if (currentAlbum.getParentAlbumID() > 0) {
            List<DSAlbum> list = hibSession
                    .createCriteria(DSAlbum.class)
                    .add(Restrictions.eq("albumID", currentAlbum.getAlbumID()))
                    .list();

            if (list.size() > 0) {
                DSAlbum ds = list.get(0);
                currentAlbum = ds;
                String albumNameStr = ds.getAlbumName();
                myAL.OnAlbumChange((albumNameStr.length() > 32) ? albumNameStr.substring(0, 29) + "..." : albumNameStr, currentAlbum);
            }
        } else {
            DSAlbum d = new DSAlbum(Lang.TabAlbumImageList_root_album, "", 0);
            d.setAlbumID(0);
            currentAlbum = d;
            myAL.OnAlbumChange(Lang.TabAlbumImageList_root_album, d);
        }
        refresh();
    }
}
