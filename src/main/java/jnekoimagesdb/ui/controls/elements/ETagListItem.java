package jnekoimagesdb.ui.controls.elements;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import jnekoimagesdb.domain.DSTag;
import jnekoimagesdb.ui.GUITools;

public class ETagListItem extends VBox {
    public static final Image
            ICON24_EDIT     = GUITools.loadIcon("pencil-24"),
            ICON24_PLUS     = GUITools.loadIcon("plus-24"),
            ICON24_MINUS    = GUITools.loadIcon("minus-24"),
            ICON24_SAVE     = GUITools.loadIcon("tick-24"),
            ICON24_DELETE   = GUITools.loadIcon("times-24");
    
    public static final int
            ITEM_SIZE = 24,
            BTN_EDIT = 1,
            BTN_PLUS = 2,
            BTN_MINUS = 3,
            BTN_DELETE = 4;

    private DSTag
            tag;
    
    private final GUIActionListener
            aListener = (a, b) -> {
            
            };
    
    private final SButton
            editBtn = new SButton(ICON24_EDIT, BTN_EDIT, ITEM_SIZE, aListener, "tli_null_btn"),
            plusBtn = new SButton(ICON24_PLUS, BTN_PLUS, ITEM_SIZE, aListener, "tli_null_btn"),
            minusBtn = new SButton(ICON24_MINUS, BTN_MINUS, ITEM_SIZE, aListener, "tli_null_btn"),
            delBtn = new SButton(ICON24_DELETE, BTN_DELETE, ITEM_SIZE, aListener, "tli_null_btn");
    
    private final Label
            tagName = new Label();
    
    private final TextField
            tagEditBox = new TextField();
    
    private boolean 
            isTagAddedToSearchList = false,
            
            
            isEditActive = false,
            isAREMode = false,
            isMinus = false;
    
    public ETagListItem(DSTag _tag, boolean _isTagAddedToSearchList, boolean _isMinus) {
        super(0);
        tag = _tag;
        isTagAddedToSearchList = _isTagAddedToSearchList;
        isMinus = _isMinus;
        tagName.setText(tag.getTagName());
        init();
        setStyle();
    }
    
    private void init() {
        this.setMinSize(ITEM_SIZE * 4, ITEM_SIZE);
        this.setPrefSize(ITEM_SIZE * 4, ITEM_SIZE);
        this.setMaxSize(9999, ITEM_SIZE);
        GUITools.setStyle(this, "ETagListItem", "root_box");
        
        tagName.setMinSize(ITEM_SIZE, ITEM_SIZE);
        tagName.setPrefSize(ITEM_SIZE, ITEM_SIZE);
        tagName.setMaxSize(9999, ITEM_SIZE);
        GUITools.setStyle(tagName, "ETagListItem", "tagname");
        
        tagEditBox.setMinSize(ITEM_SIZE, ITEM_SIZE);
        tagEditBox.setPrefSize(ITEM_SIZE, ITEM_SIZE);
        tagEditBox.setMaxSize(9999, ITEM_SIZE);
        GUITools.setStyle(tagEditBox, "ETagListItem", "tagname_editor");
        
        if (isTagAddedToSearchList) {
            
            
            
        }
        
        
        
    }
    
    private void setStyle() {
        this.getStyleClass().removeAll("root_box_red", "root_box_green", "root_box_blue");
        if (!isTagAddedToSearchList) {
            this.getStyleClass().add("root_box_blue");
            this.getChildren().clear();
            if (isEditActive) {
                editBtn.setIcon(ICON24_SAVE);
                this.getChildren().addAll(delBtn, editBtn, tagEditBox);
            } else {
                editBtn.setIcon(ICON24_EDIT);
                this.getChildren().addAll(minusBtn, plusBtn, editBtn, tagName);
            }
        } else {
            this.getStyleClass().add(isMinus ? "root_box_red" : "root_box_green");
            this.getChildren().clear();
            this.getChildren().addAll(delBtn, tagName);
        }
    }
    
    
    
}
