package dialogs;

import java.io.File;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class DYesNo {
    public interface DYesNoActionListener {
        public void OnYes();
        public void OnNo();
    }
    
    private final Button 
            yesImg = new Button("", new ImageView(new Image(new File("./icons/d_yes.png").toURI().toString()))), 
            noImg  = new Button("", new ImageView(new Image(new File("./icons/d_no.png").toURI().toString())));
    
    private final TextArea MessageStr = new TextArea("");
                
    private final DYesNoActionListener AL;
    private final ArrayList<Node> firstNode = new ArrayList<>();
    
    public DYesNo(Pane parent, DYesNoActionListener al, String message) {
        super();
        AL = al;
        parent.getChildren().stream().forEach((n) -> {
            firstNode.add(n);
        });
        
        MessageStr.setText(message);
        MessageStr.setEditable(false);
        MessageStr.setWrapText(true);
        
        _s2(yesImg, 64, 64);
        _s2(noImg, 64, 64);
        _s1(MessageStr, 9999, 64);
        
        _z(yesImg, "DYesButton");
        _z(noImg, "DNoButton");
        _z(MessageStr, "DMessageStr");
        
        yesImg.setOnMouseClicked((MouseEvent event) -> {
            parent.getChildren().clear();
            AL.OnYes();
            firstNode.stream().forEach((n) -> {
                parent.getChildren().add(n);
            });
            event.consume();
        }); 
        
        noImg.setOnMouseClicked((MouseEvent event) -> {
            parent.getChildren().clear();
            AL.OnNo();
            firstNode.stream().forEach((n) -> {
                parent.getChildren().add(n);
            });
            event.consume();
        });

        parent.getChildren().clear();
        parent.getChildren().addAll(MessageStr, noImg, yesImg);
    }
    
    private void _z(Region n, String styleID) {
        n.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        n.getStyleClass().add(styleID);
    }

    private void _s2(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
        n.setMinSize(w, h);
    }
    
    private void _s1(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
    }
}
