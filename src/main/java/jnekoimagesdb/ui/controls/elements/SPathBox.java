package jnekoimagesdb.ui.controls.elements;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import jnekoimagesdb.ui.GUITools;

public class SPathBox extends TextField {
    private Path
            currValue = null;
    
    public SPathBox(int w, int h) {
        init(w, h, "textfield");
    }
    
    public SPathBox(int w, int h, String style) {
        init(w, h, style);
    }
    
    private void init(int w, int h, String styleName) {
        GUITools.setStyle(this, "GUIElements", styleName);
        if (w <= 0) GUITools.setMaxSize(this, 9999, h); GUITools.setFixedSize(this, w, h);
        this.setAlignment(Pos.CENTER_LEFT);
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            this.getStyleClass().remove("GUIElements_textfield_valid");
            this.getStyleClass().remove("GUIElements_textfield_error");
            final Path p = FileSystems.getDefault().getPath(this.getText());
            currValue = null;
            if (p != null) {
                if (Files.exists(p) && Files.isDirectory(p) && Files.isReadable(p) && Files.isWritable(p)) {
                    currValue = p.toAbsolutePath();
                    this.getStyleClass().add("GUIElements_textfield_valid");
                } else 
                    this.getStyleClass().add("GUIElements_textfield_error");
                
            } else 
                this.getStyleClass().add("GUIElements_textfield_error");
        });
    }
    
    public String getValueString() {
        return currValue.toString();
    }
    
    public Path getValue() {
        return currValue;
    }
    
    public void setValue(Path p) {
        this.setText(p.toAbsolutePath().toString());
    }
    
    public boolean isNull() {
        return (currValue == null);
    }
}
