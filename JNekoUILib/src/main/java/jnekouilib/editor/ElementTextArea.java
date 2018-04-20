package jnekouilib.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import jnekouilib.anno.UITextArea;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ElementTextArea extends VBox implements EditorTypeText, EditorTypeLabeled, EditorTypeValidable, EditorFillable {
    private final TextArea
            field = new TextArea();
    
    private final Label
            title = new Label();
    
    public ElementTextArea() {
        super.getStyleClass().addAll("eStringFieldElementRoot", Editor.maxWidthStyle);
        title.getStyleClass().addAll("eStringFieldElementLabel", Editor.maxWidthStyle);
        field.getStyleClass().addAll("eStringFieldElementTextArea", Editor.maxWidthStyle, "StringFieldElementTextArea_height");
        
        field.setWrapText(true);
        
        super.getChildren().addAll(title, field);
    }
    
    public void setTextDisabled(boolean d)    {
        field.setEditable(d);
        field.setFocusTraversable(d);
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
    public void setXTextReadOnly(boolean ro) {
        field.setEditable(ro);
        
    }

    @Override
    public void setXTextMaxChars(int max) {
        // TODO: add code for validate form
    }

    @Override
    public void setValid(boolean v) {
        field.getStyleClass().remove(Editor.okStyle);
        field.getStyleClass().remove(Editor.errorStyle);
        field.getStyleClass().add((v) ? Editor.okStyle : Editor.errorStyle);
    }

    @Override
    public void fillFromObject(Object o, Method m) {
        final String annoLabel = m.getAnnotation(UITextArea.class).labelText();
        if (annoLabel != null) 
            setXLabelText(annoLabel);

        final String annoHelp = m.getAnnotation(UITextArea.class).helpText();
        if (annoHelp != null) 
            setXTextHelp(annoHelp); 

        final int annoMaxChars = m.getAnnotation(UITextArea.class).maxChars();
        setXTextMaxChars(annoMaxChars); 

        final int annoRO = m.getAnnotation(UITextArea.class).readOnly();
        setXTextReadOnly(annoRO == 0);

        String annoRetVal;
        try {
            annoRetVal = (String) m.invoke(o);
            if (annoRetVal != null) setXText(annoRetVal);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
