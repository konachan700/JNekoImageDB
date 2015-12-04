package dialogs;

import dataaccess.Lang;
import imgfsgui.ToolsPanelTop;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

public class DialogMTPrevGenProgress extends DialogWindow {
    private static final Image 
            IMG64_SELECT_NO     = new Image(new File("./icons/d_no.png").toURI().toString()),
            IMG32_IN_PROGRESS   = new Image(new File("./icons/inprogress.png").toURI().toString()),
            IMG32_IN_UNKNOWN    = new Image(new File("./icons/unknown32.png").toURI().toString()),
            IMG32_COMPLETED     = new Image(new File("./icons/selected.png").toURI().toString());
    
    public static class ProgressElement extends HBox {
        public static final int ELEMENT_HEIGHT = 32; 
        
        public volatile int ID = 0;
        
        private final ImageView
                icon = new ImageView(IMG32_IN_UNKNOWN);
        
        private final VBox
                middle = new VBox(0);
        
        private final Label
                tIDLabel = new Label(), 
                countLabel = new Label(),
                pathLabel = new Label();
        
        public ProgressElement() {
            super(0);
            
            this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
            this.getStyleClass().add("ProgressElement_root_pane");
            this.setAlignment(Pos.CENTER);
            
            icon.setPreserveRatio(true);
            icon.setSmooth(true);
            
            tIDLabel.getStyleClass().add("ProgressElement_tIDLabel");
            GUITools.setFixedSize(tIDLabel, 130, ELEMENT_HEIGHT);
            tIDLabel.setMinSize(130, ELEMENT_HEIGHT);
            tIDLabel.setPrefSize(130, ELEMENT_HEIGHT);
            tIDLabel.setMaxSize(130, ELEMENT_HEIGHT);
            tIDLabel.setAlignment(Pos.CENTER_RIGHT);
            
            middle.getStyleClass().add("ProgressElement_middle");
            GUITools.setMaxSize(middle, 9999, ELEMENT_HEIGHT);
            middle.setAlignment(Pos.CENTER_LEFT);
            
            countLabel.getStyleClass().add("ProgressElement_label");
            GUITools.setMaxSize(countLabel, 9999, ELEMENT_HEIGHT / 2);
            countLabel.setAlignment(Pos.CENTER_LEFT);
            
            pathLabel.getStyleClass().add("ProgressElement_label");
            GUITools.setMaxSize(pathLabel, 9999, ELEMENT_HEIGHT / 2);
            pathLabel.setAlignment(Pos.CENTER_LEFT);
            
            middle.getChildren().addAll(countLabel, pathLabel);
            this.getChildren().addAll(icon, middle, tIDLabel);
        }
        
        public void setInfo(Path p, int tid, int count, String quene) {
            final String threadID = Integer.toHexString(tid).toUpperCase() + "00000000";
            final String fname = (p != null) ? p.toString() : "";
            final String fn = ((fname.length() > 40) ? (fname.substring(0, 20) + "..." + fname.substring(fname.length()-17 , fname.length())) : fname);
            
            tIDLabel.setText(threadID.substring(0, 8));
            countLabel.setText("Очередь: "+quene+", элементов осталось: "+count);
            pathLabel.setText(fn);
        }
        
        public void setInProgress(int tid) {
            icon.setImage(IMG32_IN_PROGRESS);
            final String threadID = Integer.toHexString(tid).toUpperCase() + "00000000";
            tIDLabel.setText(threadID.substring(0, 8));
        }
        
        public void setCompleted() {
            icon.setImage(IMG32_COMPLETED);
        }
    }

    public final static int
            SELECT_NO = 2;
    
    private final ToolsPanelTop
        panel = new ToolsPanelTop((index) -> {
            switch (index) {
                case SELECT_NO:
                    // todo: threads stop & clear 
                    this.hide();
                    break;
            }
        });
    
    private final ScrollPane
            sPane = new ScrollPane();
    
    private final VBox
            cVBox = new VBox(4);
    
    private final Map<Integer, ProgressElement>
            elements = new HashMap<>();
    
    public void itemCreate(Integer tID) {
        elements.put(tID, new ProgressElement());
        cVBox.getChildren().add(elements.get(tID));
    }
    
    public void itemComplete(Integer tID) {
        elements.get(tID).setCompleted();
    }
    
    public void itemProgresss(Integer tID) {
        elements.get(tID).setInProgress(tID);
    }
    
    public void itemSetInfo(Integer tID, Path p, int count, String quene) {
        elements.get(tID).setInfo(p, tID, count, quene); 
    }
    
    public DialogMTPrevGenProgress() {
        super(600, 600, false);
        elements.clear();
        
        panel.addSeparator();
        panel.addButton(IMG64_SELECT_NO, SELECT_NO);
        this.getToolbox().getChildren().add(panel);
        
        cVBox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        cVBox.getStyleClass().add("DialogMTPrevGenProgress_cVBox");
        GUITools.setMaxSize(cVBox, 9999, 9999);

        sPane.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        sPane.getStyleClass().add("DialogMTPrevGenProgress_root_pane");
        sPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sPane.setFitToHeight(true);
        sPane.setFitToWidth(true);
        GUITools.setMaxSize(sPane, 9999, 9999);
        sPane.setContent(cVBox);
        
        this.getMainContainer().getChildren().add(sPane);
    }
}
