package imgfsgui;

import jnekoimagesdb.Lang;
import imgfs.ImgFS;
import imgfsgui.GUIElements.SScrollPane;
import imgfstabs.TabAlbumImageList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import jnekoimagesdb.GUITools;

public class AlbumList extends GUIElements.SEVBox {
    public static final int
            ADD_NEW_ELEMENT_VSIZE = 32;
    
    private long 
            albumID = 0, 
            albumParentID = 0, 
            albumCountInThis = 0, 
            imagesCount = 0;
    
    public static final Image 
            ALBUM_DEFAULT = GUITools.loadIcon("dir-normal-128"),
            SELECTED_16 = GUITools.loadIcon("selected-16"),
            UNSELECTED_16 = GUITools.loadIcon("unselected2-16"),
            SAVE_16 = GUITools.loadIcon("save-16"),
            EDIT_16 = GUITools.loadIcon("edit-16");
    
    private final GUIElements.SEVBox
            container = new GUIElements.SEVBox();
    
    private final SScrollPane
            albumList = new SScrollPane();
    
    private Connection
            conn = null;
    
    private boolean
            globalDialogMode = false;
    
    private final ArrayList<Long>
            selectedItems = new ArrayList<>();
    
    public static interface AlbumListActionListener {
        void OnAlbumChange(String newAlbumName, long ID, long PID);
        void OnListCompleted(long count, long ID, long PID);
    }
    
    private final AlbumListActionListener
            myAL;

    public static interface AddNewAlbumActionListener {
        void OnNew(long parent, String title);
    }
    
    public static class AddNewAlbumElement extends GUIElements.SFHBox {
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
        void OnItemClick(Long id, AlbumsListElement e);
        void OnSave(Long id, AlbumsListElement e, String newTitle, String newText); 
        void OnCheck(Long id, boolean state);
    }

    public static class AlbumsListElement extends GUIElements.SFHBox {
        public  final Long  
                ID, 
                parent;

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
        
        private final GUIElements.SFVBox 
                elementContainer = new GUIElements.SFVBox(0, 128, 9999, 128, 128);
        
        private final GUIElements.SFHBox 
                titleContainer = new GUIElements.SFHBox(4, 128, 9999, 24, 24);
        
        private final GUIElements.STextArea
                albumText = new GUIElements.STextArea(128, 9999, 90, 90, "albumText");

        private final ImageView
                icon_d = new ImageView(ALBUM_DEFAULT),
                selected_i = new ImageView(SELECTED_16),
                unselected_i = new ImageView(UNSELECTED_16),
                save_i = new ImageView(SAVE_16),
                edit_i = new ImageView(EDIT_16);

        private final Button
                saveBtn = new Button(Lang.NullString, edit_i),
                selectBtn = new Button(Lang.NullString, unselected_i);

        public AlbumsListElement(Long id, Long pid, String xtitle, String xtext, AlbumListElementActionListener al, boolean dm) {
            super(16, 128, 9999, 128, 128);
            this.getStyleClass().add("AlbumsListElement_rootPane");

            dialogMode  = dm;
            ID          = id;
            elementAL   = al;
            parent      = pid;

            title.setText(xtitle);
            titleLabel.setText(xtitle);
            albumText.setText(xtext);

            _init();
        }
        
        public String getText() {
            return title.getText();
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
                        elementAL.OnSave(ID, this, title.getText().trim(), albumText.getText());
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
                elementAL.OnCheck(ID, checked);
            });
            
