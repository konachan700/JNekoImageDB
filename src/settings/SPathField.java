package settings;

import dataaccess.SQLite;
import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class SPathField extends TextField {
    private final String ID;
    private final SQLite SQL;
    private File FILE;
    private final int TYPE;
    
    public static final int 
            TYPE_DIR  = 1,
            TYPE_FILE = 2;
    
    public SPathField(SQLite sql, String id, int type) {
        super();
        ID = id;
        SQL = sql;
        TYPE = type;
        
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("STextField");
        
        this.setText(SQL.ReadAPPSettingsString("ff_"+ID));
        this.textProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue) -> {          
            FILE = new File(newValue);
            this.getStyleClass().removeAll("STextFieldYes", "STextFieldNo");
            
            if (TYPE == TYPE_DIR) {
                if (FILE.canRead() && FILE.isDirectory()) {
                    SQL.WriteAPPSettingsString("ff_"+ID, newValue);
                    this.getStyleClass().add("STextFieldYes");
                } else 
                    this.getStyleClass().add("STextFieldNo");
            } else if (TYPE == TYPE_FILE) {
                if (FILE.canRead() && FILE.isFile()) {
                    SQL.WriteAPPSettingsString("ff_"+ID, newValue);
                    this.getStyleClass().add("STextFieldYes");
                } else 
                    this.getStyleClass().add("STextFieldNo");
            }
            
            this.applyCss();
        });
    }
    
    public String getPath() {
        return FILE.getAbsolutePath();
    }
    
    public File getFile() {
        return FILE;
    }
}
