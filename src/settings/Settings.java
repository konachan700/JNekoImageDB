package settings;

import dataaccess.SQLite;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class Settings extends VBox {
    private final SQLite SQL;
    private final SPathField uploadPath;
    private final SCheckBox showDeleted;
    
    public Settings(SQLite sql) {
        this.setMaxWidth(9999);
        this.setPrefWidth(9999);
        this.setAlignment(Pos.BOTTOM_CENTER);
        
        SQL = sql;
        
        showDeleted = new SCheckBox(SQL, "showDeleted");
        
        uploadPath = new SPathField(SQL, "uploadPath", SPathField.TYPE_DIR);
        uploadPath.setMaxWidth(9999);
        uploadPath.setPrefWidth(9999);
        
        
        
        this.getChildren().add(new SettingsElementContainer("Путь к папке для выгрузки картинок", uploadPath));
        this.getChildren().add(new SettingsElementContainer("Показывать группу удаленных", showDeleted));
    }
    
    
}
