package smallpaginator;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

public class SmallPaginator extends HBox  {
    private final SmallPaginatorLabel
            prevLabel,
            nextLabel,
            numLabel;
    
    private int 
            pageCount = 0,
            currPage = 0;
    
    @SuppressWarnings("Convert2Lambda")
    private final SmallPaginatorActionListener AL = new SmallPaginatorActionListener() {
        @Override
        public void OnPageChange(int page) {
            if (page == 1) Prev();
            if (page == 2) Next();
        }
    };
    
    private final SmallPaginatorActionListener ALX;
    
    public void Prev() {
        if (currPage > 0) {
            currPage--;
            numLabel.setText((currPage+1) + "/" + pageCount);
            ALX.OnPageChange(currPage);
        }
    }
    
    public void Next() {
        if (currPage < (pageCount-1)) {
            currPage++;
            numLabel.setText((currPage+1) + "/" + pageCount);
            ALX.OnPageChange(currPage);
        }
    }
    
    public SmallPaginator(SmallPaginatorActionListener a) {
        super(2);
        ALX = a;
        
        prevLabel = new SmallPaginatorLabel("←", 48, AL);
        nextLabel = new SmallPaginatorLabel("→", 48, AL);
        numLabel = new SmallPaginatorLabel("0/0", 128, null);
        
        prevLabel.setValue(1);
        nextLabel.setValue(2);

        this.getStylesheets().add(getClass().getResource("MainStyles.css").toExternalForm());
        this.getStyleClass().add("SmallPaginator");
        prevLabel.getStyleClass().add("SmallPaginatorPREV");
        nextLabel.getStyleClass().add("SmallPaginatorNEXT");
        numLabel.getStyleClass().add("SmallPaginatorN");
        
        this.setPrefSize(900, 24);
        this.setMaxSize(900, 24);
        this.setMinSize(32, 24);
        this.setAlignment(Pos.CENTER_RIGHT);
        
        this.getChildren().add(prevLabel);
        this.getChildren().add(numLabel);
        this.getChildren().add(nextLabel);
    }
    
    public final void setCurrentPage(int p) {
        currPage = p;
        numLabel.setText((currPage+1) + "/" + pageCount); 
    }
    
     public final void setPageCount(int c) {
         pageCount = c;
         numLabel.setText((currPage+1) + "/" + pageCount);
     }
}
