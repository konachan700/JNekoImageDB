package ui.paginator;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import jiconfont.javafx.IconNode;
import org.slf4j.LoggerFactory;
import ui.inputbox.NumericTextField;

public class Paginator extends HBox {
    private final org.slf4j.Logger 
            logger = LoggerFactory.getLogger(Paginator.class);

    public static final int PAGINATOR_TYPE_FULL = 1;
    public static final int PAGINATOR_TYPE_SMALL = 2;
        
    private int 
            currentPage = 1,
            pageCount = 0;
    
    private final Label 
            totalCount = new Label("0");
    
    private final HBox
            pagesContainer = new HBox(),
            navToContainer = new HBox();
    
    private final IconNode 
            iconPrev = new IconNode(),
            iconNext = new IconNode(),
            iconEnd = new IconNode(),
            iconStart = new IconNode(),
            iconNavTo = new IconNode();
    
    private final NumericTextField
            navToText = new NumericTextField("main_window_good_input", "main_window_badly_input");
    
    private final PaginatorActionListener
            actListener;
    
    public Paginator(PaginatorActionListener pl, int pageinatorType) {
        super();
        actListener = pl;
        //this.getStylesheets().add(getClass().getResource("/style/css/paginator.css").toExternalForm());
        this.getStyleClass().addAll("paginator_max_width", "paginator_max_height", "paginator_root_pane", "paginator_root_spacer");
        
        pagesContainer.setAlignment(Pos.CENTER);
        pagesContainer.getStyleClass().addAll("paginator_max_width", "paginator_max_height", "paginator_null_pane");
        
        navToContainer.setAlignment(Pos.CENTER);
        navToContainer.getStyleClass().addAll("paginator_max_height", "paginator_null_pane", "paginator_root_spacer");
        
        iconPrev.getStyleClass().addAll("paginator_null_pane", "paginator_icon", "paginator_icon_prev");
        iconNext.getStyleClass().addAll("paginator_null_pane", "paginator_icon", "paginator_icon_next");
        iconStart.getStyleClass().addAll("paginator_null_pane", "paginator_icon", "paginator_icon_start");
        iconEnd.getStyleClass().addAll("paginator_null_pane", "paginator_icon", "paginator_icon_end");
        iconNavTo.getStyleClass().addAll("paginator_null_pane", "paginator_icon", "paginator_icon_navto");
        
        navToText.setAlignment(Pos.CENTER);
        navToText.getStyleClass().addAll("paginator_text_width", "paginator_max_height", "paginator_text_pane");
        navToText.setText("1");
        
        iconPrev.setOnMouseClicked(c -> {
            pagPrev();
        });
        
        iconNext.setOnMouseClicked(c -> {
            pagNext();
        });
        
        iconStart.setOnMouseClicked(c -> {
            currentPage = 1;
            refresh();
        });
        
        iconEnd.setOnMouseClicked(c -> {
            currentPage = pageCount;
            refresh();
        });
        
        iconNavTo.setOnMouseClicked(c -> {
            if (!navToText.isValid()) return;
            currentPage = navToText.getInt();
            refresh();
        });
        
        navToText.setOnKeyPressed((KeyEvent key) -> {
            if (key.getCode() == KeyCode.ENTER) {
                if (!navToText.isValid()) return;
                currentPage = navToText.getInt();
                refresh();
            }
        });
        
        totalCount.setAlignment(Pos.CENTER);
        totalCount.getStyleClass().addAll("paginator_max_height", "paginator_total_count_label");
        totalCount.setMinWidth(Region.USE_PREF_SIZE);

        if (pageinatorType == PAGINATOR_TYPE_FULL) {
            this.getChildren().addAll(
                    pagesContainer,
                    navToContainer
            );
            navToContainer.getChildren().addAll(
                    navToText,
                    iconNavTo
            );
        } else if (pageinatorType == PAGINATOR_TYPE_SMALL) {
            this.getChildren().addAll(pagesContainer);
        }

        pagesContainer.getChildren().addAll(
                iconStart,
                iconPrev,
                totalCount,
                iconNext,
                iconEnd
        );
        
        refresh();
    }
    
    private void refresh() {
        totalCount.setText("Page " + currentPage + " of " + pageCount);
        if ((pageCount > 0) && (currentPage > 0)) actListener.OnPageChange(currentPage, pageCount);
    }
    
    public final void pagNext() {
        if (currentPage < pageCount) {
            currentPage++;
            refresh();
        }
    }
    
    public final void pagPrev() {
        if (currentPage > 1) {
            currentPage--;
            refresh();
        }
    }
    
    public final boolean isOnStart() {
        return (currentPage <= 1);
    }
    
    public final boolean isOnEnd() {
        return (currentPage >= pageCount);
    }
    
    public int getCurrentPageIndex() {
        return currentPage;
    }
    
    public void setCurrentPageIndex(int p) {
        if (pageCount > pageCount) return;
        currentPage = p;
        totalCount.setText("Page " + currentPage + " of " + pageCount);
    }
    
    public void setPageCount(int p) {
        pageCount = p;
        totalCount.setText("Page " + currentPage + " of " + pageCount);
        navToText.setMinMax(1, pageCount);
    }
}
