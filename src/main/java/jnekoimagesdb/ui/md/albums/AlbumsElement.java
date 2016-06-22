package jnekoimagesdb.ui.md.albums;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.ui.md.tags.TagElementButton;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;

public class AlbumsElement extends HBox {
    private final AlbumsElementActionListener elementAL;
    private final DSAlbum dsAlbum;
    
    private final VBox 
            rootContainer = new VBox();
    
    private final HBox 
            titleContainer = new HBox(),
            titleEditorContainer = new HBox();
    
    private final Label 
            title = new Label(),
            text = new Label();
    
    private final TextArea
            albumText = new TextArea();
    
    private final TextField 
            titleEditor = new TextField();
    
    private final TagElementButton
            editBtn, saveBtn, deleteBtn;
    
    private boolean 
            selected = false,
            buttonsEnable = true;
    
    private final TopPanelButton 
            navigateToImg;
    
    public final AlbumsElement setDeleteMode() {
        buttonsEnable = false;
        
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().addAll("albums_delete_element_icon");
        navigateToImg.setGraphic(iconNode); 
        
        titleContainer.getChildren().clear();
        titleContainer.getChildren().addAll(title);
        return this;
    }
    
    public final AlbumsElement setSelectMode() {
        buttonsEnable = false;
        
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().addAll("albums_add_to_element_icon");
        navigateToImg.setGraphic(iconNode); 
        
        titleContainer.getChildren().clear();
        titleContainer.getChildren().addAll(title);
        return this;
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    public AlbumsElement(DSAlbum d, AlbumsElementActionListener al) {
        super();
        dsAlbum = d;
        elementAL = al;
        
        this.setAlignment(Pos.CENTER);
        
        navigateToImg = new TopPanelButton("albums_element_to_img_icon", "Перейти к просмотру альбома", c -> {
            if (c.getClickCount() == 1) elementAL.OnToImagesButtonClick(dsAlbum, c);
        });
        this.setOnMouseClicked(c -> {
            if (c.getClickCount() == 2) elementAL.OnDoubleClick(this, dsAlbum, c); 
            if (c.getClickCount() == 1) elementAL.OnClick(this, dsAlbum, c);
        });
        
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().addAll("albums_element_icon");
        
        rootContainer.setAlignment(Pos.CENTER);
        rootContainer.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_null_pane");
        this.getChildren().addAll(iconNode, rootContainer, navigateToImg);
        
        titleContainer.setAlignment(Pos.CENTER);
        titleContainer.getStyleClass().addAll("albums_max_width", "albums_title_conainer_height", "albums_null_pane");
        
        titleEditorContainer.setAlignment(Pos.CENTER);
        titleEditorContainer.getStyleClass().addAll("albums_max_width", "albums_title_conainer_height", "albums_null_pane");
        
        title.setText(d.getAlbumName());
        title.setAlignment(Pos.CENTER_LEFT);
        title.getStyleClass().addAll("albums_max_width", "albums_title_conainer_height", "albums_null_pane", "album_element_title_font");
        
        titleEditor.setAlignment(Pos.CENTER_LEFT);
        titleEditor.getStyleClass().addAll("albums_max_width", "albums_title_conainer_height", "albums_null_pane", "album_element_title_font");
        
        text.setText(d.getAlbumText());
        text.setAlignment(Pos.TOP_LEFT);
        text.setWrapText(true);
        text.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_null_pane", "album_element_text_font");
        
        albumText.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_element_textarera");
        
        deleteBtn = new TagElementButton("albums_element_button_icon_delete", "Удалить элемент", el -> {
            elementAL.OnDelete(dsAlbum);
        });
        
        saveBtn = new TagElementButton("albums_element_button_icon_save", "Сохранить изменения", el -> {
            elementAL.OnEdit(dsAlbum, titleEditor.getText(), albumText.getText());
            editModeOff();
        });
        
        editBtn = new TagElementButton("albums_element_button_icon_edit", "Редактировать элемент", el -> {
            editModeOn();
        });
        
        titleContainer.getChildren().addAll(title,/* deleteBtn,*/ editBtn);
        titleEditorContainer.getChildren().addAll(titleEditor, saveBtn);
        
        editModeOff();
        setSelected(false);
    }
    
    public final void setSelected(boolean sel) {
        if (buttonsEnable == false) sel = false;
        
        deleteBtn.setVisible(sel);
        editBtn.setVisible(sel);
        selected = sel;
        this.getStyleClass().clear();
        if (selected) 
            this.getStyleClass().addAll("albums_max_width", "albums_element_height", "albums_element_root_pane", "albums_element_root_pane_selected");
        else
            this.getStyleClass().addAll("albums_max_width", "albums_element_height", "albums_element_root_pane", "albums_element_root_pane_non_selected");
    }
    
    private void editModeOn() {
        if (buttonsEnable == false) return;
        
        albumText.setText(dsAlbum.getAlbumText());
        titleEditor.setText(dsAlbum.getAlbumName()); 
        rootContainer.getChildren().clear();
        rootContainer.getChildren().addAll(titleEditorContainer, albumText);
        albumText.setEditable(true);
    }
    
    private void editModeOff() {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().addAll(titleContainer, text);
        albumText.setEditable(false);
    }
}
