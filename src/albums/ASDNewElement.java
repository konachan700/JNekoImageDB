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

public class ASDNewElement extends HBox{
    private final Button
            addBtn = new Button("", new ImageView(new Image(new File("./icons/plus32.png").toURI().toString())));

    private final TextField
            newItemName = new TextField();

    private final Label       
            newTitle = new Label("Добавить альбом");

    private final ASDNewElementActionListener
            elAL;

    private final long
            parent_id;

    public ASDNewElement(ASDNewElementActionListener al, long pid) {
        super(4);
        this.getStylesheets().add(getClass().getResource("app_style.css").toExternalForm());
        this.getStyleClass().add("ASDNewElement_HBox");

        elAL = al;
        parent_id = pid;

        GUITools.setFixedSize(newTitle, 196, 32);
        newTitle.setAlignment(Pos.CENTER_LEFT);
        newTitle.getStyleClass().add("ASDNewElement_newTitle");

        GUITools.setFixedSize(addBtn, 32, 32);
        addBtn.getStyleClass().add("ASDNewElement_addBtn");
        addBtn.setOnMouseClicked((MouseEvent event) -> {
            if (newItemName.getText().trim().length() > 0) elAL.OnNew(parent_id, newItemName.getText().trim());
        });

        GUITools.setMaxSize(newItemName, 9999, 32);
        newItemName.getStyleClass().add("ASDNewElement_newItemName");

        this.getChildren().addAll(newTitle, newItemName, addBtn);
    }
}
