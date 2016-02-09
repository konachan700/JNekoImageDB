package jnekoimagesdb.ui.controls.elements;

import jnekoimagesdb.domain.DSTag;

import java.util.ArrayList;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import jnekoimagesdb.ui.GUITools;

public class STagAddInputField extends FlowPane {
    private boolean 
            searchMode = false;
    
    private final ArrayList<DSTag> 
            currentTags = new ArrayList<>(),
            currentNotTags = new ArrayList<>();

    private final STextField
            mainInput;
    
    private final ETagListItemActionListener
            actListener = new ETagListItemActionListener() {
                @Override
                public void onClick(DSTag tag) {
                }

                @Override
                public void onAddToListBtnClick(DSTag tag, boolean isSetMinus) {
                }

                @Override
                public void onEditComplete(DSTag tag) {
                }

                @Override
                public void onDelete(DSTag tag) {
                    currentTags.remove(tag);
                    currentNotTags.remove(tag);
                    regenerateAll();
                }
    };
    
    @SuppressWarnings("LeakingThisInConstructor")
    public STagAddInputField(boolean _searchMode) {
        super();
        searchMode = _searchMode;
        
        this.setVgap(4);
        this.setHgap(4);
        this.setMinHeight(USE_PREF_SIZE);
        this.setMaxWidth(9999);
        this.setPrefWidth(9999);
        this.setMinWidth(300);
        this.setFocusTraversable(true);
        GUITools.setStyle(this, "STagAddInputField", "root_pane");
        
        mainInput = new STextField(0, ETagListItem.ITEM_SIZE, null, "textfield_new_tag");
        mainInput.setPromptText("Enter your tag...");
        mainInput.setMinWidth(Region.USE_PREF_SIZE);
        mainInput.setPrefWidth(Region.USE_COMPUTED_SIZE);
        mainInput.setMaxWidth(Double.MAX_VALUE);
        mainInput.setOnKeyPressed((KeyEvent key) -> {
            if (key.getCode() == KeyCode.ENTER) {
                String stag = mainInput.getText().trim();
                if (!searchMode) {
                    final Pattern pattern = Pattern.compile("^([\\-]+)");
                    stag = pattern.matcher(stag).replaceAll("");
                }
                if (stag.isEmpty()) return;
                
                final DSTag t = new DSTag(stag);
                if (currentNotTags.contains(t) || currentTags.contains(t)) return;
                if (stag.startsWith("-")) {
                    currentNotTags.add(t);
                } else {
                    currentTags.add(t);
                }
                
                mainInput.setText("");
                regenerateAll();
            }
        }); 
        mainInput.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            mainInput.setPrefWidth((newValue.length() * 8) + 150); // it's magic!
        });
        
        this.getChildren().add(mainInput);
    }
    
    private void regenerateAll() {
        this.getChildren().clear();
        currentTags.forEach(c -> {
            this.getChildren().add(new ETagListItem(c, true, false, actListener));
        });
        currentNotTags.forEach(c -> {
            this.getChildren().add(new ETagListItem(c, true, true, actListener));
        });
        this.getChildren().add(mainInput);
    }
    
    public void clear() {
        currentTags.clear();
        currentNotTags.clear();
        regenerateAll();
    }
    
    public boolean isEmpty() {
        return (currentTags.isEmpty() && currentNotTags.isEmpty());
    }
    
    public ArrayList<DSTag> getTags() {
        return currentTags;
    }
    
    public ArrayList<DSTag> getNotTags() {
        return currentNotTags;
    }
}
