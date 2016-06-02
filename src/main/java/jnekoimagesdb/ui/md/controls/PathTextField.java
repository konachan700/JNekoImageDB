package jnekoimagesdb.ui.md.controls;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class PathTextField extends TextField {
    private boolean valid = false;
    private Path value = null;
    
    public PathTextField(String okStyle, String errorStyle) {
        super();
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            this.getStyleClass().remove(okStyle);
            this.getStyleClass().remove(errorStyle);
            
            final Path p = FileSystems.getDefault().getPath(newValue).toAbsolutePath();
            if (Files.exists(p) && Files.isReadable(p)) { 
                this.getStyleClass().add(okStyle);
                valid = true;
                value = p;
            } else {
                this.getStyleClass().add(errorStyle);
                value = null;
                valid = false;
            }
        });
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public Path getPath() {
        return value;
    }
    
    public String getPathString() {
        return value.toFile().getAbsolutePath();
    }
}
