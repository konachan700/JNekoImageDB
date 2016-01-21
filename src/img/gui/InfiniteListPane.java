package img.gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jnekoimagesdb.GUITools;

public class InfiniteListPane extends ScrollPane {
    public static final int
            ACTION_TYPE_SCROLL_UP      = 1,
            ACTION_TYPE_SCROLL_DOWN    = 2,
            ACTION_TYPE_V_RESIZE       = 3,
            ACTION_TYPE_H_RESIZE       = 4;
    
    private final VBox 
            rootPanel = new VBox(),
            waitPanel = new VBox();
    
    private final Pane
            topDummy = new Pane(),
            bottomDummy = new Pane();
    
    private final FlowPane
            mainContainer = new FlowPane();
    
    private boolean
            scrollDownTrigger       = false,
            scrollUpTrigger         = false,
            scrollDisable           = false;
    
    private int
            scrollCounterX          = 1,
            itemsInvisible          = 0,
            scrollSpeed             = 64, // скорость прокрутки
            downCounter             = 0,
            upCounter               = 0,
            spaceSize               = 4,
            posCounter              = 0,
            rowSize                 = (128 + spaceSize);

    private long
            scrollBlockCounter      = 0,
            scrollBlockCounterMax   = Long.MAX_VALUE;
    
    private double
            myFullHeigth        = 0D, // полная высота контейнера
            myVisibleHeight     = 0D, // высота видимого окна листа
            myVisibleWidth      = 0D;
            
    private final StringBuilder 
            waitingText = new StringBuilder();
    
    private InfiniteListPaneActionListener
            actionListener = null;

    private final Timeline animationTimer = new Timeline(new KeyFrame(Duration.millis(1), ae -> {
        if (!scrollDisable) {
            if (scrollDownTrigger) {
                scrollDown();
                downCounter++;
                if (downCounter >= scrollSpeed) {
                    downCounter = 0;
                    scrollDownTrigger = false;
                }
            }

            if (scrollUpTrigger) {
                scrollUp();
                upCounter++;
                if (upCounter >= scrollSpeed) {
                    upCounter = 0;
                    scrollUpTrigger = false;
                }
            }
        }
        
        posCounter++;
        if (posCounter > rowSize) {
            posCounter = 0;
            centerScroll();
        }
    }));

