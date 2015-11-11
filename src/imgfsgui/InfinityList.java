package imgfsgui;

import dataaccess.Lang;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class InfinityList extends ScrollPane{
    public static final int
            ACTION_TYPE_SCROLL_UP      = 1,
            ACTION_TYPE_SCROLL_DOWN    = 2,
            ACTION_TYPE_V_RESIZE       = 3,
            ACTION_TYPE_H_RESIZE       = 4;
    
    private final VBox 
            rootPanel = new VBox();
    
    private final Pane
            topDummy = new Pane(),
            bottomDummy = new Pane();
    
    private final FlowPane
            mainContainer = new FlowPane();
    
    private int
            rowSize             = (128 + 16),
            scrollSpeed         = 16, // скорость прокрутки
            scrollCounter       = 0, // счетчик прокрутки, сбрасывается при превышении rowSize
            scrollCounterLast   = 0;
    
    private double
            myFullHeigth        = 0D, // полная высота контейнера
            myVisibleHeight     = 0D, // высота видимого окна листа
            myVisibleWidth      = 0D;
            
    private InfinityListActionListener
            actionListener = null;
    
    private volatile boolean
            isScrolled = false;
    
    private final Timeline animationTimer = new Timeline(new KeyFrame(Duration.millis(5), ae -> {
        if (isScrolled) {
            if (scrollCounterLast < scrollCounter) {
                scrollCounterLast++;
            } else {
                scrollCounterLast--; 
            }
            
            setTopDummyHeight(rowSize + scrollCounterLast);
            setBottomDummyHeight(rowSize - scrollCounterLast);
            
            if (scrollCounterLast == scrollCounter) isScrolled = false;
        }
    }));

    public InfinityList() {
        super();
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.NEVER);
        this.setContent(rootPanel);
        this.setFitToWidth(true);
        this.setFitToHeight(false);
        this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                myVisibleWidth = newValue.doubleValue();
                if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_H_RESIZE, myVisibleWidth, myVisibleHeight, myFullHeigth);
            }
        });

        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                myVisibleHeight = newValue.doubleValue();
                myFullHeigth = myVisibleHeight + (rowSize * 4);
                rootPanel.setMaxHeight(myFullHeigth);
                rootPanel.setMinHeight(myFullHeigth);
                rootPanel.setPrefHeight(myFullHeigth);
                if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_V_RESIZE, myVisibleWidth, myVisibleHeight, myFullHeigth);
            }
            centerScroll();
        });
        
        rootPanel.setAlignment(Pos.CENTER);
        rootPanel.getChildren().addAll(topDummy, mainContainer, bottomDummy);
        rootPanel.setOnScroll((ScrollEvent event) -> {
            if (isScrolled) {
                event.consume();
                return;
            }
            
            scrollCounterLast = scrollCounter;
            
            centerScroll();
            event.consume();
            
            if (event.getDeltaY() < 0) {
                scrollCounter += scrollSpeed;
                if (scrollCounter > rowSize) {
                    scrollCounter = (-1 * rowSize);
                    setTopDummyHeight(rowSize + scrollCounter);
                    setBottomDummyHeight(rowSize - scrollCounter);
                    if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_SCROLL_DOWN, myVisibleWidth, myVisibleHeight, myFullHeigth);
                    return;
                }
            } else {
                scrollCounter -= scrollSpeed;
                if ((scrollCounter < (-1 * rowSize))) {
                    scrollCounter = Math.abs(rowSize);
                    setTopDummyHeight(rowSize + scrollCounter);
                    setBottomDummyHeight(rowSize - scrollCounter);
                    if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_SCROLL_UP, myVisibleWidth, myVisibleHeight, myFullHeigth);
                    return;
                }
            }

            isScrolled = true;
        });
        
        mainContainer.setMaxHeight(9999);
        mainContainer.setPrefHeight(9999);
        mainContainer.setAlignment(Pos.CENTER);

        
        mainContainer.getStyleClass().add("InfinityList_mainContainer");
        topDummy.getStyleClass().add("InfinityList_topDummy");
        bottomDummy.getStyleClass().add("InfinityList_bottomDummy");
        
        HBox c = new HBox();
        mainContainer.getChildren().add(c);
        
        c.getStyleClass().add("InfinityList_bottomT");
        c.setMaxHeight(rowSize);
        c.setPrefHeight(rowSize);
        c.setMinHeight(rowSize);
        c.setMinWidth(rowSize);
        
        animationTimer.setCycleCount(Animation.INDEFINITE);
        animationTimer.play();
    }

    public void setRowSize(int sz) {
        rowSize = sz;
    }
    
    public void setScrollSpeed(int value) {
        scrollSpeed = value;
    }

    private void setTopDummyHeight(double sz) {
        topDummy.setMaxHeight(sz);
        topDummy.setMinHeight(sz);
        topDummy.setPrefHeight(sz);
    }
    
    private void setBottomDummyHeight(double sz) {
        bottomDummy.setMaxHeight(sz);
        bottomDummy.setMinHeight(sz);
        bottomDummy.setPrefHeight(sz);
    }
    
    private void centerScroll() {
        double h = this.getContent().getBoundsInLocal().getHeight();
        double y = (rootPanel.getBoundsInParent().getMaxY() + rootPanel.getBoundsInParent().getMinY()) / 2.0;
        double v = this.getViewportBounds().getHeight();
        this.setVvalue(this.getVmax() * ((y - 0.5 * v) / (h - v)));    
    }
    
    public void addItem(Node n) {
        mainContainer.getChildren().add(n);
    }
    
    public void removeItem(Node n) {
        mainContainer.getChildren().remove(n);
    }
    
    public void clearAll() {
        mainContainer.getChildren().clear();
    }
}
