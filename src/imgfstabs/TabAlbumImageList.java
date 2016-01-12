package imgfstabs;

import dataaccess.Lang;
import imgfs.ImgFS;
import imgfsgui.GUIElements.SScrollPane;
import imgfsgui.GUIElements.STabTextButton;
import imgfsgui.GUIElements.SEVBox;
import imgfsgui.GUIElements.SFHBox;
import imgfsgui.GUIElements.SFLabel;
import imgfsgui.GUIElements.SFVBox;
import imgfsgui.GUIElements.STextArea;
import imgfsgui.ToolsPanelTop;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import jnekoimagesdb.GUITools;

public class TabAlbumImageList extends SEVBox {
    public static final int
            HEADER_VSIZE = 27,
            HBUTTON_HSIZE = 150,
            ALBTITLE_HSIZE = 210,
            ADD_NEW_ELEMENT_VSIZE = 32,
            
            BTN_LVL_UP = 1;
    
    public static final Image 
            ALBUM_DEFAULT = GUITools.loadIcon("dir-normal-128"),
            IMG24_LEVEL_UP = GUITools.loadIcon("lvlup-48"),
            SAVE_16 = GUITools.loadIcon("save-16"),
            EDIT_16 = GUITools.loadIcon("edit-16");
    
    
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
            });

            GUITools.setStyle(newItemName, "AddNewAlbumElement", "newItemName");
            GUITools.setMaxSize(newItemName, 9999, ADD_NEW_ELEMENT_VSIZE);

            this.getChildren().addAll(newTitle, newItemName, addBtn);
        }
    }
    
    public static interface AlbumListElementActionListener {
        void OnItemClick(Long id, AlbumsListElement e);
        void OnSave(Long id, AlbumsListElement e, String newTitle, String newText);  
    }
    
    public static class AlbumsListElement extends SFHBox {
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
                editMode = false;
        
        private final SFVBox 
                elementContainer = new SFVBox(0, 128, 9999, 128, 128);
        
        private final SFHBox 
                titleContainer = new SFHBox(4, 128, 9999, 24, 24);
        
        private final STextArea
                albumText = new STextArea(128, 9999, 90, 90, "albumText");

        private final ImageView
                icon_d = new ImageView(ALBUM_DEFAULT),
                save_i = new ImageView(SAVE_16),
                edit_i = new ImageView(EDIT_16);

        private final Button
                saveBtn = new Button(Lang.NullString, edit_i);

        public AlbumsListElement(Long id, Long pid, String xtitle, String xtext, AlbumListElementActionListener al) {
            super(16, 128, 9999, 128, 128);
            this.getStyleClass().add("AlbumsListElement_rootPane");

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
            titleContainer.getChildren().addAll(titleLabel, saveBtn);
            elementContainer.getChildren().addAll(titleContainer, albumText);
        }
    }

    private final STabTextButton 
            album, images;
    
    private final SScrollPane
            albumList = new SScrollPane();
    
    private final Pane
            topToolbar;
    
    private final SEVBox 
            container = new SEVBox();
    
    private final SFLabel
            bottomPanelForAlbums = new SFLabel("Статистика альбома", 128, 9999, 24, 24, "bottomPanelForAlbums", "TabAlbumImageList"),
            albumName = new SFLabel(Lang.TabAlbumImageList_root_album, ALBTITLE_HSIZE, ALBTITLE_HSIZE, HEADER_VSIZE, HEADER_VSIZE, "albumName", "TabAlbumImageList");
    
    private long 
            albumID = 0, 
            albumParentID = 0, 
            albumCountInThis = 0;
    
    private Connection
            conn = null;
    
    private final ToolsPanelTop 
            panelTop;
    
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
                _clear();
                _album();
            }, 0);
    
    private final AlbumListElementActionListener
            elementsListener = new AlbumListElementActionListener() {
                @Override
                public void OnItemClick(Long id, AlbumsListElement e) {
                    albumID = id;
                    albumParentID = e.parent;
                    albumName.setText((e.getText().length() > 32) ? e.getText().substring(0, 29) + "..." : e.getText());
                    _clear();
                    _album();
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
                    _clear();
                    _album();
                }
            };
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TabAlbumImageList(Pane _topToolbar) {
        super(0, 9999, 9999);
                
        topToolbar = _topToolbar;
        
        final HBox header = new HBox();
        GUITools.setStyle(header, "TabAlbumImageList", "header");
        header.setMaxSize(9999, HEADER_VSIZE);
        header.setPrefSize(9999, HEADER_VSIZE);
        header.setMinSize(HBUTTON_HSIZE * 3, HEADER_VSIZE);
        header.setAlignment(Pos.CENTER);
        
        GUITools.setStyle(container, "TabAlbumImageList", "container");
        GUITools.setMaxSize(container, 9999, 9999);
        
        album = new STabTextButton(Lang.AlbumImageList_Albums, 1, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            _clear();
            _album();
        });
        
        images = new STabTextButton(Lang.AlbumImageList_Images, 2, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            _clear();
            _images();
        });
        
        panelTop = new ToolsPanelTop((index) -> {
            switch (index) {
                case BTN_LVL_UP:
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
                                    albumName.setText((albumNameStr.length() > 32) ? albumNameStr.substring(0, 29) + "..." : albumNameStr);
                                }
                            }
                        } catch (SQLException ex) { }
                    } else {
                        albumName.setText(Lang.TabAlbumImageList_root_album);
                        albumID = 0;
                    }
                    
                    _clear();
                    _album();
                    break;
            }
        });
        topToolbar.getChildren().add(panelTop);

        albumName.setAlignment(Pos.CENTER);
        header.getChildren().addAll(albumName, GUITools.getSeparator(), album, GUITools.getSeparator(4), images);
        albumList.getStyleClass().add("TabAlbumImageList_rootPane_line");
        albumList.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        albumList.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        //albumList.set
        albumList.setFitToHeight(true);
        albumList.setFitToWidth(true);
        albumList.setContent(container);
        this.getChildren().addAll(header, albumList);
    }
    
    public void initDB() {
        conn = ImgFS.getH2Connection();
        if (conn == null) {
            _fatalError(Lang.TabAlbumImageList_db_error);
        }
        _clear();
        _album();
    }
    
    private void _fatalError(String text) {
        this.getChildren().clear();
        final SEVBox errBox = new SEVBox(0);
        final SFLabel errLabel = new SFLabel(text, 1, 9999, 1, 9999, "albumName", "TabAlbumImageList");
        errBox.setAlignment(Pos.CENTER);
        errBox.getChildren().add(errLabel);
        this.getChildren().add(errBox);
    }
    
    private void _clear() {
        topToolbar.getChildren().clear();
        container.getChildren().clear();
        this.getChildren().remove(albumList);
        this.getChildren().remove(addAlbum);
        panelTop.clearAll();
    }
    
    private void _images() {
        
        
        
        
    }
    
    private void _album() {
        this.getChildren().addAll(addAlbum, albumList);
        albumCountInThis = 0;
        //container.getChildren().add(addAlbum);
        try {
            final PreparedStatement ps = conn.prepareStatement("SELECT * FROM albums WHERE piid=? ORDER BY iid ASC;");
            ps.setLong(1, albumID);
            final ResultSet rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    albumCountInThis++;
                    final AlbumsListElement ale = new AlbumsListElement(
                            rs.getLong("iid"), rs.getLong("piid"), rs.getString("xname"), new String(rs.getBytes("xtext")), elementsListener);
                    container.getChildren().add(ale);
                }
                rs.close();
            }
            
            ps.clearWarnings();
            ps.close();
            
            bottomPanelForAlbums.setText(String.format(Lang.TabAlbumImageList_info_format, albumCountInThis, 0)); 
            if (albumID > 0) {
                if (!topToolbar.getChildren().contains(panelTop)) topToolbar.getChildren().add(panelTop);
                panelTop.addButton(IMG24_LEVEL_UP, BTN_LVL_UP); 
            }
        } catch (SQLException ex) {
            _fatalError(Lang.TabAlbumImageList_db_error);
            Logger.getLogger(TabAlbumImageList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Parent getBottomPanel() {
        return bottomPanelForAlbums;
    }
    
    public void setAlbumTabVisible(boolean v) {
        album.setVisible(v);
    }
    
    public void setAlbumName(String s) {
        album.setText((s.length() < 32) ? s : s.substring(0, 29) + "...");
    }
}
