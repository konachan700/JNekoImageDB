package ui.inputbox;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NumericTextField extends TextField {
    private boolean valid = false;
    private volatile long 
            value = 0,
            max = Long.MAX_VALUE,
            min = Long.MIN_VALUE;
    
    private InputBoxesActionListener iAL = null;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public NumericTextField(String okStyle, String errorStyle) {
        super();
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            this.getStyleClass().remove(okStyle);
            this.getStyleClass().remove(errorStyle);
            try {
                value = Long.parseLong(newValue.trim(), 10);
                if ((value >= min) && (value <= max)) {
                    this.getStyleClass().add(okStyle);
                    valid = true;
                    if (iAL != null) iAL.OnNewAndValidData(this);
                } else {
                    this.getStyleClass().add(errorStyle);
                    value = 0;
                    valid = false;
                }
            } catch (NumberFormatException e) { 
                valid = false;
                this.getStyleClass().add(errorStyle);
            }
        });
    }
    
    public void setActionListener(InputBoxesActionListener al) {
        iAL = al;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public long getLong() {
        return value;
    }
    
    public int getInt() {
        return (int) value;
    }
    
    public void setMinMax(long minVal, long maxVal) {
        min = minVal;
        max = maxVal;
    }
}
