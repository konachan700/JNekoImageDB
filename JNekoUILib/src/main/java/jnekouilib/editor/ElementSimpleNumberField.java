package jnekouilib.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import jnekouilib.anno.UILongField;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ElementSimpleNumberField extends VBox implements EditorTypeText, EditorTypeLabeled, EditorTypeNumber, EditorTypeValidable, EditorFillable {
    private boolean 
            valid = false, 
            readOnly = false;
    
    private volatile long 
            value = 0,
            max = Long.MAX_VALUE,
            min = Long.MIN_VALUE;
    
    private final TextField
            field = new TextField();
    
    private final Label
            title = new Label();
    
    public ElementSimpleNumberField() {
        field.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            if (readOnly) return;
            
            field.getStyleClass().remove(Editor.okStyle);
            field.getStyleClass().remove(Editor.errorStyle);
            try {
                value = Long.parseLong(newValue.trim(), 10);
                if ((value >= min) && (value <= max)) {
                    field.getStyleClass().add(Editor.okStyle);
                    valid = true;
                } else {
                    field.getStyleClass().add(Editor.errorStyle);
                    value = 0;
                    valid = false;
                }
            } catch (NumberFormatException e) { 
                valid = false;
                field.getStyleClass().add(Editor.errorStyle);
            }
        });
        
        super.getStyleClass().addAll("eStringFieldElementRoot", Editor.maxWidthStyle);
        title.getStyleClass().addAll("eStringFieldElementLabel", Editor.maxWidthStyle);
        field.getStyleClass().addAll("eStringFieldElementText", Editor.maxWidthStyle);
        
        super.getChildren().addAll(title, field);
    }
    
    @Override
    public void setXText(String text) {
        field.setText(text);
    }

    @Override
    public String getXText() {
        return field.getText();
    }

    @Override
    public boolean isXTextEmpty() {
        return (field.getText().trim().length() <= 0);
    }

    @Override
    public void setXLabelText(String text) {
        title.setText(text);
    }

    @Override
    public void setXTextHelp(String text) {
        field.setPromptText(text);
    }

    @Override
    public void setXNumberBorderValues(long min, long max) {
        this.max = max;
        this.min = min;
    }

    @Override
    public void setXNumber(long val) {
        field.setText(Long.toString(val));
        value = val;
    }

    @Override
    public long getXNumber() {
        return value;
    }

    @Override
    public boolean isXNumberValid() {
        return valid;
    }

    @Override
    public void setXTextReadOnly(boolean ro) {
        readOnly = !ro;
        field.setEditable(ro);
        
        field.getStyleClass().removeAll("eStringFieldElementText", "eStringFieldElementText_RO");
        field.getStyleClass().addAll((!ro) ? "eStringFieldElementText_RO" : "eStringFieldElementText");
    }

    @Override
    public void setXTextMaxChars(int max) {
    }
    
    @Override
    public void setValid(boolean v) {
        field.getStyleClass().remove(Editor.okStyle);
        field.getStyleClass().remove(Editor.errorStyle);
        field.getStyleClass().add((v) ? Editor.okStyle : Editor.errorStyle);
    }

    @Override
    public void fillFromObject(Object o, Method m) {
        final String annoLabel = m.getAnnotation(UILongField.class).labelText();
        if (annoLabel != null) 
            setXLabelText(annoLabel);
        
        final int annoRO = m.getAnnotation(UILongField.class).readOnly();
        setXTextReadOnly(annoRO == 0);
        
        setXNumberBorderValues(m.getAnnotation(UILongField.class).minVal(), m.getAnnotation(UILongField.class).maxVal());
        
        Long annoRetVal;
        try {
            annoRetVal = (Long) m.invoke(o);
            if (annoRetVal != null) setXNumber(annoRetVal);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
