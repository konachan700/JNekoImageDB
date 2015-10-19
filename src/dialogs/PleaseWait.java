package dialogs;

import dataaccess.Lang;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PleaseWait extends VBox {
    private final TextArea
            taLOG = new TextArea();
    
    private final Label
            currentSystemLoad = new Label();
    
    private final Pane
            parentPane;
    
    private final ArrayList<Node> 
            firstNode = new ArrayList<>();
    
    private StringBuilder 
            textStr = null,
            logText = null;
    
    private boolean 
            isActive = false;
    
    public PleaseWait(Pane parent, StringBuilder topText, StringBuilder logTextX) {
        textStr = topText;
        logText = logTextX;
        parentPane = parent;
        
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        this.getStyleClass().add("PleaseWait_RootPane");
        this.setMaxSize(9999, 9999);
        this.setPrefSize(9999, 9999);
        this.setAlignment(Pos.TOP_LEFT); 
        
        final Label pleaseWaitLabel = new Label(Lang.PleaseWait_PW);
        pleaseWaitLabel.getStyleClass().add("PleaseWait_pleaseWaitLabel");
        final DropShadow ds = new DropShadow();
        ds.setOffsetY(0f);
        ds.setRadius(7f);
        ds.setSpread(0.8f);
        ds.setColor(Color.color(0.99f, 0.99f, 0.99f));
        pleaseWaitLabel.setEffect(ds);
        pleaseWaitLabel.setMaxSize(9999, 21);
        pleaseWaitLabel.setPrefSize(9999, 21);

        taLOG.setMaxSize(9999, 9999);
        taLOG.setPrefSize(9999, 9999);
        taLOG.setWrapText(true);
        taLOG.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        taLOG.getStyleClass().add("PleaseWait_taLOG");
        
        currentSystemLoad.setEffect(ds);
        currentSystemLoad.getStyleClass().add("PleaseWait_currentSystemLoad");
        
        this.getChildren().add(pleaseWaitLabel);
        this.getChildren().add(currentSystemLoad);
        this.getChildren().add(taLOG);  
    }
    
    public void setVis(boolean b) {
        if (b == isActive) return;
        isActive = b;
        Platform.runLater(() -> { 
            if (isActive) {
                firstNode.clear();
                parentPane.getChildren().stream().forEach((n) -> {
                    firstNode.add(n);
                });
                parentPane.getChildren().clear();
                parentPane.getChildren().add(this);
            } else {
                parentPane.getChildren().clear();
                firstNode.stream().forEach((n) -> {
                    parentPane.getChildren().add(n);
                });
            }
        });
    }
    
    public void Update() {
        if (!isActive) return;
        Platform.runLater(() -> { 
            currentSystemLoad.setText(textStr.substring(0));
            taLOG.setText(logText.substring(0));
            if (taLOG.getText().length() > (128 * 1024)) {
                taLOG.clear();
                logText.delete(0, logText.length());
            }
            taLOG.setScrollTop(Double.MIN_VALUE);
        });
    }
}
