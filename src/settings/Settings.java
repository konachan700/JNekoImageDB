package settings;

import dataaccess.DBEngine;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class Settings extends VBox {
    private final DBEngine SQL;
    private final SPathField uploadPath;
    private final SCheckBox 
            showDeleted,
            showNonSquaredPreview;
    
    public Settings(DBEngine sql) {
        this.setMaxWidth(9999);
        this.setPrefWidth(9999);
        this.setAlignment(Pos.BOTTOM_CENTER);
        
        SQL = sql;
        
        showDeleted = new SCheckBox("showDeleted");
        showNonSquaredPreview = new SCheckBox("showNSPreview");
        
        uploadPath = new SPathField("uploadPath", SPathField.TYPE_DIR);
        uploadPath.setMaxWidth(9999);
        uploadPath.setPrefWidth(9999);
                
        this.getChildren().add(new SettingsElementContainer("Путь к папке для выгрузки картинок", uploadPath));
        this.getChildren().add(new SettingsElementContainer("Показывать группу удаленных", showDeleted));
        this.getChildren().add(new SettingsElementContainer("Показывать полные миниатюры", showNonSquaredPreview));
    }
    
    
}
