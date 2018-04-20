package jnekouilib.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import jnekouilib.anno.UIBooleanField;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class ElementCheckBox extends VBox implements EditorTypeLabeled, EditorFillable {
    private final CheckBox
            check = new CheckBox();
    
    public ElementCheckBox() {
        super.getStyleClass().addAll("eStringFieldElementRoot", Editor.maxWidthStyle);
        check.getStyleClass().addAll("checkBox", Editor.maxWidthStyle);
        
        super.getChildren().addAll(check);
    }

    @Override
    public void setXLabelText(String text) {
        check.setText(text);
    }

    public void setReadOnly(boolean ro) {
        check.setDisable(!ro);
    }
    
    public Boolean getValue() {
        return check.isSelected();
    }
    
    public void setSelected(Boolean c) {
        check.setSelected(c);
    }

    @Override
    public void fillFromObject(Object o, Method m) {
        final String annoLabel = m.getAnnotation(UIBooleanField.class).labelText();
        if (annoLabel != null) 
            setXLabelText(annoLabel);

        final int annoRO = m.getAnnotation(UIBooleanField.class).readOnly();
        setReadOnly(annoRO == 0);

        Boolean annoRetVal;
        try {
            annoRetVal = (Boolean) m.invoke(o);
            if (annoRetVal != null) check.setSelected(annoRetVal);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
