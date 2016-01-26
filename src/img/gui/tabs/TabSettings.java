package img.gui.tabs;

import datasources.SettingsUtil;
import img.gui.PreviewTypesList;
import img.gui.elements.SEVBox;
import img.gui.elements.SFHBox;
import img.gui.elements.SFLabel;
import img.gui.elements.SNumericTextField;
import img.gui.elements.SScrollPane;
import img.gui.elements.STabTextButton;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import jnekoimagesdb.GUITools;

public class TabSettings extends SEVBox   {
    
    private final PreviewTypesList
            prevTypes = new PreviewTypesList();
    
    private final SScrollPane
            itemsScroll = new SScrollPane();
    
    private final SEVBox
            scrollableContainer = new SEVBox(0);
    
    private final SNumericTextField
            previewFSCacheThreadsCount = new SNumericTextField(0, 100, 32, null), 
            mainPreviewGenThreadsCount = new SNumericTextField(0, 100, 32, null);
    
    public TabSettings() {
        super(0);
        
        prevTypes.setMinSize(256, 300);
        prevTypes.setPrefSize(9999, 300);
        prevTypes.setMaxSize(9999, 300);
        
        itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        itemsScroll.setFitToHeight(true);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setContent(scrollableContainer);
        
        final SEVBox mtOptions = new SEVBox("svbox_sett_container_green");
        mtOptions.setAlignment(Pos.CENTER_RIGHT);
        
        final GridPane gpMtOptions = new GridPane();
        GUITools.setMaxSize(gpMtOptions, 9999, 64);
        GUITools.setStyle(gpMtOptions, "TabSettings", "null_gp");
        final ColumnConstraints 
                textCol = new ColumnConstraints(100, 9999, 9999),
                valCol = new ColumnConstraints(120);
        valCol.setHalignment(HPos.RIGHT);
        gpMtOptions.setVgap(2);
        gpMtOptions.getColumnConstraints().addAll(textCol, valCol);
        gpMtOptions.add(new SFLabel("Кол-во потоков генератора превью в просмотрщике", 64, 9999, 32, 32, "label", "TypesListItem"), 0, 0);
        gpMtOptions.add(mainPreviewGenThreadsCount, 1, 0);
        gpMtOptions.add(new SFLabel("Кол-во потоков генератора превью в диалоге выбора файлов", 64, 9999, 32, 32, "label", "TypesListItem"), 0, 1);
        gpMtOptions.add(previewFSCacheThreadsCount, 1, 1);
        gpMtOptions.add(
                new STabTextButton("Сохранить", 0 , 100, 32, (x, y) -> {
                
                
                }), 1, 2);
        
        mtOptions.getChildren().addAll(
                new SFLabel("Настройка многопоточности", 64, 9999, 20, 20, "label_darkgreen", "TypesListItem"),
                new SFLabel("Количество потоков напрямую влияет на скорость генерации превью, однако на слабых машинах большое количество потоков "
                        + "приведет к падению общей производительности системы. Процессоров в системе: "+Runtime.getRuntime().availableProcessors(), 
                        64, 9999, 44, 44, "label_darkgreen_small", "TypesListItem"),
                gpMtOptions
        );
        
        scrollableContainer.getChildren().addAll(
                prevTypes,   
                GUITools.getHSeparator(4),
                mtOptions
        );
        
        this.getChildren().addAll(
                new SFLabel("Настройки программы", 64, 9999, 32, 32, "label_header", "TabSettings"), 
                itemsScroll
        );
    }
    
    public void refresh() {
        prevTypes.refresh();
        final int 
                processorsCount = Runtime.getRuntime().availableProcessors(),
                defaultThreadCount = (processorsCount < 4) ? 1 : processorsCount / 4;
        previewFSCacheThreadsCount.max = (processorsCount <= 1) ? 2 : processorsCount;
        mainPreviewGenThreadsCount.max = (processorsCount <= 1) ? 2 : processorsCount;
        previewFSCacheThreadsCount.min = 1;
        mainPreviewGenThreadsCount.min = 1;
        mainPreviewGenThreadsCount.setText("" + SettingsUtil.getLong("previewFSCacheThreadsCount.value", defaultThreadCount));
        previewFSCacheThreadsCount.setText("" + SettingsUtil.getLong("previewFSCacheThreadsCount.value", defaultThreadCount));
    }
    
}
