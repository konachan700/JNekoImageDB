package a_old;

import dataaccess.Lang;
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
import jnekoimagesdb.GUITools;

@Deprecated
public class DYesNo {
    public interface DYesNoActionListener {
        public void OnYes();
        public void OnNo();
    }
    
    private final Button 
            yesImg = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/d_yes.png").toURI().toString()))), 
            noImg  = new Button(Lang.NullString, new ImageView(new Image(new File("./icons/d_no.png").toURI().toString())));
    
    private final TextArea MessageStr = new TextArea(Lang.NullString);
                
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
        
        GUITools.setFixedSize(yesImg, 64, 64);
        GUITools.setFixedSize(noImg, 64, 64);
        GUITools.setMaxSize(MessageStr, 9999, 64);
        
        setStyle(yesImg, "DYesNo_YesButton");
        setStyle(noImg, "DYesNo_NoButton");
        setStyle(MessageStr, "DYesNo_MessageStr");
        
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
    
    private void setStyle(Region n, String styleID) {
        n.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        n.getStyleClass().add(styleID);
    }
}
