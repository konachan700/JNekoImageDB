package jnekoimagesdb.ui.md.settings;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import jiconfont.javafx.IconNode;
import jnekoimagesdb.domain.DSPreviewSize;

public class PreviewTypesElement extends HBox {
    private final DSPreviewSize
            thisSize;
    
    private final PreviewTypesElementActionListener
            eAL;
    
    private final Label 
            textPreviews = new Label();
    
    private boolean 
            selected = false;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public PreviewTypesElement(DSPreviewSize sz, PreviewTypesElementActionListener al) {
        super();
        thisSize = sz;
        eAL = al;
        
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().addAll("albums_max_width", "previews_element_height", "albums_element_root_pane");
        
        final IconNode iconNode = new IconNode();
        iconNode.getStyleClass().addAll("previews_element_icon");
        
        textPreviews.setAlignment(Pos.CENTER_LEFT);
        textPreviews.setWrapText(true);
        textPreviews.getStyleClass().addAll("albums_max_width", "albums_max_height", "previews_element_text");
        textPreviews.setText("Ширина: "+sz.getWidth()+"px; Высота: "+sz.getHeight()+"px; "
                +((sz.isSquared()) ? "Квадратные; " : "Без обрезки; ")+
                ((sz.isPrimary()) ? "Используется;" : ""));
        
        this.getChildren().addAll(
                iconNode,
                textPreviews
        );
        
        if (!sz.isPrimary())
            this.getChildren().addAll(
                    new PreviewTypesElementButton("previews_element_set_default_icon", "Установить как размер по-умолчанию", c -> {
                            eAL.OnSetDefault(this, sz); 
                    }),
                    new PreviewTypesElementButton("previews_element_delete_icon", "Удалить", c -> {
                            eAL.OnDelete(this, sz); 
                    })
            );
        
        setSelected(false);
    }
    
    public final void setSelected(boolean sel) {
        selected = sel;
        this.getStyleClass().clear();
        if (selected) 
            this.getStyleClass().addAll("albums_max_width", "previews_element_height", "previews_element_root_pane", "albums_element_root_pane_selected");
        else
            this.getStyleClass().addAll("albums_max_width", "previews_element_height", "previews_element_root_pane", "albums_element_root_pane_non_selected");
    }
    
    public DSPreviewSize getSize() {
        return thisSize;
    }
}
