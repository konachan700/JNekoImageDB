package jnekoimagesdb.ui.md.settings;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.md.controls.HSeparator;
import jnekoimagesdb.ui.md.controls.LabeledBox;
import jnekoimagesdb.ui.md.controls.NumericTextField;
import jnekoimagesdb.ui.md.controls.PathTextField;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;

public class Settings extends ScrollPane {
    private static Settings
            settings = null;

    private final VBox 
            rootContainer = new VBox();
    
    private final NumericTextField
            threadsFSPrevGen = new NumericTextField("main_window_good_input", "main_window_badly_input"),
            threadsMainPrevGen = new NumericTextField("main_window_good_input", "main_window_badly_input");
    
    private final PathTextField
            browserFolder = new PathTextField("main_window_good_input", "main_window_badly_input");
    
    private final Label 
            textThreads = new Label(),
            textBrowserFolder = new Label();
    
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images");
    
    private final TopPanel
            panelTop;
    
    private Settings() {
        super();
        
        final int 
                processorsCount = Runtime.getRuntime().availableProcessors(),
                defaultThreadCount = (processorsCount < 4) ? 1 : processorsCount / 4;
        
        panelTop = new TopPanel(); 
        panelTop.addNode(infoBox);
        infoBox.setTitle("Настройки программы");
        infoBox.setText("Version 0.1a testing. Not for regular use."); 
        
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setFitToHeight(false);
        this.setFitToWidth(true);
        this.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_element_textarera");
        
        rootContainer.setAlignment(Pos.TOP_CENTER);
        rootContainer.getStyleClass().addAll("albums_max_width", "albums_x_root_pane", "albums_h_space");

        textThreads.setAlignment(Pos.TOP_LEFT);
        textThreads.setWrapText(true);
        textThreads.getStyleClass().addAll("main_window_max_width", "main_window_messagebox_text");
        textThreads.setText("Количество потоков напрямую влияет на скорость генерации превью, однако на слабых машинах большое количество потоков (много больше числа процессоров) "
                + "приведет к падению общей производительности системы. Процессоров в системе: "+processorsCount+".");
        
        threadsFSPrevGen.setMinMax(1, (processorsCount <= 1) ? 2 : processorsCount);
        threadsFSPrevGen.setText("" + SettingsUtil.getLong("previewFSCacheThreadsCount.value", defaultThreadCount));
        threadsFSPrevGen.setActionListener(node -> {      
                SettingsUtil.setLong("previewFSCacheThreadsCount.value", threadsFSPrevGen.getLong());
        });
        
        threadsMainPrevGen.setMinMax(1, (processorsCount <= 1) ? 2 : processorsCount);
        threadsMainPrevGen.setText("" + SettingsUtil.getLong("mainPreviewGenThreadsCount.value", defaultThreadCount));
        threadsMainPrevGen.setActionListener(node -> {
                SettingsUtil.setLong("mainPreviewGenThreadsCount.value", threadsMainPrevGen.getLong());
        });
        
        textBrowserFolder.setAlignment(Pos.TOP_LEFT);
        textBrowserFolder.setWrapText(true);
        textBrowserFolder.getStyleClass().addAll("main_window_max_width", "main_window_messagebox_text");
        textBrowserFolder.setText("Путь к папке обмена для браузера. Этот функционал позволяет одним нажатием сохранять выделенные картинки в указанную тут папку. "
                + "Это очень удобно при общении на имиджбордах или при ведении пабликов в соцсетях.");
        
        browserFolder.setText(SettingsUtil.getString("pathBrowserExchange", "./")); 
        browserFolder.setActionListener(node -> {
                SettingsUtil.setString("pathBrowserExchange", browserFolder.getPathString());
        });
        
        
        
        rootContainer.getChildren().addAll(
                textThreads,
                new LabeledBox("Кол-во потоков в файл-менеджере", threadsFSPrevGen),
                new LabeledBox("Кол-во потоков в генераторе превью", threadsMainPrevGen),
                new HSeparator(),
                textBrowserFolder,
                browserFolder,
                new HSeparator()
        );
             
        this.setContent(rootContainer);
    }
    
    public Node getPanel() {
        return panelTop;
    }
    
    public static Settings getSettingBox() {
        if (settings == null) settings = new Settings();
        return settings;
    }
}
