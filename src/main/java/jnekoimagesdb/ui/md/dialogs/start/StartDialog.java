package jnekoimagesdb.ui.md.dialogs.start;

import javafx.application.Platform;
import jnekoimagesdb.ui.md.dialogs.FullDialog;
import jnekoimagesdb.ui.md.menu.Menu;
import jnekoimagesdb.ui.md.menu.MenuGroup;
import jnekoimagesdb.ui.md.menu.MenuItem;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelInfobox;

public class StartDialog extends FullDialog {
    private final static StartDialog dialog = new StartDialog();

    private final TopPanel panelTop = new TopPanel();
    private final TopPanelInfobox 
            infoBox = new TopPanelInfobox("panel_icon_all_images");
    
    private final StartDialogOpenTab openDBTab;
    private final StartDialogNewDBTab newDBTab;
    private String dbName = "";
    
    public StartDialog() {
        super(800, 500, true);
//        ..this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.setOnCloseRequest(c -> {
            c.consume();
        });
        
        newDBTab = new StartDialogNewDBTab(c -> {
            dbName = c;
            this.hide();
        });
        
        openDBTab = new StartDialogOpenTab(c -> {
            dbName = c;
            this.hide();
        });
        
        final Menu mn = new Menu(
                new MenuGroup(
                        "Стартовое меню", "menu_group_container_red", "header_icon_images",
                        new MenuItem("Открыть БД", (c) -> {
                            openDB();
                        }).defaultSelected(),
                        new MenuItem("Создать БД", (c) -> {
                            newDB();
                        }),
                        new MenuItem("Выход", (c) -> {
                            Platform.exit();
                            this.hide();
                        })
                )
        );
        this.setMenu(mn);

        panelTop.addNode(infoBox);
        this.setPanel(panelTop);
        
        openDB();
    }
    
    private void newDB() {
        infoBox.setTitle("Создать БД");
        infoBox.setText("");
        this.setMainContent(newDBTab);
    }
    
    private void openDB() {
        infoBox.setTitle("Открыть БД");
        infoBox.setText("Для открытия нажмите на стрелку.");
        this.setMainContent(openDBTab);
    }
    
    public String getIDBName() {
        return dbName;
    }
        
    public static void showDialog() {
        dialog.showAndWait();
    }
    
    public static String getDBName() {
        return dialog.getIDBName();
    }
}