            GUITools.setMaxSize(titleLabel, 9999, 16);
            GUITools.setStyle(titleLabel, "AlbumsListElement", "titleLabel");
            titleLabel.setAlignment(Pos.CENTER_LEFT);
            this.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    elementAL.OnItemClick(ID, this);
                    event.consume();
                }
            });
            
            albumText.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    elementAL.OnItemClick(ID, this);
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
                try {
                    final PreparedStatement ps = conn.prepareStatement("INSERT INTO albums VALUES (default, ?, ?, ?, 0)");
                    ps.setLong(1, albumID);
                    ps.setString(2, title);
                    ps.setBytes(3, " ".getBytes());
                    ps.execute();
                    ps.clearWarnings();
                    ps.close();
                } catch (SQLException ex) {
                    _fatalError(Lang.TabAlbumImageList_db_error);
                    Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
                }
                refresh();
            }, 0);
    
    private final AlbumListElementActionListener
            elementsListener = new AlbumListElementActionListener() {
                @Override
                public void OnItemClick(Long id, AlbumsListElement e) {
                    albumID = id;
                    albumParentID = e.parent;
                    myAL.OnAlbumChange(((e.getText().length() > 32) ? e.getText().substring(0, 29) + "..." : e.getText()), albumID, albumParentID);
                    refresh();
                }

                @Override
                public void OnSave(Long id, AlbumsListElement e, String newTitlee, String newText) {
                    if (newTitlee.trim().length() < 2) return;
                    try {
                        final PreparedStatement ps = conn.prepareStatement("UPDATE albums SET xname=?, xtext=? WHERE iid=?");
                        ps.setString(1, newTitlee);
                        ps.setBytes(2, newText.getBytes());
                        ps.setLong(3, id);
                        ps.execute();
                        ps.clearWarnings();
                        ps.close();
                    } catch (SQLException ex) {
                        _fatalError(Lang.TabAlbumImageList_db_error);
                        Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    refresh();
                }

                @Override
                public void OnCheck(Long id, boolean s) { 
                    if (s) selectedItems.add(id); else selectedItems.remove(id);
                }
            };
    
    public void initDB() {
        if (conn != null) return;
        
        conn = ImgFS.getH2Connection();
        if (conn == null) {
            _fatalError(Lang.TabAlbumImageList_db_error);
        }
//        refresh();
    }
    
    private void _fatalError(String text) {
        this.getChildren().clear();
        final GUIElements.SEVBox errBox = new GUIElements.SEVBox(0);
        final GUIElements.SFLabel errLabel = new GUIElements.SFLabel(text, 1, 9999, 1, 9999, "albumName", "TabAlbumImageList");
        errBox.setAlignment(Pos.CENTER);
        errBox.getChildren().add(errLabel);
        this.getChildren().add(errBox);
    }

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
        return albumID;
    }
    
    public long getParentAlbumID() {
        return albumParentID;
    }
    
    public long getAlbumsCount() {
        return albumCountInThis;
    }
    
    public final void refresh() {
        container.getChildren().clear();
        albumCountInThis = 0;
        try {
            final PreparedStatement ps = conn.prepareStatement("SELECT * FROM albums WHERE piid=? ORDER BY iid ASC;");
            ps.setLong(1, albumID);
            final ResultSet rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    albumCountInThis++;
                    final Long iid_n = rs.getLong("iid");
                    final AlbumsListElement ale = new AlbumsListElement(
                            iid_n, rs.getLong("piid"), rs.getString("xname"), new String(rs.getBytes("xtext")), elementsListener, globalDialogMode);
                    if (globalDialogMode) {
                        ale.setSelected(selectedItems.contains(iid_n));
                    }
                    container.getChildren().add(ale);
                }
                rs.close();
            }
            
            ps.clearWarnings();
            ps.close();
            
            myAL.OnListCompleted(albumCountInThis, albumID, albumParentID);
        } catch (SQLException ex) {
            _fatalError(Lang.TabAlbumImageList_db_error);
            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public final ArrayList<Long> getSelected() {
        return selectedItems;
    }
    
    public final void clearSelected() {
        selectedItems.clear();
    }
    
    public final void setDialogMode(boolean b) {
        globalDialogMode = b;
    }
    
    public final void levelUp() {
        if (albumParentID > 0) {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM albums WHERE iid=?;");
                ps.setLong(1, albumParentID);
                ResultSet rs = ps.executeQuery();
                if (rs != null) {
                    if (rs.next()) {
                        albumID = rs.getLong("iid");
                        albumParentID = rs.getLong("piid");
                        String albumNameStr = rs.getString("xname");
                        myAL.OnAlbumChange((albumNameStr.length() > 32) ? albumNameStr.substring(0, 29) + "..." : albumNameStr, albumID, albumParentID);
                    }
                }
            } catch (SQLException ex) { }
        } else {
            myAL.OnAlbumChange(Lang.TabAlbumImageList_root_album, albumID, albumParentID);
            albumID = 0;
        }
        refresh();
    }
}
