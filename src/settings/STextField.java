package settings;

import dataaccess.SQLite;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class STextField extends TextField {
    private final String ID;
    private final SQLite SQL;
    
    public STextField(SQLite sql, String id) {
        super();
        ID = id;
        SQL = sql;
        
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("STextField");
        
        this.setText(SQL.ReadAPPSettingsString("tf_"+ID));
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {
            SQL.WriteAPPSettingsString("tf_"+ID, newValue);
        });
    }
}
