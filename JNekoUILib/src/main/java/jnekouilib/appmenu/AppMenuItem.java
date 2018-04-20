package jnekouilib.appmenu;

import java.util.ArrayList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class AppMenuItem extends Label {
    private final AppMenuActionListener mil;
    private boolean selected = false;
    private final ArrayList<AppMenuActionListener> serviceAL = new ArrayList<>();

    public AppMenuItem(String text, AppMenuActionListener m) {
        super(text);
        mil = m;
        this.getStyleClass().addAll("menuItem");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setOnMouseClicked(c -> {
            if (!selected) {
                mil.OnClick(c);
                serviceAL.forEach(x -> {
                    x.OnClick(c); 
                });
                setSelected(true);
            }
        });
    }
    
    public AppMenuItem defaultSelected() {
        setSelected(true);
        return this;
    }
    
    public final void setSelected(boolean s) {
        selected = s;
        if (s) 
            this.getStyleClass().add("menuSelectedItem");
        else
            this.getStyleClass().removeAll("menuSelectedItem");
    }
    
    public final boolean isSelected() {
        return selected;
    }
    
    protected final void registerServiceAL(AppMenuActionListener s) {
        serviceAL.add(s);
    }
}
