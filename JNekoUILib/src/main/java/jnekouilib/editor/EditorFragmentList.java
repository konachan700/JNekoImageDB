package jnekouilib.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import jnekouilib.anno.UIListItem;
import jnekouilib.anno.UIListItemHeader;
import jnekouilib.fragment.Fragment;
import jnekouilib.utils.MessageBus;
import jnekouilib.utils.MessageBusActions;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EditorFragmentList extends Fragment {

    public ElementListLink getParentElement() {
        return parentElement;
    }

    public void setParentElement(ElementListLink parentElement) {
        this.parentElement = parentElement;
    }
    public static interface FragmentListSaver {
        public void onSave(String uiName, Object currentItem);
    }
    
    private class FragmentListHelper {
        public Method getterForName;
        public Collection allData;
        public Collection selData;
        public Object currentItem;
        public Parent uiElement;
        public String uiName;
    }
    
    private ElementListLink
            parentElement = null;
    
    private final ArrayList<FragmentListHelper>
            flHelper = new ArrayList<>();
    
    private FragmentListHelper
            currItem = null;
    
    private final ScrollPane
            elementsSP= new ScrollPane();
    
    private final VBox
            vContainer = new VBox();
    
     private final HBox
            vYesNoContainer = new HBox();
    
    private final Button
//            bYes = new Button("Save"),
            bNo = new Button("Back");
    
    private boolean
//            isYesNoHeaderPresent = true,
            isMultiselect = false;
    
    private EditorFragmentListActionListener
            actionListener = null;
    
//    private Collection 
//            selectedItems = new HashSet();
    
    private final Label
            lNoItems = new Label("No items in this list");
    
    public boolean isMultiselectEnable() {
        return isMultiselect;
    }
    
//    public FragmentList(boolean isYesNoHeaderPresent, boolean isMultiselect) {
    public EditorFragmentList(boolean isMultiselect) {
//        this.isYesNoHeaderPresent = (isMultiselect) ? true : isYesNoHeaderPresent;  
        this.isMultiselect = isMultiselect;
        
        this.getStyleClass().addAll("maxHeight", "maxWidth");
        
        lNoItems.getStyleClass().addAll("StringFieldElementLabel", "maxWidth");

        elementsSP.getStyleClass().addAll("maxWidth", "maxHeight", "ScrollPane");
        elementsSP.setContent(vContainer);
        elementsSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        elementsSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        elementsSP.setFitToWidth(true);
        elementsSP.setFitToHeight(false);
        
        if (isMultiselect) {
//            bYes.getStyleClass().addAll("yesno_button");
//            bYes.setOnAction(c -> {
//                //onYesClicked();
//                if (actionListener != null) 
//                    actionListener.OnListYesClick(selectedItems);
//                super.back();
//            });
            
            bNo.getStyleClass().addAll("yesno_button");
            bNo.setOnAction(c -> {
                if (actionListener != null) 
                    actionListener.OnListNoClick(this);
                super.back();
            });
            
            final VBox sep1 = new VBox();
            sep1.getStyleClass().addAll("maxWidth");
            
            vYesNoContainer.getStyleClass().addAll("ynbox1", "maxWidth");
            //vYesNoContainer.getChildren().addAll(bNo, sep1, bYes);
            vYesNoContainer.getChildren().addAll(bNo, sep1);
            super.getChildren().add(vYesNoContainer);
        }
        
        super.getChildren().add(elementsSP);
    }
    
    private void setNoItems() {
        vContainer.getChildren().addAll(lNoItems);
    }
    
    public void saveCollection(FragmentListSaver fls, String refName) {
        if (fls == null) return;
        if (isMultiselect) {
            flHelper.forEach(el -> {
                if (el.uiElement instanceof CheckBox) {
                    if (((CheckBox) el.uiElement).isSelected()) fls.onSave(el.uiName, el.currentItem);
                }
            });
            MessageBus.sendMessage(MessageBusActions.EditorFragmentListRefresh,  refName);
        } else {
            if (currItem != null) {
                fls.onSave(currItem.uiName, currItem.currentItem);
                MessageBus.sendMessage(MessageBusActions.EditorFragmentListRefresh,  refName);
            }
        }
    }
    
    public void readCollection(Collection selectedList, Collection fullList, EditorFragmentListActionListener fl) {
        actionListener = fl;
        vContainer.getChildren().clear();
        
        if ((fullList == null) || (selectedList == null)) {
            setNoItems();
            return;
        }
        
        if (fullList.isEmpty()) {
            setNoItems();
            return;
        }
                
        fullList.forEach(el -> {
            if (el.getClass().isAnnotationPresent(UIListItem.class)) {
                final Method[] methods = el.getClass().getMethods();
                if (methods == null) return;
                if (methods.length == 0) return;
                
                for (Method m : methods) {
                    if (m.isAnnotationPresent(UIListItemHeader.class)) {
                        if (m.getReturnType().equals(String.class)) {
                            String itemName = null;
                            try {
                                itemName = (String) m.invoke(el);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            final FragmentListHelper f = new FragmentListHelper();
                            f.uiName = itemName;

                            if (isMultiselect) {
                                final CheckBox check = new CheckBox();
                                check.getStyleClass().addAll("checkBoxList", Editor.maxWidthStyle);
                                check.setSelected(selectedList.contains(el)); 
                                if (itemName != null) check.setText(itemName);
                                vContainer.getChildren().add(check);
                                f.uiElement = check;
                            } else {
                                final Label lItem = new Label();
                                lItem.getStyleClass().addAll("labelList", Editor.maxWidthStyle);
                                lItem.setOnMouseClicked(event -> {
                                    flHelper.forEach(el1 -> {
                                        if (event.getSource().equals(el1.uiElement)) {
                                            currItem = el1;
                                            //System.out.println("TEST: " + el1.uiName);
                                        } 
                                    });
//                                    if (actionListener != null) {
//                                        actionListener.OnListYesClick(selectedItems);
//                                    }
                                    if (actionListener != null) 
                                        actionListener.OnListNoClick(this);
                                    event.consume();
                                    super.back();
                                });
                                if (itemName != null) lItem.setText(itemName);
                                vContainer.getChildren().add(lItem);
                                f.uiElement = lItem;
                            }

                            f.allData = fullList;
                            f.currentItem = el;
                            f.getterForName = m;
                            f.selData = selectedList;
                            flHelper.add(f);
                            
                            break;
                        }
                    }
                }
            }
        });
    }
}
