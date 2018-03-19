package ui.dialog;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class BaseDialogWindow extends Stage {
    private final VBox windowContainer = new VBox();
    private final VBox childContainer = new VBox();
    private final Label windowTitle = new Label();

    public BaseDialogWindow(String title, int xSize, int ySize, boolean modal) {
        super();
        final Scene scene = new Scene(windowContainer, xSize, ySize);
        this.initStyle(StageStyle.UNDECORATED);

        windowContainer.getStylesheets().add(getClass().getResource("/style/css/main.css").toExternalForm());
        windowContainer.getStyleClass().addAll("null_pane", "max_width", "max_height");
        childContainer.getStyleClass().addAll("dialog_pane", "max_width", "max_height");
        windowTitle.getStyleClass().addAll("dialog_title", "max_width");
        windowTitle.setText(title);

        windowContainer.getChildren().addAll(windowTitle, childContainer);
        
        if (modal) this.initModality(Modality.APPLICATION_MODAL);
        this.getIcons().add(new Image("/style/icons/icon32.png"));
        this.getIcons().add(new Image("/style/icons/icon64.png"));
        this.getIcons().add(new Image("/style/icons/icon128.png"));
        this.setMinWidth(xSize);
        this.setMinHeight(ySize);
        this.setTitle(title);
        this.setScene(scene);
    }
    
    public void setContent(Node content) {
        childContainer.getChildren().clear();
        childContainer.getChildren().add(content);
    }
    
    public void setContent(Node ... content) {
        childContainer.getChildren().clear();
        childContainer.getChildren().addAll(content);
    }

    public void setCaption(String title) {
        windowTitle.setText(title);
    }
}
