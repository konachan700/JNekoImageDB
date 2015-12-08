package dialogs;

import imgfsgui.ToolsPanelTop;
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
            IMG64_SELECT_NO     = GUITools.loadIcon("delete-48"), 
            IMG32_IN_PROGRESS   = GUITools.loadIcon("inprogress-32"),  
            IMG32_IN_UNKNOWN    = GUITools.loadIcon("unknown-32"), 
            IMG32_COMPLETED     = GUITools.loadIcon("selected-32"); 
    
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
            
            GUITools.setStyle(this, "ProgressElement", "root_pane");
            this.setAlignment(Pos.CENTER);
            
            icon.setPreserveRatio(true);
            icon.setSmooth(true);
            
            GUITools.setStyle(tIDLabel, "ProgressElement", "tIDLabel");
            GUITools.setFixedSize(tIDLabel, 130, ELEMENT_HEIGHT);
            tIDLabel.setAlignment(Pos.CENTER_RIGHT);
            
            GUITools.setStyle(middle, "ProgressElement", "middle");
            GUITools.setMaxSize(middle, 9999, ELEMENT_HEIGHT);
            middle.setAlignment(Pos.CENTER_LEFT);
            
            GUITools.setStyle(countLabel, "ProgressElement", "label");
            GUITools.setMaxSize(countLabel, 9999, ELEMENT_HEIGHT / 2);
            countLabel.setAlignment(Pos.CENTER_LEFT);
            
            GUITools.setStyle(pathLabel, "ProgressElement", "label");
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
        
        GUITools.setStyle(cVBox, "DialogMTPrevGenProgress", "cVBox");
        GUITools.setMaxSize(cVBox, 9999, 9999);

        GUITools.setStyle(sPane, "DialogMTPrevGenProgress", "root_pane");
        sPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sPane.setFitToHeight(true);
        sPane.setFitToWidth(true);
        GUITools.setMaxSize(sPane, 9999, 9999);
        sPane.setContent(cVBox);
        
        this.getMainContainer().getChildren().add(sPane);
    }
}
