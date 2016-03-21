package jnekoimagesdb.ui.controls.tabs;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.PreviewTypesList;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.SNumericTextField;
import jnekoimagesdb.ui.controls.elements.SPathBox;
import jnekoimagesdb.ui.controls.elements.SScrollPane;
import jnekoimagesdb.ui.controls.elements.STabTextButton;

public class TabSettings extends SEVBox {
    
    private final PreviewTypesList
            prevTypes = new PreviewTypesList();
    
    private final SScrollPane
            itemsScroll = new SScrollPane();
    
    private final SEVBox
            scrollableContainer = new SEVBox(0);
    
    private final SNumericTextField
            previewFSCacheThreadsCount = new SNumericTextField(ElementsIDCodes.textUnknown, 100, 32, null), 
            mainPreviewGenThreadsCount = new SNumericTextField(ElementsIDCodes.textUnknown, 100, 32, null);
    
    private final SPathBox
            pathBrowserExchange = new SPathBox(-1, 32),
            pathAlbumsExchange = new SPathBox(-1, 32);
    
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
                new STabTextButton("Сохранить", ElementsIDCodes.buttonUnknown, 100, 32, (x, y) -> {
                    final int 
                            mainTC = mainPreviewGenThreadsCount.getIntValue(),
                            prevTC = previewFSCacheThreadsCount.getIntValue();
                    if ((mainTC > 0) && (prevTC > 0)) {
                        SettingsUtil.setLong("mainPreviewGenThreadsCount.value", mainTC);
                        SettingsUtil.setLong("previewFSCacheThreadsCount.value", prevTC);
                        XImg.msgbox("Настройки вступят в силу только после перезапуска программы!");
                    } else {
                        XImg.msgbox("Данные введены неправильно!");
                    }
                    
                }), 1, 2);
        
        mtOptions.getChildren().addAll(
                new SFLabel("Настройка многопоточности", 64, 9999, 20, 20, "label_darkgreen", "TypesListItem"),
                new SFLabel("Количество потоков напрямую влияет на скорость генерации превью, однако на слабых машинах большое количество потоков "
                        + "приведет к падению общей производительности системы. Процессоров в системе: "+Runtime.getRuntime().availableProcessors(), 
                        64, 9999, 44, 44, "label_darkgreen_small", "TypesListItem"),
                gpMtOptions
        );
        
        final SEVBox folderOptions = new SEVBox(2, "svbox_sett_container_blue");
        folderOptions.setAlignment(Pos.CENTER_RIGHT);
        folderOptions.getChildren().addAll(
                new SFLabel("Настройка папок обмена", 64, 9999, 20, 20, "label_darkblue", "TypesListItem"),
                new SFLabel("Папка обмена для браузера", 64, 9999, 24, 24, "label_darkblue_small", "TypesListItem"),
                pathBrowserExchange,
                new SFLabel("Папка обмена для выгрузки альбомов", 64, 9999, 24, 24, "label_darkblue_small", "TypesListItem"),
                pathAlbumsExchange,
                new STabTextButton("Сохранить", ElementsIDCodes.buttonUnknown , 100, 32, (x, y) -> {
                    if (pathBrowserExchange.isNull() || pathAlbumsExchange.isNull()) return;
                    SettingsUtil.setPath("pathBrowserExchange", pathBrowserExchange.getValue());
                    SettingsUtil.setPath("pathAlbumsExchange", pathAlbumsExchange.getValue());
                })
        );
        
        
        
        scrollableContainer.getChildren().addAll(
                prevTypes,   
                GUITools.getHSeparator(4),
                mtOptions,
                GUITools.getHSeparator(4),
                folderOptions
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
        mainPreviewGenThreadsCount.setText("" + SettingsUtil.getLong("mainPreviewGenThreadsCount.value", defaultThreadCount));
        previewFSCacheThreadsCount.setText("" + SettingsUtil.getLong("previewFSCacheThreadsCount.value", defaultThreadCount));
        pathBrowserExchange.setValue(SettingsUtil.getPath("pathBrowserExchange"));
        pathAlbumsExchange.setValue(SettingsUtil.getPath("pathAlbumsExchange"));
    }
    
}
