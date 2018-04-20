package jnekouilib.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import jnekouilib.anno.UICollection;
import jnekouilib.anno.UIListItem;
import jnekouilib.anno.UIListItemHeader;
import jnekouilib.utils.MessageBus;
import jnekouilib.utils.MessageBusActions;
import jnekouilib.utils.UIUtils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;

public class ElementListLink extends HBox implements EditorTypeLabeled {
    private EditorFragmentList
            parentFL = null;
    
    private final VBox 
            container = new VBox(),
            listContainer = new VBox();
    
    private final Label
            textLabel = new Label();
    
    private final IconNode 
            icon = new IconNode();
    
    private Object myObject = null;
    private Method myMethod = null;

    public ElementListLink(String collectionRefName, Object obj, Method m) {
        myObject = obj;
        myMethod = m;
        
        super.getStyleClass().addAll("eStringFieldElementRoot", Editor.maxWidthStyle);
        textLabel.getStyleClass().addAll("labelListOnEditor", Editor.maxWidthStyle);
        icon.getStyleClass().addAll("iconListOnEditor");
        
        super.setAlignment(Pos.TOP_CENTER);
        super.getChildren().addAll(icon, container);
        container.getChildren().addAll(textLabel, listContainer);
        
        listContainer.setAlignment(Pos.TOP_CENTER);
        listContainer.getStyleClass().addAll("eStringFieldElementsListSmall", Editor.maxWidthStyle);
        
        MessageBus.registerMessageReceiver( 
                MessageBusActions.EditorFragmentListRefresh, 
                (b, objects) -> {
            final String msg = UIUtils.getStringFromObject(0, objects);
            if (msg == null) return;
            if (msg.equalsIgnoreCase(collectionRefName)) create();
        });
    }
    
    public void create() {
        listContainer.getChildren().clear();
        final StringBuilder sb = new StringBuilder();

        if (myObject == null) return;
        if (myMethod == null) return;
        if (!myMethod.isAnnotationPresent(UICollection.class)) return;
        
        try {
            final Collection annoRetVal = (Collection) myMethod.invoke(myObject);
            if (annoRetVal == null) return;
            if (annoRetVal.size() <= 0) {
                final Label l = new Label("No items on this list");
                l.getStyleClass().addAll("eStringFieldElementsListSmall", Editor.maxWidthStyle);
            } else {
                annoRetVal.forEach(el -> {
                    if (el.getClass().isAnnotationPresent(UIListItem.class)) {
                        final Method[] methods = el.getClass().getMethods();
                        if (methods == null) return;
                        if (methods.length == 0) return;

                        for (Method ml : methods) {
                            if (ml.isAnnotationPresent(UIListItemHeader.class)) {
                                if (ml.getReturnType().equals(String.class)) {
                                    String itemName = null;
                                    try {
                                        itemName = (String) ml.invoke(el);
                                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                        Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    
                                    sb.append((itemName == null) ? "Null item" : itemName).append("; ");
                                }
                            }
                        }
                    }
                });
                
                final String st = sb.toString().trim();
                final Label l = new Label((st.lastIndexOf(';') > 0) ? st.substring(0, st.lastIndexOf(';')) : st); 
                l.getStyleClass().addAll("eStringFieldElementLabelListSmall", Editor.maxWidthStyle);
                l.setWrapText(true);
                listContainer.getChildren().add(l);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ElementListLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setXLabelText(String text) {
        textLabel.setText(text);
    }

    public EditorFragmentList getParentFL() {
        return parentFL;
    }

    public void setParentFL(EditorFragmentList parentFL) {
        this.parentFL = parentFL;
    }
}
