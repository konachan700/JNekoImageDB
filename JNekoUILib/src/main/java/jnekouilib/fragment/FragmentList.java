package jnekouilib.fragment;

import java.util.ArrayList;
import java.util.Collection;

import jnekouilib.editor.Editor;
import jnekouilib.editor.EditorFormValidator;
import jnekouilib.panel.Panel;
import jnekouilib.panel.PanelButton;
import jnekouilib.panel.PanelInfobox;
import jnekouilib.utils.MessageBus;
import jnekouilib.utils.MessageBusActions;
import jnekouilib.utils.ReflectionUtils;
import jnekouilib.utils.UIUtils;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class FragmentList<T> extends Fragment implements FragmentListItemActionListener<T> {
    private Collection<T> 
            items;
    
    private final ArrayList<FragmentListItem<T>>
            uiItems = new ArrayList<>();
    
    private final ScrollPane
            elementsSP= new ScrollPane();
    
    private final VBox
            vContainer = new VBox();
    
    private T
            selectedObject = null;
    
    private FragmentListItem
            selectedItem = null;
    
    private final Editor
            editor = new Editor();
    
    private EditorFormValidator
            formValForEdit = null,
            formValForAdd = null;
    
    private final Panel
            panel = new Panel();
    
    private final PanelButton
            addButton;

    private final Class 
            collectionGenericType;
    
    private final Fragment
            parentFragment;
    
//    private boolean 
//            enmbeddedMode = false;
    
    private final PanelInfobox
            infoBox = new PanelInfobox("iconTest01");
    
    public void setCollection(Collection<T> c) {
        if (!ReflectionUtils.isCreatable(c.getClass()))
            panel.removeNode(addButton);
        
        items = c;
    }
    
    public FragmentList(Class collectionGenericType, String collectionRefName, Fragment parentFragment) {
        super();
        super.setHost(parentFragment.getHost()); 
        
        this.collectionGenericType = collectionGenericType;
//        enmbeddedMode = true;
        
        if (parentFragment == null) throw new Error("The parentFragment cannot be null!");
        this.parentFragment = parentFragment;

        this.getStyleClass().addAll("embListHeight", "maxWidth");

        elementsSP.getStyleClass().addAll("maxWidth", "maxHeight", "ScrollPane");
        elementsSP.setContent(vContainer);
        elementsSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        elementsSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        elementsSP.setFitToWidth(true);
        elementsSP.setFitToHeight(false);
        
        super.getChildren().addAll(panel, elementsSP);
        
        MessageBus.registerMessageReceiver(
                MessageBusActions.EditorFragmentListRefresh,
                (b, objects) -> {
            final String msg = UIUtils.getStringFromObject(0, objects);
            if (msg == null) return;
            if (msg.equalsIgnoreCase(collectionRefName)) create();
        });
        
        panel.addNodes(infoBox);

        addButton = new PanelButton("iconAddForList", "Add item to list...", "Add item", e -> {
            addItemForm();
        });
           
        panel.addNodes(
                addButton,
                new PanelButton("iconDeleteForList", "Delete item from list", "Remove", e -> {
                    deleteSelectedItem();
                }),
                new PanelButton("iconEditForList", "Edit selected item...", "Edit item", e -> {
                    editSelectedItem();
                })
        );
    }
    
    public FragmentList(Class collectionGenericType, String collectionRefName) {
        super();
        addButton = null;
        parentFragment = null;
        
        this.collectionGenericType = collectionGenericType;
        
        this.getStyleClass().addAll("maxHeight", "maxWidth");

        elementsSP.getStyleClass().addAll("maxWidth", "maxHeight", "ScrollPane");
        elementsSP.setContent(vContainer);
        elementsSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        elementsSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        elementsSP.setFitToWidth(true);
        elementsSP.setFitToHeight(false);
        
        super.getChildren().addAll(elementsSP);
        
        MessageBus.registerMessageReceiver( 
                MessageBusActions.EditorFragmentListRefresh, 
                (b, objects) -> {
            final String msg = UIUtils.getStringFromObject(0, objects);
            if (msg == null) return;
            if (msg.equalsIgnoreCase(collectionRefName)) create();
        });
        
        super.getPanel().clear();
        super.getPanel().addSeparator();
        
//        if (ReflectionUtils.isCreatable(items)) 
            super.getPanel().addNodes(
                    new PanelButton("iconAddForList", "Add item to list...", "Add item", e -> {
                        addItemForm();
                    })
            );
        
        super.getPanel().addNodes(
                new PanelButton("iconDeleteForList", "Delete item from list", "Remove", e -> {
                    deleteSelectedItem();
                }),
                new PanelButton("iconEditForList", "Edit selected item...", "Edit item", e -> {
                    editSelectedItem();
                })
        );
    }
    
    public final void create() {
        if (items == null) return;
        
        uiItems.clear();
        vContainer.getChildren().clear();
        
        items.forEach(item -> {
            final FragmentListItem fli = new FragmentListItem(item, this);
            fli.create();
            uiItems.add(fli);
            vContainer.getChildren().add(fli);
        });
    }
    
    private void goBack() {
        super.getHost().back();
        create();
    }
    
    public void addItemForm() {
        final T newObject = (T) ReflectionUtils.createNew(collectionGenericType);
        if (newObject == null) {
            System.err.println("newObject is null!");
            return;
        }
        
        editor.readObject(newObject);  
        
        editor.getPanel().clear();
        editor.getPanel().addSeparator();
        editor.getPanel().addNodes(
                new PanelButton("iconBackForList", "Exit without save", ex -> {
                        goBack();
                }),
                new PanelButton("iconSaveForList", "Save and exit", ex -> {
                    if (formValForAdd != null) 
                        editor.validateForm(formValForAdd);
                    editor.saveObject();
                    items.add(newObject);
                    MessageBus.sendMessage(MessageBusActions.HibernateAddNew, newObject);
                    goBack();
                })
        );
        
//        if (enmbeddedMode)
//            parentFragment.getHost().showFragment(editor, false);
//        else
            super.getHost().showFragment(editor, false);
    }

    
    public void deleteSelectedItem() {
        if (selectedObject == null) { 
            this.showMessage("Error", "No items selected");
            return;
        }
        
        this.showMessageYesNo("Remove", "Do you want to remove this item?", c -> {
            if (c == FragmentMessageResult.YES) {
                items.remove(selectedObject);
                MessageBus.sendMessage(MessageBusActions.HibernateDelete, selectedObject);

                selectedObject = null;
                selectedItem = null;

                create();
            }
        });
    }

    
    public void addCollectionHelper(String name, Collection items) {
        editor.addCollectionHelper(name, items);
    }
    
    public void editSelectedItem() {
        if (selectedObject == null) { 
            this.showMessage("Error", "No items selected");
            return;
        }
//        if (objectRequesterForNew == null) return;
        
        editor.readObject(selectedObject);
        
        editor.getPanel().clear();
        editor.getPanel().addSeparator();
        editor.getPanel().addNodes(
                new PanelButton("iconBackForList", "Exit without save", e -> {
                    goBack();
                }),
                new PanelButton("iconSaveForList", "Save and exit", e -> {
                    if (formValForEdit != null) 
                            editor.validateForm(formValForEdit);
                    editor.saveObject();
                    MessageBus.sendMessage(MessageBusActions.HibernateEdit, selectedObject);
                    goBack();
                })
        );

//        if (enmbeddedMode)
//            parentFragment.getHost().showFragment(editor, false);
//        else
            super.getHost().showFragment(editor, false);
    }
    
    @Override
    public void OnItemClick(T object, FragmentListItem fli, MouseEvent me) {
        uiItems.forEach(item -> {
            item.setSelected(false);
        });
        fli.setSelected(true);
        
        selectedObject = object;
        selectedItem = fli;
    }
    
    public T getSelectedObject() {
        return selectedObject;
    }
    
    public T getItem(int index) {
        return uiItems.get(index).getMyObject();
    }
    
    public FragmentListItem<T> getUIItem(int index) {
        return uiItems.get(index);
    }

    public EditorFormValidator getFormValForEdit() {
        return formValForEdit;
    }

    public void setFormValForEdit(EditorFormValidator formValForEdit) {
        this.formValForEdit = formValForEdit;
    }

    public EditorFormValidator getFormValForAdd() {
        return formValForAdd;
    }

    public void setFormValForAdd(EditorFormValidator formValForAdd) {
        this.formValForAdd = formValForAdd;
    }

    public Fragment getParentFragment() {
        return parentFragment;
    }
    
    public void setHeaderText(String title, String text) {
        infoBox.setText(text);
        infoBox.setTitle(title); 
    }
}
