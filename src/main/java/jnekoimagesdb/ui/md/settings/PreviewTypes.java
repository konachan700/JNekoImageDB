package jnekoimagesdb.ui.md.settings;

import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgPreviewSizes;
import jnekoimagesdb.domain.DSPreviewSize;
import jnekoimagesdb.ui.md.controls.HSeparator;
import jnekoimagesdb.ui.md.controls.LabeledBox;
import jnekoimagesdb.ui.md.controls.NumericTextField;
import jnekoimagesdb.ui.md.dialogs.YesNoBox;
import jnekoimagesdb.ui.md.dialogs.YesNoBoxResult;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;

public class PreviewTypes extends VBox {
    private static PreviewTypes
            previewTypes = null;
    
    public final static String
            CSS_FILE = new File("./style/style-gmd-albums.css").toURI().toString();
        
    private final ScrollPane 
            listSP = new ScrollPane();
    
    private final VBox
            listContainer = new VBox(),
            addContainer = new VBox();
    
    private final NumericTextField
            prevX = new NumericTextField("main_window_good_input", "main_window_badly_input"),
            prevY = new NumericTextField("main_window_good_input", "main_window_badly_input");
    
    private final Button 
            btnCreate = new Button("Создать");
    
    private final CheckBox
            cutSquare = new CheckBox("Обрезать до квадрата");
    
    private final Label 
            textPreviews = new Label();
    
    private final TopPanel
            panelTop;
    
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images");
    
    private final TopPanelMenuButton 
            menuBtn = new TopPanelMenuButton();
    
    private final PreviewTypesElementActionListener
            elementsAL = new PreviewTypesElementActionListener() {
        @Override
        public void OnSetDefault(PreviewTypesElement element, DSPreviewSize size) {
            XImg.getPSizes().setPrimary(size);
            refresh();
        }

        @Override
        public void OnDelete(PreviewTypesElement element, DSPreviewSize size) {
            if (YesNoBox.show("Вы уверенны, что хотите удалить элемент?", "Да, удалить", "Нет") == YesNoBoxResult.YES) {
                XImg.getPSizes().deletePreviewSize(size);
                refresh();
            }
        }
    };
    
    private PreviewTypes() {
        super();
        
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_null_pane");
        
        panelTop = new TopPanel(); 
        panelTop.addNode(infoBox);
        infoBox.setTitle("Управление превью");
        infoBox.setText(""); 
        
        menuBtn.addMenuItem("Перестроить кеш миниатюр", (c) -> {
            
        });
        menuBtn.addMenuItem("Очистить все кеши", (c) -> {
            
        });
        panelTop.addNode(menuBtn);
        
        listSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        listSP.setFitToHeight(false);
        listSP.setFitToWidth(true);
        listSP.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_element_textarera");
        listSP.setContent(listContainer);
        
        listContainer.getStyleClass().addAll("albums_max_width", "albums_null_pane");
        
        cutSquare.getStyleClass().addAll("main_window_messagebox_text");
        cutSquare.setSelected(true);
        
        btnCreate.getStyleClass().addAll("main_window_messagebox_button");
        btnCreate.setOnAction(c -> {
                XImg.getPSizes().addPreviewSize(prevX.getLong(), prevY.getLong(), cutSquare.isSelected());
                refresh();
        });
        
        textPreviews.setAlignment(Pos.TOP_LEFT);
        textPreviews.setWrapText(true);
        textPreviews.getStyleClass().addAll("main_window_max_width", "pt_info_text_height", "main_window_messagebox_text");
        textPreviews.setText("Здесь можно добавить или удалить размеры превью, а также заранее создать или очистить кеши. Редактирование уже созданных размеров невозможно. "
                + "После переключения типа превью рекомендуем перезапустить программу, в текушей версии могут быть баги интерфейса.");
        
        prevX.setMinMax(32, 1024);
        prevX.setText("128");
        
        prevY.setMinMax(32, 1024);
        prevY.setText("128");
        
        addContainer.setAlignment(Pos.CENTER);
        addContainer.getStyleClass().addAll("albums_max_width", "albums_null_pane", "settings_separator");
        addContainer.getChildren().addAll(
                textPreviews,
                new LabeledBox("Ширина превью", prevX),
                new LabeledBox("Высота превью", prevY),
                cutSquare,
                btnCreate,
                new HSeparator()
        );
        
        final Label listText = new Label();
        listText.setAlignment(Pos.TOP_LEFT);
        listText.setWrapText(true);
        listText.getStyleClass().addAll("main_window_max_width", "main_window_messagebox_text");
        listText.setText("Список превью");
        
        this.getChildren().addAll(
                addContainer,
                listText,
                listSP
        );
        
        refresh();
    }
    
    public final void refresh() {
        listContainer.getChildren().clear();
        final XImgPreviewSizes sizes = XImg.getPSizes();
        sizes.getPreviewSizes().forEach(element -> {
            listContainer.getChildren().addAll(
                    new PreviewTypesElement(element, elementsAL)
            );
        });
    }
    
    public Node getPanel() {
        return panelTop;
    }
    
    public static PreviewTypes get() {
        if (previewTypes == null) previewTypes = new PreviewTypes();
        return previewTypes;
    }
}
