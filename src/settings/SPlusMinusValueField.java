package settings;

import dataaccess.SQLite;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;

public class SPlusMinusValueField extends HBox {
    private final String ID;
    private final SQLite SQL;
    private final STextField textField;
    private final Button BP, BM;
    
    private long 
            min   = 0,
            max   = 100,
            value = 0;
    
    private double 
            scrollNum = 0;
    
    public void setMinimum(long l) {
        min = l;
    }
    
    public void setMaximum(long l) {
        max = l;
    }
    
    public void setValue(long l) {
        value = l;
    }
    
    public long getValue() {
        return value;
    }
    
    public SPlusMinusValueField(SQLite sql, String id) {
        SQL = sql;
        ID  = id;
        
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("SPlusMinusValueField");
        
        textField = new STextField(SQL, ID);
        textField.setEditable(false);
        textField.setMinSize(9999, 16);
        textField.setMaxSize(9999, 16);
        textField.setPrefSize(9999, 16);
        textField.getStyleClass().add("SPlusMinusValueField_textField");
        textField.setAlignment(Pos.CENTER);
        
        try {
            value = Long.getLong(textField.getText(), 10);
        } catch (Exception e) { 
            value = 0; 
            textField.setText("0");
        }
        
        BP = new Button("", new ImageView(new Image(new File("./icons/up.png").toURI().toString())));
        BP.setOnMouseClicked((MouseEvent event) -> {
            if (value < max) max++;
            textField.setText(""+value);
            event.consume();
        });
        BP.setMinSize(16, 16);
        BP.setMaxSize(16, 16);
        BP.setPrefSize(16, 16);
        BP.getStyleClass().add("SPlusMinusValueField_B");
        
        BM = new Button("", new ImageView(new Image(new File("./icons/dwn.png").toURI().toString())));
        BM.setOnMouseClicked((MouseEvent event) -> {
            if (value > min) value--;
            textField.setText(""+value);
            event.consume();
        });
        BM.setMinSize(16, 16);
        BM.setMaxSize(16, 16);
        BM.setPrefSize(16, 16);
        BM.getStyleClass().add("SPlusMinusValueField_B");
        
        this.setOnScroll((ScrollEvent event) -> {
            scrollNum = scrollNum + event.getDeltaY();
            if (scrollNum >= 60) {
                if (value > min) value--;
                textField.setText(""+value);
                scrollNum = 0;
            }
            
            if (scrollNum <= -60) {
                if (value < max) max++;
                textField.setText(""+value);
                scrollNum = 0;
            }
        });
        
        this.getChildren().addAll(BM, textField, BP);
    }
}
