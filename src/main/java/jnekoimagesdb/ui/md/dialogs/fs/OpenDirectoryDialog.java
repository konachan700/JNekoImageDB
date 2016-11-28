package jnekoimagesdb.ui.md.dialogs.fs;

import java.nio.file.Path;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.md.dialogs.PanelDialog;
import jnekoimagesdb.ui.md.filelist.PagedFileList;
import static jnekoimagesdb.ui.md.filelist.PagedFileList.FIELD_PATH;
import jnekoimagesdb.ui.md.filelist.PagedFileListFullActionListener;
import jnekoimagesdb.ui.md.paginator.Paginator;
import jnekoimagesdb.ui.md.paginator.PaginatorActionListener;
import jnekoimagesdb.ui.md.toppanel.TopPanel;
import jnekoimagesdb.ui.md.toppanel.TopPanelButton;
import jnekoimagesdb.ui.md.toppanel.TopPanelMenuButton;

public class OpenDirectoryDialog extends PanelDialog implements PagedFileListFullActionListener, PaginatorActionListener {
    private final org.slf4j.Logger 
            logger = org.slf4j.LoggerFactory.getLogger(OpenDirectoryDialog.class);
    
    private static OpenDirectoryDialog
            odd = null;
    
    private final PagedFileList
            pfl = new PagedFileList(this, true);
    
    private boolean 
            retVal = false;
    
    private final TopPanel 
            panelTop = new TopPanel();
    
    private final TopPanelMenuButton 
            menuBtn = new TopPanelMenuButton();
    
    private final Paginator
            newPag = new Paginator(this); 
    
    private OpenDirectoryDialog() {
        super(800, 700, true);

        menuBtn.addMenuItemBold("menuitem_ok_open_icon", "Открыть", (c) -> {
            retVal = true;
            super.hide();
        });
        
        menuBtn.addMenuItemBold("menuitem_cancel_icon", "Отмена (закрыть окно)", (c) -> {
            retVal = false;
            super.hide();
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
        
        super.setMainContent(pfl);
        super.setPanel(panelTop);
        super.setPaginator(newPag);
        
        pfl.init(true); 
        pfl.navigateTo(SettingsUtil.getPath(FIELD_PATH).toString());
    }
    
    private Path getP() {
        return pfl.getPath();
    }
    
    private void resetRetVal() {
        retVal = false;
    }
    
    private boolean isResultPr() {
        return retVal;
    }

    @Override
    public void onDisable(boolean dis) {
        
    }

    @Override
    public void onPageCountChange(int pageCount) {
        newPag.setCurrentPageIndex(1);
        newPag.setPageCount((pageCount == 0) ? 1 : pageCount);
    }

    @Override
    public void onPageChange(int page) {
        newPag.setCurrentPageIndex(page+1);
    }

    @Override
    public void OnPageChange(int page, int pages) {
        logger.info("OnPageChange().page = "+page);
        pfl.pageSet(page-1);
    }
    
    private void disposeMe() {
        pfl.dispose();
    }
    
    public static void showDialog() {
        if (odd == null) odd = new OpenDirectoryDialog();
        odd.resetRetVal();
        odd.showAndWait();
    }   
    
    public static Path getPath() {
        return (odd == null) ? null : odd.getP();
    }
    
    public static boolean isResultPresent() {
        return (odd == null) ? false : odd.isResultPr();
    }
    
    public static void dispose() {
        if (odd != null) odd.disposeMe();
    }
}
