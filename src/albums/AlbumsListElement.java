package albums;

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

        private boolean     selectState     = false;
        public  final Long  ID, parent;
        
        private final TextField       
                title = new TextField();
        
        private final Label       
                titleLabel = new Label();
        
        private final ASDElementActionListener
                elementAL;
        
        private boolean editMode = false;
        
        private final ImageView
                save_i = new ImageView(new Image(new File("./icons/save16.png").toURI().toString())),
                edit_i = new ImageView(new Image(new File("./icons/edit16.png").toURI().toString()));
        
        private final Button
                checkBtn = new Button(),
                saveBtn = new Button("", edit_i);
        
        public AlbumsListElement(Long id, Long pid, String xtitle, ASDElementActionListener al) {
            super();
            this.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            this.getStyleClass().add("ASDElementHBox");
            
            ID          = id;
            elementAL   = al;
            parent      = pid;
            
            title.setText(xtitle);
            titleLabel.setText(xtitle);
            
            _init();
        }
        
        private void _editModeOff() {
            saveBtn.setGraphic(edit_i);
            editMode = false;
            this.getChildren().clear();
            this.getChildren().addAll(checkBtn, titleLabel, saveBtn);
        }
        
        private void _editModeOn() {
            saveBtn.setGraphic(save_i);
            editMode = true;
            this.getChildren().clear();
            this.getChildren().addAll(checkBtn, title, saveBtn);
        }
        
        private void _init() {
            GUITools.setFixedSize(saveBtn, 16, 16);
            saveBtn.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            saveBtn.getStyleClass().add("SSaveBox");
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
            checkBtn.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            checkBtn.getStyleClass().add("SCheckBox");
            checkBtn.setOnMouseClicked((MouseEvent event) -> {
                selectState = !selectState;
                if (ID > 0) {
                    checkBtn.setGraphic(new ImageView((selectState) ? sel : unsel));
                }
                
                if (selectState)  
                    if (ID > 0) elementAL.OnCheck(ID, this); 
                else 
                    if (ID > 0) elementAL.OnUncheck(ID, this); 
                
                event.consume();
            }); 
            
            GUITools.setMaxSize(titleLabel, 9999, 16);
            titleLabel.setAlignment(Pos.CENTER_LEFT);
            titleLabel.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            titleLabel.getStyleClass().add("SEC_Label");
            titleLabel.setOnMouseClicked((MouseEvent event) -> {
                elementAL.OnItemClick(ID, this);
            });
            
            GUITools.setMaxSize(title, 9999, 16);
            title.setAlignment(Pos.CENTER_LEFT);
            title.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            title.getStyleClass().add("SEC_Label_editable");
            
            if (ID > 0) {
                this.getChildren().addAll(checkBtn, titleLabel, saveBtn);
            } else {
                //title.setEditable(false);
                this.getChildren().addAll(checkBtn, titleLabel);
            }
        }
    }