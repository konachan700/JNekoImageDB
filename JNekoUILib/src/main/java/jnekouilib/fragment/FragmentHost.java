package jnekouilib.fragment;

import java.util.Stack;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class FragmentHost extends VBox {
    private final Stack<Fragment>
            fragments = new Stack<>();
    
    private Fragment
            currentFragment = null;
    
    private Pane
            panelHost = null;
    
    public FragmentHost() {
        super();
        super.getStylesheets().add("/styles/window.css");
        super.getStyleClass().addAll("maxHeight", "maxWidth");
    }
    
    public void setPanelHost(Pane fh) {
        panelHost = fh;
    }
    
    public void showFragment(Fragment f, boolean clearAll) {
        if (clearAll)
            fragments.clear();
        
        if (currentFragment != null)
            fragments.push(currentFragment);
        
        currentFragment = f;
        currentFragment.setHost(this);
        
        this.getChildren().clear();
        this.getChildren().add(currentFragment);
        
        if (panelHost != null) {
            panelHost.getChildren().clear();
            panelHost.getChildren().add(f.getPanel()); 
        }
    }
    
    public void back() {
        if (fragments.isEmpty())
            return;
        
        currentFragment = fragments.pop();
        
        this.getChildren().clear();
        this.getChildren().add(currentFragment);
        
        if (panelHost != null) {
            panelHost.getChildren().clear();
            panelHost.getChildren().add(currentFragment.getPanel()); 
        }
    }
}
