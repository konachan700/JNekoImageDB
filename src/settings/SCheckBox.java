package settings;

import dataaccess.DBWrapper;
import dataaccess.DBEngine;
import java.io.File;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class SCheckBox extends Button {
    private final Image
            sel = new Image(new File("./icons/selected16.png").toURI().toString()),
            unsel = new Image(new File("./icons/unselected16.png").toURI().toString());

    private boolean
            value = false;
    
    private final String 
            ID;
    
    public SCheckBox(String id) {
        ID = id;
        
        this.setMaxSize(16, 16);
        this.setMinSize(16, 16);
        this.setPrefSize(16, 16);
        
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("SCheckBox");
        
        this.setChecked(DBWrapper.ReadAPPSettingsString("bl_"+ID).contentEquals("YES"));
        this.setOnMouseClicked((MouseEvent event) -> {
            invertCheched();
            event.consume();
        }); 
    }
    
    public final void invertCheched() {
        value = !value;
        this.setGraphic(new ImageView((value) ? sel : unsel));
        DBWrapper.WriteAPPSettingsString("bl_"+ID, (value) ? "YES" : "NO");
    }
    
    public final void setChecked(boolean b) {
        value = b;
        this.setGraphic(new ImageView((value) ? sel : unsel));
        DBWrapper.WriteAPPSettingsString("bl_"+ID, (value) ? "YES" : "NO");
    }
    
    public boolean isChecked() {
        return value;
    }
}
