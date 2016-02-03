package jnekoimagesdb.ui.controls.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.GUITools;

public class ETagListItem extends HBox {
    public static final Image
            ICON24_EDIT     = GUITools.loadIcon("pencil-16"),
            ICON24_PLUS     = GUITools.loadIcon("plus-16"),
            ICON24_MINUS    = GUITools.loadIcon("minus-16"),
            ICON24_SAVE     = GUITools.loadIcon("tick-16"),
            ICON24_DELETE   = GUITools.loadIcon("times-16");
    
    public static final int
            ITEM_SIZE = 24,
            BTN_EDIT = 1,
            BTN_PLUS = 2,
            BTN_MINUS = 3,
            BTN_DELETE = 4;

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
                    case BTN_EDIT:
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
                    case BTN_DELETE:
                        actListener.onDelete(tag);
                        break;
                    case BTN_PLUS:
                        actListener.onAddToListBtnClick(tag, false);
                        break;
                    case BTN_MINUS:
                        actListener.onAddToListBtnClick(tag, true);
                        break;
                }
            };
    
    private final SButton
            editBtn = new SButton(ICON24_EDIT, BTN_EDIT, 16, aListener, "tli_null_btn"),
            plusBtn = new SButton(ICON24_PLUS, BTN_PLUS, 16, aListener, "tli_null_btn"),
            minusBtn = new SButton(ICON24_MINUS, BTN_MINUS, 16, aListener, "tli_null_btn"),
            delBtn = new SButton(ICON24_DELETE, BTN_DELETE, 16, aListener, "tli_null_btn");

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
            this.getStyleClass().add("ETagListItem_root_box_blue");
            this.getChildren().clear();
            if (isEditActive) {
                editBtn.setIcon(ICON24_SAVE);
                this.getChildren().addAll(delBtn, editBtn, tagEditBox);
            } else {
                editBtn.setIcon(ICON24_EDIT);
                this.getChildren().addAll(minusBtn, plusBtn, editBtn, tagName);
            }
        } else {
            this.getStyleClass().add(isMinus ? "ETagListItem_root_box_red" : "ETagListItem_root_box_green");
            this.getChildren().clear();
            this.getChildren().addAll(delBtn, tagName);
        }
    }
}
