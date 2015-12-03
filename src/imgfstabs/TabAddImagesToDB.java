package imgfstabs;

import dialogs.DialogYesNoBox;
import imgfs.ImgFSCrypto;
import imgfsgui.InfiniteFileList;
import imgfsgui.ToolsPanelBottom;
import imgfsgui.ToolsPanelTop;
import java.io.File;
import java.nio.file.Files;
import javafx.scene.image.Image;

public class TabAddImagesToDB {
    private final int
            BTN_LELEL_UP    = 1,
            BTN_NAVTO       = 2,
            BTN_NAVTO_ROOT  = 3,
            BTN_SELALL      = 4,
            BTN_SELNONE     = 5,
            BTN_DEL         = 6, 
            BTN_ADD         = 7;
    
    private final Image 
            IMG24_LEVEL_UP          = new Image(new File("./icons/lvlup24.png").toURI().toString()),
            IMG24_NAVIGATE_TO       = new Image(new File("./icons/navto24.png").toURI().toString()),
            IMG24_TO_ROOT           = new Image(new File("./icons/root24.png").toURI().toString());
    
    private final Image 
            IMG64_SELECT_ALL        = new Image(new File("./icons/selectall.png").toURI().toString()),
            IMG64_SELECT_NONE       = new Image(new File("./icons/selectnone.png").toURI().toString()),
            IMG64_DELETE            = new Image(new File("./icons/del48.png").toURI().toString()),
            IMG64_ADD_TO_DB         = new Image(new File("./icons/adddef.png").toURI().toString());
    
    private final ToolsPanelBottom
            panelBottom;
    
    private final ToolsPanelTop 
            panelTop;
    
    private final InfiniteFileList
            fileList;
    
    private final ImgFSCrypto
            crypt;
    
    private final String 
            databaseName;
    
    private final DialogYesNoBox
            dialogYN = new DialogYesNoBox();
    
    public TabAddImagesToDB(ImgFSCrypto c, String dbname) {
        crypt           = c;
        databaseName    = dbname;
        fileList        = new InfiniteFileList(crypt, databaseName);
        
        panelBottom = new ToolsPanelBottom();
        panelBottom.setAL((index) -> {
            switch (index) {
                case BTN_NAVTO_ROOT:
                    final File[] rootList = File.listRoots();
                    if (rootList.length > 0) {
                        if (rootList[0].getAbsolutePath().contains(":")) {
                            fileList.setWindowsRootPath(rootList);
                            panelBottom.getTextField("path").setText("Мой компьютер");
                        } else {
                            fileList.setPath(new File("/"));
                            panelBottom.getTextField("path").setText("/");
                        }
                    }
                    
                    break;
                case BTN_LELEL_UP:
                    // очень, очень странно. Просто fileList.getParentPath() при первом запуске возвращает null, а вот эта дикая конструкция - нет. Хотя одно и тоже.
                    final File parentFile = new File(fileList.getPath().getAbsolutePath()).getParentFile();
                    if (parentFile != null) {
                        fileList.setPath(parentFile);
                        panelBottom.getTextField("path").setText(parentFile.getAbsolutePath());
                    }

                    break;
                case BTN_NAVTO:
                    final String p = panelBottom.getTextField("path").getText();
                    final File navFile = new File(p);
                    if (navFile.exists() && navFile.isDirectory() && navFile.canRead()) {
                        fileList.setPath(navFile);
                    }
                    
                    break;
            }
        });
        panelBottom.addButton(BTN_NAVTO_ROOT, IMG24_TO_ROOT);
        panelBottom.addButton(BTN_LELEL_UP, IMG24_LEVEL_UP);
        panelBottom.addFixedSeparator();
        panelBottom.addTextField("path");
        panelBottom.addButton(BTN_NAVTO, IMG24_NAVIGATE_TO);

        fileList.init();
        fileList.setPath(new File(""));
        fileList.setActionListener((path) -> {
            if (Files.isDirectory(path)) {
                if (Files.isReadable(path)) {
                    fileList.setPath(path.toFile());
                    panelBottom.getTextField("path").setText(path.toString());
                }
            } else {
                if (Files.isReadable(path)) {
                    //todo: view image in new window
                }
            }
        });
        panelBottom.getTextField("path").setText(fileList.getPath().getAbsolutePath());
                
        panelTop = new ToolsPanelTop((index) -> {
            switch (index) {
                case BTN_SELALL:
                    if (fileList.getElementCount() > 4300) {
                        if (dialogYN.showYesNo("Опepация может занять некторое время. Продолжить?") == DialogYesNoBox.SELECT_YES) fileList.selectAll();
                    } else {
                        fileList.selectAll();
                    }   
                    break;
                case BTN_SELNONE:
                    fileList.clearSelection();
                    break;
                case BTN_DEL:
                    final int result = dialogYN.showYesNo("Точно удалить выбранные файлы с диска?");
                    break;
                case BTN_ADD:
                    
                    break;
            }
        });
        
        panelTop.addButton(IMG64_SELECT_ALL, BTN_SELALL);
        panelTop.addButton(IMG64_SELECT_NONE, BTN_SELNONE);
        panelTop.addFixedSeparator();
        panelTop.addButton(IMG64_DELETE, BTN_DEL);
        panelTop.addSeparator();
        panelTop.addButton(IMG64_ADD_TO_DB, BTN_ADD);
    }
    
    public ToolsPanelBottom getBottomPanel() {
        return panelBottom;
    }
    
    public ToolsPanelTop getTopPanel() {
        return panelTop;
    }
    
    public InfiniteFileList getList() {
        return fileList;
    }
    
    public void dispose() {
        fileList.dispose();
    }
}
