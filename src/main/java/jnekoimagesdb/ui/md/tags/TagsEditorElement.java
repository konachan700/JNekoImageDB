package jnekoimagesdb.ui.md.tags;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import jnekoimagesdb.domain.DSTag;

public class TagsEditorElement extends HBox {
    private final TagsEditorElementActionListener tagElementAL;
    private final DSTag currTag;
    private final TagElementButton 
            btnDel, btnEditStart, btnEditSave;
    private final Label tagText = new Label();
    private final TextField tagEditor = new TextField();
    
    public TagsEditorElement(DSTag tag, TagsEditorElementActionListener tal) {
        super();
        tagElementAL = tal;
        currTag = tag;
        
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().addAll("tags_element_height", "tags_element_root_pane");
        
        tagText.setAlignment(Pos.CENTER_LEFT);
        tagText.getStyleClass().addAll("tags_element_button_height", "tags_null_pane", "tags_element_text");
        
        tagEditor.setAlignment(Pos.CENTER_LEFT);
        tagEditor.getStyleClass().addAll("tags_element_button_height", "tags_null_pane", "tags_element_text");
        
        btnDel = new TagElementButton("tags_element_button_icon_delete", "Удалить элемент", el -> {
            tagElementAL.OnDelete(currTag);
        });
        
        btnEditStart = new TagElementButton("tags_element_button_icon_edit", "Редактировать элемент", el -> {
            tagEditor.setText(tag.getTagName());
            guiEdit();
        });
        
        btnEditSave = new TagElementButton("tags_element_button_icon_save", "Сохранить изменения", el -> {
            tagElementAL.OnEdit(currTag, tagEditor.getText().trim());
            guiNotEdit();
        });
        
        tagText.setText(currTag.getTagName());
        guiNotEdit();
    }
    
    private void guiNotEdit() {
        this.getChildren().clear();
        this.getChildren().addAll(btnDel, btnEditStart, tagText);
    }
    
    private void guiEdit() {
        this.getChildren().clear();
        this.getChildren().addAll(btnEditSave, tagEditor);
    }
}
