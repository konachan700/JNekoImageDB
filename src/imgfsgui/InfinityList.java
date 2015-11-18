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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class InfinityList extends ScrollPane {
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
    
    private int
            spaceSize           = 4,
            rowSize             = (128 + spaceSize),
            itemUnvisibleLines  = 6,
            scrollSpeed         = 16, // скорость прокрутки
            scrollCounter       = 0, // счетчик прокрутки, сбрасывается при превышении rowSize
            scrollCounterLast   = 0;
    
    private long
            scrollBlockCounter      = 0,
            scrollBlockCounterMax   = Long.MAX_VALUE;
    
    private double
            myFullHeigth        = 0D, // полная высота контейнера
            myVisibleHeight     = 0D, // высота видимого окна листа
            myVisibleWidth      = 0D;
            
    private final StringBuilder 
            waitingText = new StringBuilder();
    
    private InfinityListActionListener
            actionListener = null;
    
    private volatile boolean
            isScrolled = false;
    
    private final Timeline animationTimer = new Timeline(new KeyFrame(Duration.millis(1), ae -> {
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
        this.getStyleClass().add("InfinityList");
        
        this.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                myVisibleWidth = newValue.doubleValue();
                if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_H_RESIZE, myVisibleWidth, myVisibleHeight, myFullHeigth);
            }
        });

        this.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() > 0) {
                myVisibleHeight = newValue.doubleValue();
                myFullHeigth = myVisibleHeight + (rowSize * itemUnvisibleLines);
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
        
        setTopDummyHeight(2 * rowSize);
        setBottomDummyHeight(0);
        scrollCounter = (2 * rowSize) - 1;
        scrollCounterLast = scrollCounter;
        isScrolled = true;
        
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
                if (scrollBlockCounter > 0) {
                    scrollCounter += scrollSpeed;
                    
                    if (scrollCounter > rowSize) {
                        scrollBlockCounter--;
//                        if (scrollBlockCounter > 0) {
                            scrollCounter = (-1 * rowSize);
                            setTopDummyHeight(rowSize + scrollCounter);
                            setBottomDummyHeight(rowSize - scrollCounter);
                            if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_SCROLL_DOWN, myVisibleWidth, myVisibleHeight, myFullHeigth);
                            return;
//                        } else {
//                            if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_SCROLL_DOWN, myVisibleWidth, myVisibleHeight, myFullHeigth);
//                            return;
//                        }
                    }
                } else {
                    if (scrollCounter < 2*rowSize) scrollCounter += scrollSpeed;
                }
            } else {
                if (scrollBlockCounter < scrollBlockCounterMax) {
                    scrollCounter -= scrollSpeed;
                    if ((scrollCounter < (-1 * rowSize))) {
                        scrollCounter = Math.abs(rowSize);
                        setTopDummyHeight(rowSize + scrollCounter);
                        setBottomDummyHeight(rowSize - scrollCounter);
                        if (actionListener != null) actionListener.onItemsUpdateNeeded(ACTION_TYPE_SCROLL_UP, myVisibleWidth, myVisibleHeight, myFullHeigth);
                        scrollBlockCounter++;
                        return;
                    }
                } else {
                    if (scrollCounter > (-1 * rowSize)) { scrollBlockCounter++; }
                }
            }

            isScrolled = true;
        });
        
        mainContainer.setMaxHeight(9999);
        mainContainer.setPrefHeight(9999);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setHgap(spaceSize);
        mainContainer.setVgap(spaceSize);

        mainContainer.getStyleClass().add("InfinityList_mainContainer");
        topDummy.getStyleClass().add("InfinityList_topDummy");
        bottomDummy.getStyleClass().add("InfinityList_bottomDummy");

        animationTimer.setCycleCount(Animation.INDEFINITE);
        animationTimer.play();
    }

    public final void setScrollMax(long sz) {
        scrollBlockCounterMax = sz;
    }
    
    public final void setRowSize(int sz) {
        rowSize = sz;
    }
    
    public final void setScrollSpeed(int value) {
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
    
    public final void setAL(InfinityListActionListener al) {
        actionListener = al;
    }
}
