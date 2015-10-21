package smallpaginator;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class SmallPaginatorLabel extends Label {
    private String ID = "";
    private int Value = 0;
    private SmallPaginatorActionListener AL = null;
    private final SmallPaginatorLabel THIS = this;
    
    @SuppressWarnings("Convert2Lambda")
    private final EventHandler<MouseEvent> onMouseEvent = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                 if (AL != null) AL.OnPageChange(Value);
            }
        }
    };
    
    public SmallPaginatorLabel(String text, double sz, SmallPaginatorActionListener a) {
        super();
        this.setText(text);
        this.setOnMouseClicked(onMouseEvent);
        setSize(sz); 
        AL = a;
    }

    public SmallPaginatorLabel(int index, double sz, SmallPaginatorActionListener a) {
        super();
        this.setText(Integer.toString(index)); 
        this.setOnMouseClicked(onMouseEvent);
        setSize(sz); 
        Value = index;
        AL = a;
    }
    
    private void setSize(double sz) {
        this.setMaxSize(sz, 24);
        this.setMinSize(sz, 24);
        this.setPrefSize(sz, 24); 
        this.setAlignment(Pos.CENTER); 
    }

    public SmallPaginatorLabel setID(String _ID) {
        ID = _ID;
        return this;
    }
    
    public String getID() {
        return ID;
    }
    
    public void setActionListener(SmallPaginatorActionListener _AL) {
        AL = _AL;
    }
    
    public int getValue() {
        return Value;
    }
    
    public void setValue(int i) {
        Value = i;
    }
}
