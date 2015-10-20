package settings;

import dataaccess.DBWrapper;
import dataaccess.DBEngine;
import dataaccess.Lang;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class STextField extends TextField {
    private final String ID;
    
    public STextField(String id) {
        super();
        ID = id;
        
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("STextField");
        
        this.setText(DBWrapper.ReadAPPSettingsString("tf_"+ID));
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            DBWrapper.WriteAPPSettingsString("tf_"+ID, newValue);
        });
    }
}
