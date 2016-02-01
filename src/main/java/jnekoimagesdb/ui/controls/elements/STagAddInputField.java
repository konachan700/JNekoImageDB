package jnekoimagesdb.ui.controls.elements;

import jnekoimagesdb.domain.DSTag;

import java.util.ArrayList;
import java.util.Set;

public class STagAddInputField extends SFHBox {
    public static final int
            ELEMENT_HEIGHT = 32;
    
    public static interface STagAddInputFieldActionListener {
        public void onAdd(Set<DSTag> list);
    }
    
    
    
    private final STagAddInputFieldActionListener
            actListener;
    
    private final ArrayList<DSTag> 
            currentTags = new ArrayList<>(),
            currentNotTags = new ArrayList<>();
    
    private final STextField
            mainInput;
    
    public STagAddInputField(int sz, int xMin, int xMax, int yMin, int yMax, STagAddInputFieldActionListener al) {
        super(sz, xMin, xMax, yMin, yMax);
        actListener = al;
        mainInput = new STextField(1, -1, ELEMENT_HEIGHT, (a, b) -> {
            
        });
        
        
        
        
    }
    
    public void allowUnknown(boolean b) {
        
    }
}
