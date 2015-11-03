package albums;

import dataaccess.ImageEngine;
import dataaccess.Lang;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import jnekoimagesdb.GUITools;

public class AlbumsListElement extends HBox {
    private final Image
            sel = new Image(new File("./icons/selected16.png").toURI().toString()),
            unsel = new Image(new File("./icons/unselected16.png").toURI().toString());

    private boolean     
            selectState = false;
    
    public  final Long  
            ID, 
            parent;

    private final TextField       
            title = new TextField();

    private final Label       
            titleLabel = new Label();

    private final ASDElementActionListener
            elementAL;

    private boolean 
            editMode = false,
            checkVisible = true;

    private final ImageView
            next_i = new ImageView(new Image(new File("./icons/alb16.png").toURI().toString())),
            save_i = new ImageView(new Image(new File("./icons/save16.png").toURI().toString())),
            edit_i = new ImageView(new Image(new File("./icons/edit16.png").toURI().toString()));

    private final Button
            checkBtn = new Button(),
            saveBtn = new Button(Lang.NullString, edit_i);

    public AlbumsListElement(Long id, Long pid, String xtitle, ASDElementActionListener al) {
        super();
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("AlbumsListElement_HBox");

        ID          = id;
        elementAL   = al;
        parent      = pid;

        title.setText(xtitle);
        titleLabel.setText(xtitle);

        _init();
    }

    public void DisableCheck() {
        checkVisible = false;
        this.getChildren().clear();
        if (_isNotSystem(ID))
            this.getChildren().addAll((checkVisible) ? checkBtn : next_i, titleLabel, saveBtn);
        else
            this.getChildren().addAll((checkVisible) ? checkBtn : next_i, titleLabel);
    }

    private void _editModeOff() {
        saveBtn.setGraphic(edit_i);
        editMode = false;
        this.getChildren().clear();
        this.getChildren().addAll((checkVisible) ? checkBtn : next_i, titleLabel, saveBtn);
    }

    private void _editModeOn() {
        saveBtn.setGraphic(save_i);
        editMode = true;
        this.getChildren().clear();
        this.getChildren().addAll((checkVisible) ? checkBtn : next_i, title, saveBtn);
    }

    private void _init() {
        GUITools.setFixedSize(saveBtn, 16, 16);
        saveBtn.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        saveBtn.getStyleClass().add("AlbumsListElement_saveBtn");
        saveBtn.setOnMouseClicked((MouseEvent event) -> {
            if (editMode) {
                if (title.getText().trim().length() > 0) {
                    elementAL.OnSave(ID, this, title.getText().trim());
                    titleLabel.setText(title.getText().trim());
                }
                _editModeOff();
            } else {
                _editModeOn();
            }
        });

        GUITools.setFixedSize(checkBtn, 16, 16);
        checkBtn.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        checkBtn.getStyleClass().add("AlbumsListElement_checkBtn");
        checkBtn.setOnMouseClicked((MouseEvent event) -> {
            selectState = !selectState;
            if (_isNotSystem(ID))
                checkBtn.setGraphic(new ImageView((selectState) ? sel : unsel));
            
            if (selectState)  
                if (_isNotSystem(ID)) elementAL.OnCheck(ID, this); 
            else 
                if (_isNotSystem(ID)) elementAL.OnUncheck(ID, this); 

            event.consume();
        }); 

        GUITools.setMaxSize(titleLabel, 9999, 16);
        titleLabel.setAlignment(Pos.CENTER_LEFT);
        titleLabel.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        
        if (_isNotSystem(ID))
            titleLabel.getStyleClass().add("AlbumsListElement_titleLabel");
        else 
            titleLabel.getStyleClass().add("AlbumsListElement_titleLabel_system");
        
        titleLabel.setOnMouseClicked((MouseEvent event) -> {
            elementAL.OnItemClick(ID, this);
        });

        GUITools.setMaxSize(title, 9999, 16);
        title.setAlignment(Pos.CENTER_LEFT);
        title.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        title.getStyleClass().add("AlbumsListElement_title");

        if (_isNotSystem(ID)) {
            this.getChildren().addAll((checkVisible) ? checkBtn : next_i, titleLabel, saveBtn);
        } else {
            this.getChildren().addAll((checkVisible) ? checkBtn : next_i, titleLabel);
        }
    }
    
    private boolean _isNotSystem(long id) {
        return !((ID == 0) || (ID == -1L) || (ID == ImageEngine.ALBUM_ID_DELETED) || (ID == ImageEngine.ALBUM_ID_FAVORITES));
    }
}