    @SuppressWarnings("LeakingThisInConstructor")
    public InfiniteListPane() {
        super();
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.NEVER);
        this.setContent(rootPanel);
        this.setFitToWidth(true);
        this.setFitToHeight(false);
        GUITools.setStyle(this, "InfinityList", "root_pane");
        
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                myVisibleWidth = newValue.doubleValue();
                if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_H_RESIZE, myVisibleWidth, myVisibleHeight, myFullHeigth);
            }
        });

        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                //final int visItemCount = newValue.intValue() / rowSize;
                final int visItemHeight = newValue.intValue(); //visItemCount * rowSize;
                final int invisItemHeight = itemsInvisible * rowSize;
                
                myVisibleHeight = newValue.doubleValue();
                myFullHeigth = (invisItemHeight * 2) + visItemHeight;
                
                rootPanel.setMaxHeight(myFullHeigth);
                rootPanel.setMinHeight(myFullHeigth);
                rootPanel.setPrefHeight(myFullHeigth);
                
                if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_V_RESIZE, myVisibleWidth, myVisibleHeight, myFullHeigth);
            }
            centerScroll();
        });
        
        waitPanel.setAlignment(Pos.CENTER);
        rootPanel.setOnScroll((ScrollEvent event) -> {
            centerScroll();
            event.consume();
        });

        rootPanel.setAlignment(Pos.CENTER);
        rootPanel.getChildren().addAll(topDummy, mainContainer, bottomDummy);
        rootPanel.setOnScroll((ScrollEvent event) -> {
            centerScroll();
            event.consume();
            
            if (scrollDownTrigger || scrollUpTrigger) return;
            if (scrollDisable) return;
            
            if (event.getDeltaY() > 0) 
                scrollDownTrigger = true;
            else 
                scrollUpTrigger = true;
        });
        
        mainContainer.setMaxHeight(9999);
        mainContainer.setPrefHeight(9999);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setHgap(spaceSize);
        mainContainer.setVgap(spaceSize);

        GUITools.setStyle(mainContainer, "InfinityList", "mainContainer");
        GUITools.setStyle(topDummy, "InfinityList", "topDummy");
        GUITools.setStyle(bottomDummy, "InfinityList", "bottomDummy");

        scrollCounterX = rowSize - 1;
        setTopDummyHeight(rowSize - 1);
        setBottomDummyHeight(1);

        animationTimer.setCycleCount(Animation.INDEFINITE);
        animationTimer.play();
    }

    public final void setScrollMax(long sz) {
        scrollBlockCounterMax = sz;
    }
    
    public final boolean isScrollDisabled() {
        return scrollDisable;
    }
    
    public final void setScrollTop() {
        scrollCounterX = rowSize - 1;
        setTopDummyHeight(rowSize - 1);
        setBottomDummyHeight(1);
        scrollBlockCounter = 0;
    }
    
    public final void setDisableScroll(boolean e) {
        scrollDisable = e;
        if (e) {
            scrollCounterX = rowSize / 2;
            setTopDummyHeight(rowSize / 2);
            setBottomDummyHeight(rowSize / 2);
        } else {
            scrollCounterX = rowSize - spaceSize;
            setTopDummyHeight(rowSize - spaceSize);
            setBottomDummyHeight(spaceSize);
        }
    }
    
    public final void setRowSize(int sz) {
        rowSize = sz + spaceSize;
    }
    
    public final void setInvisibleItemsCount(int c) {
        itemsInvisible = c;
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
    
    public final long getCurrentRow() {
        return scrollBlockCounter;
    }
    
    public final boolean isMinimum() {
        return (scrollBlockCounter <= 0);
    }
    
    public final boolean isMaximum() {
        return (scrollBlockCounter >= scrollBlockCounterMax);
    }
    
    private void scrollUp() {
        if (scrollCounterX > 0) 
            scrollCounterX--;
        else {
            if (scrollBlockCounter >= scrollBlockCounterMax) return; else scrollBlockCounter++;
            scrollCounterX = rowSize - 1;
            if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_SCROLL_UP, myVisibleWidth, myVisibleHeight, myFullHeigth);
        }
        
        setTopDummyHeight(scrollCounterX);
        setBottomDummyHeight(rowSize - scrollCounterX);
    }
    
    private void scrollDown() {
        if (scrollCounterX < rowSize) 
            scrollCounterX++;
        else {
            if (scrollBlockCounter <= 0) return; else scrollBlockCounter--;
            scrollCounterX = 1;
            if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_SCROLL_DOWN, myVisibleWidth, myVisibleHeight, myFullHeigth);
        }
        
        setTopDummyHeight(scrollCounterX);
        setBottomDummyHeight(rowSize - scrollCounterX);
    }
    
    private void centerScroll() {
        double h = this.getContent().getBoundsInLocal().getHeight();
        double y = (rootPanel.getBoundsInParent().getMaxY() + rootPanel.getBoundsInParent().getMinY()) / 2.0;
        double v = this.getViewportBounds().getHeight();
        this.setVvalue(this.getVmax() * ((y - 0.5 * v) / (h - v)));    
    }
    
    public final void setWait(boolean wait) {
        if (wait) {
            animationTimer.stop();
            this.setContent(waitPanel);
            this.setFitToWidth(true);
            this.setFitToHeight(true); 
        } else {
            this.setContent(rootPanel);
            this.setFitToWidth(true);
            this.setFitToHeight(false); 
            animationTimer.setCycleCount(Animation.INDEFINITE);
            animationTimer.play();
        }
    }
    
    public final StringBuilder getWaitText() {
        return waitingText;
    }
    
    public final void addItem(Node n) {
        mainContainer.getChildren().add(n);
    }
    
    public final void removeItem(Node n) {
        mainContainer.getChildren().remove(n);
    }
    
    public final void clearAll() {
        mainContainer.getChildren().clear();
    }
    
    public final void setAL(InfiniteListPaneActionListener al) {
        actionListener = al;
    }
}
