package jnekoimagesdb.ui.md.dialogs.start;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.core.img.XImgCrypto;
import jnekoimagesdb.core.img.XImgPreviewSizes;
import jnekoimagesdb.domain.HibernateUtil;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.md.controls.LabeledBox;
import jnekoimagesdb.ui.md.controls.NumericTextField;
import jnekoimagesdb.ui.md.controls.RegexpTextField;
import jnekoimagesdb.ui.md.dialogs.MessageBox;
import jnekoimagesdb.ui.md.dialogs.WaitBox;
import static org.fusesource.leveldbjni.JniDBFactory.factory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class StartDialogNewDBTab extends VBox {
    public final static String
            CSS_FILE = new File("./style/style-gmd-main-window.css").toURI().toString();
    
    private final EnumMap<XImg.PreviewType, DB>
            levelDB = new EnumMap(XImg.PreviewType.class);
    
    private final XImgCrypto cryptoEx = new XImgCrypto(() -> {
            return null;
        });
    
    private final XImgPreviewSizes
            psizes = new XImgPreviewSizes();
    
    private final StartDialogOpenTabActionListener
            outActListener;

    private final NumericTextField
            prevX = new NumericTextField("main_window_good_input", "main_window_badly_input"),
            prevY = new NumericTextField("main_window_good_input", "main_window_badly_input"),
            threads = new NumericTextField("main_window_good_input", "main_window_badly_input");
    
//    private final PathTextField
//            exchangePath = new PathTextField("main_window_good_input", "main_window_badly_input");
    
    private final RegexpTextField
            databaseName = new RegexpTextField("main_window_good_input", "main_window_badly_input", "^[a-zA-Z0-9_-]{1,32}$");
    
    private final Button 
            btnCreate = new Button("Создать БД");
    
    private String 
            dbName = null;
    
    private final Label 
            text = new Label();
    
    public StartDialogNewDBTab(StartDialogOpenTabActionListener al) {
        super();
        this.setAlignment(Pos.TOP_CENTER);
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_new_db_root_pane");
        outActListener = al;
        
        text.setAlignment(Pos.CENTER);
        text.setWrapText(true);
        text.getStyleClass().addAll("main_window_max_width", "main_window_messagebox_text");
        text.setText("Создание новой базы данных. Введите в поля ниже нужную информацию или оставьте значения по-умолчанию, после чего нажмите кнопку создания. "
                + "Если в каком-то из полей будет допущена ошибка, оно станет красным. Все параметры, кроме имени БД, можно будет впоследствии изменить в настройках программы.");
        
        databaseName.setText("default");
        databaseName.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_text_ntf");
        
        prevX.setMinMax(60, 501);
        prevX.setText("150");
        prevX.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_text_ntf");
        
        prevY.setMinMax(60, 501);
        prevY.setText("150");
        prevY.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_text_ntf");
        
        threads.setMinMax(0, Runtime.getRuntime().availableProcessors() * 2);
        threads.setText("" + Runtime.getRuntime().availableProcessors());
        threads.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_text_ntf");
        
//        exchangePath.setText("./");
//        exchangePath.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_text_ntf");
        
        btnCreate.getStyleClass().addAll("main_window_messagebox_button");
        btnCreate.setOnAction(c -> {
            if ( databaseName.isValid() && 
                    prevX.isValid() && 
                    prevY.isValid() && 
                    threads.isValid()/* && 
                    exchangePath.isValid()**/) {
                dbName = databaseName.getText().trim();
                createDBExec();
            } else 
                MessageBox.show("Одно из полей заполнено неверно!");
        });
        
        this.getChildren().addAll(
                text,
                new LabeledBox("Имя базы данных", databaseName),
                new LabeledBox("Высота превью", prevX),
                new LabeledBox("Ширина превью", prevY),
                new LabeledBox("Кол-во потоков", threads),
                //new LabeledBox("Путь к папке обмена браузера", exchangePath),
                new LabeledBox("", new VBox()),
                btnCreate
        );
    }

    private void createDBExec() {
        if (new File(dbName).exists()) {
            MessageBox.show("Невозможно создать базу, папка или файл с таким именем уже существуют.");
            return;
        }
        
        createDBX();
    }
    
    private void createDBX() {
        final Runnable
            dbCreationThread = () -> {
                if (createDB()) {
//                    final StringBuilder sb = new StringBuilder();
//                    sb
//                            .append("Preview_")
//                            .append(prevX.getLong())
//                            .append("x")
//                            .append(prevY.getLong())
//                            .append("_SQ");
                    psizes.addPreviewSize(prevX.getLong(), prevY.getLong(), true);
                    psizes.get(0).setPrimary(true);
                    
                    SettingsUtil.setLong("mainPreviewGenThreadsCount.value", threads.getLong());
                    SettingsUtil.setLong("previewFSCacheThreadsCount.value", threads.getLong());
                    
                    HibernateUtil.dispose();

                    Platform.runLater(() -> { 
                        WaitBox.hideMe();
                        outActListener.OnSelect(dbName); 
                    });
                } else {
                    Platform.runLater(() -> { 
                        WaitBox.hideMe();
                        MessageBox.show("Невозможно создать БД: диск переполнен или папка недоступна на запись.");
                    });
                }
            };
        new Thread(dbCreationThread).start();
        WaitBox.show("Подождите, идет создание БД. Это может занять продолжительное время...");
    }
    
    public void initIDB(XImg.PreviewType dbNameIDB) {
        final File levelDBFile = new File(dbName + File.separator + dbNameIDB.name());
        Options options = new Options();
        options.createIfMissing(true);   
        try {
            levelDB.put(dbNameIDB, factory.open(levelDBFile, options));
        } catch (IOException ex) {
            Logger.getLogger(XImg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean createDB() {
        if (!new File(dbName).mkdir()) return false;
        try {
            cryptoEx.init(dbName);
            initIDB(XImg.PreviewType.cache);
            initIDB(XImg.PreviewType.previews);
            final Set<XImg.PreviewType> s = levelDB.keySet();
            s.forEach((x) -> {
                 try {
                    levelDB.get(x).close();
                } catch (IOException ex) {}
            });
            
            HibernateUtil.hibernateInit(dbName, "jneko", cryptoEx.getPassword());
            SettingsUtil.init();
            psizes.refreshPreviewSizes();
            
            return true;
        } catch (Exception ex) {
            Logger.getLogger(StartDialogNewDBTab.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
