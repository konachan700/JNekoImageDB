package jnekoimagesdb.ui.md.dialogs.fs;

import com.sun.javafx.scene.control.skin.PaginationSkin;
import javafx.scene.control.Pagination;
import jnekoimagesdb.ui.md.dialogs.PanelDialog;
import jnekoimagesdb.ui.md.filelist.PagedFileList;
import jnekoimagesdb.ui.md.filelist.PagedFileListFullActionListener;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;

public class OpenDirectoryDialog extends PanelDialog implements PagedFileListFullActionListener {
    
    private final PagedFileList
            pfl = new PagedFileList(this);
    
    private final TopPanel 
            panelTop = new TopPanel();
    
    private final TopPanelMenuButton 
            menuBtn = new TopPanelMenuButton();
    
    private final Pagination
            pag = new Pagination(); 
    
    public OpenDirectoryDialog() {
        super(800, 700, true);
        
        pag.setMaxSize(9999, 24);
        pag.setMinSize(128, 24);
        pag.setPrefSize(9999, 24);
        pag.getStyleClass().addAll("xxPagination");
        
        PaginationSkin ps = (PaginationSkin) pag.getSkin();
        ps.setPageInformationVisible(false);
        
        menuBtn.addMenuItemBold("menuitem_ok_open_icon", "Открыть", (c) -> {
            
        });
        
        menuBtn.addMenuItemBold("menuitem_cancel_icon", "Отмена (закрыть окно)", (c) -> {
            
        });
        
        
        panelTop.addNodes(
                new TopPanelButton("panel_icon_lvlup", "На один уровень вверх", c -> {
                    pfl.levelUp();
                }),
                new TopPanelButton("panel_icon_my_computer", "Мой компьютер (корень фс)", c -> {
                    pfl.navigateRoot();
                })
        );
        panelTop.addSeparator();
        panelTop.addNode(menuBtn);
    }

    @Override
    public void onDisable(boolean dis) {
        
    }

    @Override
    public void onPageCountChange(int pageCount) {
        pag.setCurrentPageIndex(0);
        pag.setPageCount((pageCount == 0) ? 1 : pageCount);
    }

    @Override
    public void onPageChange(int page) {
        pag.setCurrentPageIndex(page);
    }
}
