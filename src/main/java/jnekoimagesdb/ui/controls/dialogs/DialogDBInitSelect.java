package jnekoimagesdb.ui.controls.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.ToolsPanelTop;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.STabTextButton;
import static jnekoimagesdb.ui.controls.tabs.TabAlbumImageList.ALBTITLE_HSIZE;
import static jnekoimagesdb.ui.controls.tabs.TabAlbumImageList.HBUTTON_HSIZE;
import static jnekoimagesdb.ui.controls.tabs.TabAlbumImageList.HEADER_VSIZE;
import jnekoimagesdb.ui.controls.tabs.TabStartNewDB;
import jnekoimagesdb.ui.controls.tabs.TabStartSelectDB;

public class DialogDBInitSelect extends DialogWindow {
    public static enum DBSelectReturnCode {
        newDB, existDB, unknown
    }
    
    private final Image 
            IMG48_SELECT_YES = GUITools.loadIcon("selected-48"),
            IMG48_SELECT_NO  = GUITools.loadIcon("delete-48");
        
    private final TabStartSelectDB
            tabSelect = new TabStartSelectDB();
    
    private final TabStartNewDB
            tabNew = new TabStartNewDB((el) -> {
                retCode = DBSelectReturnCode.newDB;
                this.hide();
            });
    
    private String 
            dbNameX = "";
    
    private DBSelectReturnCode 
            retCode = DBSelectReturnCode.unknown;
    
    private final ToolsPanelTop
            okPanel = new ToolsPanelTop(ID -> {
                if (ID == 1) {
                    if (tabSelect.getDBName().trim().length() >= 1) {
                        dbNameX = tabSelect.getDBName().trim();
                        retCode = DBSelectReturnCode.existDB;
                        this.hide();
                    } else {
                        XImg.msgbox("БД не выбрана!");
                    }
                } else {
                    this.hide();
                    Platform.exit(); 
                }
            });
    
    private final HBox 
            header = new HBox(4);
     
    private final STabTextButton 
            openBtn, create;
    
    private final SFLabel
            albumName = new SFLabel("Создать/открыть БД", ALBTITLE_HSIZE, ALBTITLE_HSIZE, HEADER_VSIZE, HEADER_VSIZE, "albumName", "TabAlbumImageList");
    
    public DialogDBInitSelect() {
        super(770, 480, false); // todo: сделать запоминание размера
        
        GUITools.setStyle(header, "TabAlbumImageList", "header");
        header.setMaxSize(9999, HEADER_VSIZE);
        header.setPrefSize(9999, HEADER_VSIZE);
        header.setMinSize(HBUTTON_HSIZE * 3, HEADER_VSIZE);
        header.setAlignment(Pos.CENTER);
        
        openBtn = new STabTextButton("Открыть", 1, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            selectDB();
        }, "STabTextButton_green");
        
        create = new STabTextButton("Создать", 2, HBUTTON_HSIZE, HEADER_VSIZE, (code, id) -> {
            newDB();
        }, "STabTextButton_red");
        
        albumName.setAlignment(Pos.CENTER);
        header.getChildren().addAll(albumName, GUITools.getSeparator(), openBtn, create);
        
        okPanel.addButton(IMG48_SELECT_NO, 0);
        okPanel.addSeparator();
        okPanel.addButton(IMG48_SELECT_YES, 1);
                
        selectDB();
    }
    
    private void selectDB() {
        if (tabNew.isLocked()) return;
        
        retCode = DBSelectReturnCode.existDB;
        this.getMainContainer().getChildren().clear();
        albumName.setText("Открыть существующую БД");
        this.getMainContainer().getChildren().addAll(header, tabSelect, GUITools.getHSeparator(4), okPanel, GUITools.getHSeparator(4));
    }
    
    private void newDB() {
        if (tabNew.isLocked()) return;
        
        retCode = DBSelectReturnCode.newDB;
        this.getMainContainer().getChildren().clear();
        albumName.setText("Создать новую БД");
        this.getMainContainer().getChildren().addAll(header, tabNew, GUITools.getHSeparator(4), GUITools.getHSeparator(4));
    }
    
    public String getDBName() {
        if (retCode == DBSelectReturnCode.newDB) return tabNew.getDBName();
        if (retCode == DBSelectReturnCode.existDB) return tabSelect.getDBName();
        return null;
    }
    
    public DBSelectReturnCode getRetCode() {
        return retCode;
    }
}
