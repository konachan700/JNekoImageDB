package ui.elements;

import org.springframework.stereotype.Component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import ui.StyleParser;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;

@Component
@HasStyledElements
public class Paginator extends HBox {
	public interface PageChangeAction {
        void onChange(int currentPage, int pageCount);
    }

    private PageChangeAction pageChangeAction = null;

    private int 
            currentPage = 0,
            pageCount = 0;

    @CssStyle({"paginator_total_count_label"})
    private final Label totalCount = new Label("0");

    @CssStyle({"paginator_null_pane"})
    private final HBox pagesContainer = new HBox();

    @CssStyle({"paginator_icon"})
    private final IconNode iconPrev = new IconNode(GoogleMaterialDesignIcons.SKIP_PREVIOUS);

    @CssStyle({"paginator_icon"})
    private final IconNode iconNext = new IconNode(GoogleMaterialDesignIcons.SKIP_NEXT);

    @CssStyle({"paginator_icon"})
    private final IconNode iconEnd = new IconNode(GoogleMaterialDesignIcons.LAST_PAGE);

    @CssStyle({"paginator_icon"})
    private final IconNode iconStart = new IconNode(GoogleMaterialDesignIcons.FIRST_PAGE);
    
    public Paginator() {
        super();
        pagesContainer.setAlignment(Pos.CENTER);
        StyleParser.parseStyles(this);

        iconPrev.setOnMouseClicked(c -> {
            pagPrev();
        });
        
        iconNext.setOnMouseClicked(c -> {
            pagNext();
        });
        
        iconStart.setOnMouseClicked(c -> {
            currentPage = 0;
            refresh();
        });
        
        iconEnd.setOnMouseClicked(c -> {
            currentPage = pageCount;
            refresh();
        });

        totalCount.setAlignment(Pos.CENTER);
        totalCount.setMinWidth(Region.USE_PREF_SIZE);

        this.getChildren().addAll(pagesContainer);

        pagesContainer.getChildren().addAll(
                iconStart,
                iconPrev,
                totalCount,
                iconNext,
                iconEnd
        );

        setText();
    }

    private void setText() {
        totalCount.setText("Page " + (currentPage+1) + " of " + (pageCount+1));
    }

    private void refresh() {
        setText();
        if ((pageCount > 0) && (currentPage >= 0)) {
            if (getPageChangeAction() != null) {
                getPageChangeAction().onChange(currentPage, pageCount);
            }
        }
    }
    
    public final void pagNext() {
        if (currentPage < (pageCount - 1)) {
            currentPage++;
            refresh();
        }
    }
    
    public final void pagPrev() {
        if (currentPage > 0) {
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
        setText();
    }
    
    public void setPageCount(int p) {
        pageCount = p;
        setText();
    }

	public PageChangeAction getPageChangeAction() {
		return pageChangeAction;
	}

	public void setPageChangeAction(PageChangeAction pageChangeAction) {
		this.pageChangeAction = pageChangeAction;
	}
}
