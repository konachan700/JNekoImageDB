package jnekoimagesdb.ui.controls.dialogs;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import jnekoimagesdb.core.img.XImgDatastore;
import jnekoimagesdb.domain.DSAlbum;
import jnekoimagesdb.domain.DSImage;
import jnekoimagesdb.ui.GUITools;


public class XAlbumsExport {
//    private boolean 
//            stateExit = false;
//    
//    private class XAlbumsExportThread implements Runnable {
//        private DSImage img;
//        
//        @Override
//        public void run() {
//            while(true) {
//                if (stateExit) return;
//                try {
//                    img = imgToExport.poll(100L, TimeUnit.MILLISECONDS);
//                    if (img != null) {
//                        threadBisy = true;
//                        Platform.runLater(() -> {
//                            outText.setText("Сохранение файла: ["+img.getImageFileName()+"]...\r\nФайлов осталось: "+imgToExport.size());
//                            closeButton.setDisable(false);
//                        });
//
//                        try {
//                            XImgDatastore.copyToExchangeFolderFromDB(exportPath, img);
//                        } catch (IOException ex) {
//                            Logger.getLogger(XAlbumsExport.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    } else {
//                        if (threadBisy) {
//                            Platform.runLater(() -> { 
//                                outText.setText("Готово."); 
//                                closeButton.setDisable(true);
//                            });
//                        }
//                        threadBisy = false;
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(XAlbumsExport.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//    }
//    
//    private final SFLabel
//            outText = new SFLabel("", 64, 9999, 64, 64, "label_darkgreen_small", "TypesListItem"),
//            outTitle = new SFLabel("", 64, 9999, 20, 20, "label_darkgreen", "TypesListItem");
//    
//    private final STabTextButton
//            closeButton = new STabTextButton("  Закрыть  ", ElementsIDCodes.buttonUnknown, 120, 32, (x, y) -> {
//                        this.hide();
//                    });
//    
//    private final LinkedBlockingDeque<DSImage>
//            imgToExport = new LinkedBlockingDeque<>();
//    
//    private Path
//            exportPath = null;
//    
//    private volatile boolean 
//            threadBisy = false;
//    
//    public XAlbumsExport() {
//        super();
//        
//        final SEVBox mtOptions = new SEVBox("svbox_sett_container_green");
//        mtOptions.setAlignment(Pos.CENTER_RIGHT);
//        
//        mtOptions.getChildren().addAll(
//                outTitle,
//                outText,
//                GUITools.getHNFSeparator(32),
//                new SElementPair(
//                        GUITools.getSeparator(), 
//                        4, 32, 32,
//                        GUITools.getSeparator(),
//                        closeButton
//                ).setAlign(Pos.CENTER_RIGHT)
//        );
//        
//        super.setClosingEnable(false);
//        super.create(null, null, mtOptions, COLOR_BLACK, 500, 233);
//    }
//    
//    public void init() {
//        final XAlbumsExportThread t = new XAlbumsExportThread();
//        final Thread th = new Thread(t);
//        th.start();
//    }
//    
//    public void dispose() {
//        stateExit = true;
//    }
//    
//    public void export(DSAlbum al, Path p) throws IOException {
//        final String dirName = al.getAlbumName().trim().replaceAll("\\W", "_");
//        // Тут скорее всего надо будет сделать ДА/НЕТ-диалог. А может и не надо =).
//        if ((dirName.length() < 1) || (dirName.length() > 128)) throw new IOException("Bad or null album name;");
//        
//        final Path newDir = FileSystems.getDefault().getPath(p.toAbsolutePath().toString(), dirName);
//        Files.createDirectory(newDir);
//        
//        exportPath = newDir;
//        imgToExport.addAll(al.getImages());
//        
//        super.showModal();
//    }
}
