package jnekoimagesdb.ui.controls.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import jnekoimagesdb.ui.GUITools;

public class EThreadStatItem extends HBox {
        public static final int ELEMENT_HEIGHT = 32; 
        
        public volatile int ID = 0;
        
        private final ImageView
                icon = new ImageView(GUITools.loadIcon("unknown-32"));

        private final Label
                tIDLabel = new Label(), 
                countLabel = new Label();
        
        @SuppressWarnings("LeakingThisInConstructor")
        public EThreadStatItem(int tid) {
            super(0);
            
            GUITools.setStyle(this, "ProgressElement", "root_pane");
            this.setAlignment(Pos.CENTER);
            
            icon.setPreserveRatio(true);
            icon.setSmooth(true);
            
            GUITools.setStyle(tIDLabel, "ProgressElement", "tIDLabel");
            GUITools.setFixedSize(tIDLabel, 100, ELEMENT_HEIGHT);
            tIDLabel.setAlignment(Pos.CENTER_RIGHT);
            
            GUITools.setStyle(countLabel, "ProgressElement", "label");
            GUITools.setMaxSize(countLabel, 9999, ELEMENT_HEIGHT);
            countLabel.setAlignment(Pos.CENTER_LEFT);

            this.getChildren().addAll(icon, countLabel, tIDLabel);
            
            final String threadID = Integer.toHexString(tid).toUpperCase() + "00000000";            
            tIDLabel.setText(threadID.substring(0, 8));
        }
        
        public void setInProgress(int counter) {
            icon.setImage(GUITools.loadIcon("inprogress-32"));
            countLabel.setText("#"+counter);
        }
        
        public void setCompleted() {
            icon.setImage(GUITools.loadIcon("selected-32"));
            countLabel.setText("OK");
        }
}
