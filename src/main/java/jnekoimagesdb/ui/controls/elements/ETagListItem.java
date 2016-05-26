package jnekoimagesdb.ui.controls.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.GUITools;

public class ETagListItem extends HBox {   
    public static final int
            ITEM_SIZE = 24;

    private DSTag
            tag;
    
    private final Label
            tagName = new Label();
    
    private final TextField
            tagEditBox = new TextField();
    
    private ETagListItemActionListener
            actListener = null;
    
    private boolean 
            isTagAddedToSearchList = false,
            isEditActive = false,
            isAREMode = false,
            isMinus = false;
    
    private final GUIActionListener
            aListener = (a, b) -> {
                switch (b) {
                    case buttonEdit:
                        isEditActive = !isEditActive;
                        setStyle();
                        if (!isEditActive) {
                            tag.setTagName(tagEditBox.getText().trim());
                            tagName.setText(tagEditBox.getText().trim());
                            actListener.onEditComplete(tag);
                        } else {
                            tagEditBox.setText(tag.getTagName());
                        }
                        break;
                    case buttonDelete:
                        actListener.onDelete(tag);
                        break;
                    case buttonPlus:
                        actListener.onAddToListBtnClick(tag, false);
                        break;
                    case buttonMinus:
                        actListener.onAddToListBtnClick(tag, true);
                        break;
                }
            };
    
    private final SButton
            editBtn = new SButton(GUITools.loadIcon("pencil-16"), ElementsIDCodes.buttonEdit, 16, aListener, "tli_null_btn"),
            plusBtn = new SButton(GUITools.loadIcon("plus-16"), ElementsIDCodes.buttonPlus, 16, aListener, "tli_null_btn"),
            minusBtn = new SButton(GUITools.loadIcon("minus-16"), ElementsIDCodes.buttonMinus, 16, aListener, "tli_null_btn"),
            delBtn = new SButton(GUITools.loadIcon("times-16"), ElementsIDCodes.buttonDelete, 16, aListener, "tli_null_btn");

    public ETagListItem(DSTag _tag, boolean _isTagAddedToSearchList, boolean _isMinus, ETagListItemActionListener al) {
        super(0);
        tag = _tag;
        actListener = al;
        isTagAddedToSearchList = _isTagAddedToSearchList;
        isMinus = _isMinus;
        tagName.setText(tag.getTagName());
        init();
        setStyle();
    }
    
    public final DSTag getTag() {
        return tag;
    }
    
    public void setTag(DSTag t) {
        tag = t;
        tagName.setText(tag.getTagName());
    }
    
    private void init() {
        this.setAlignment(Pos.CENTER);
        this.setMinSize(Region.USE_PREF_SIZE, ITEM_SIZE);
        GUITools.setStyle(this, "ETagListItem", "root_box");
        
        tagName.setMinSize(Region.USE_PREF_SIZE, ITEM_SIZE);
        GUITools.setStyle(tagName, "ETagListItem", "tagname");
        tagName.setOnMouseClicked((c) -> {
            actListener.onClick(tag);
        });
        
        tagEditBox.setMinSize(Region.USE_PREF_SIZE, ITEM_SIZE);
        GUITools.setStyle(tagEditBox, "ETagListItem", "tagname_editor");
    }
    
    private void setStyle() {
        this.getStyleClass().removeAll("ETagListItem_root_box_red", "ETagListItem_root_box_green", "ETagListItem_root_box_blue");
        if (!isTagAddedToSearchList) {
            this.getStyleClass().add("ETagListItem_root_box_red");
            this.getChildren().clear();
            if (isEditActive) {
                editBtn.setIcon(GUITools.loadIcon("tick-16"));
                this.getChildren().addAll(editBtn, tagEditBox);
            } else {
                editBtn.setIcon(GUITools.loadIcon("pencil-16"));
                this.getChildren().addAll(delBtn, editBtn, tagName);
                //this.getChildren().addAll(minusBtn, plusBtn, editBtn, tagName);
            }
        } else {
            this.getStyleClass().add(isMinus ? "ETagListItem_root_box_blue" : "ETagListItem_root_box_green");
            this.getChildren().clear();
            this.getChildren().addAll(delBtn, tagName);
        }
    }
}
