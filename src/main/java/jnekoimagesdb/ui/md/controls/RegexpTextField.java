package jnekoimagesdb.ui.md.controls;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class RegexpTextField extends TextField {
    private boolean valid = false;
    private String value = null;
    
    public RegexpTextField(String okStyle, String errorStyle, String regexp) {
        super();
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            this.getStyleClass().remove(okStyle);
            this.getStyleClass().remove(errorStyle);
            
            if (newValue.matches(regexp)) { 
                this.getStyleClass().add(okStyle);
                valid = true;
                value = newValue;
            } else {
                this.getStyleClass().add(errorStyle);
                value = "";
                valid = false;
            }
        });
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getValue() {
        return value;
    }
}
