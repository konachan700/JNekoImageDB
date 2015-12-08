package imgfstabs;

import dialogs.DialogFSImageView;
import dialogs.DialogYesNoBox;
import imgfs.ImgFS;
import imgfs.ImgFSCrypto;
import imgfsgui.InfiniteFileList;
import imgfsgui.ToolsPanelBottom;
import imgfsgui.ToolsPanelTop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javafx.scene.image.Image;
import jnekoimagesdb.GUITools;

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
            IMG24_LEVEL_UP          = GUITools.loadIcon("lvlup-24"),
            IMG24_NAVIGATE_TO       = GUITools.loadIcon("navto-24"),
            IMG24_TO_ROOT           = GUITools.loadIcon("root-24"); 
    
    private final Image 
            IMG64_SELECT_ALL        = GUITools.loadIcon("selectall-48"), 
            IMG64_SELECT_NONE       = GUITools.loadIcon("selectnone-48"), 
            IMG64_DELETE            = GUITools.loadIcon("delete-48"), 
            IMG64_ADD_TO_DB         = GUITools.loadIcon("add-to-db-48");
    
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
    
    private final DialogFSImageView
            imageViewer = new DialogFSImageView();
    
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
                    final ArrayList<Path> alp = fileList.getMainList();
                    imageViewer.setFiles(alp);
                    imageViewer.setFileIndex(path);
                    imageViewer.show();
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
                    ImgFS.progressShow();
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
