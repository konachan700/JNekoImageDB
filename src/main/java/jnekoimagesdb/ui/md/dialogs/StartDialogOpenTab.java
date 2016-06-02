package jnekoimagesdb.ui.md.dialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import jnekoimagesdb.core.img.XImgCrypto;

public class StartDialogOpenTab extends ScrollPane {
    public final static String
            CSS_FILE = new File("./style/style-gmd-main-window.css").toURI().toString();
    
    private final Path 
                currentFolder = FileSystems.getDefault().getPath(".").toAbsolutePath();
    
    private final StartDialogOpenTabActionListener
            outActListener;
    
    private final VBox 
            elementsContainer = new VBox();
    
    public StartDialogOpenTab(StartDialogOpenTabActionListener al) {
        super();
        outActListener = al;
        
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setFitToHeight(false);
        this.setFitToWidth(true);
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("main_window_max_width", "main_window_max_height", "main_window_sp1");
        this.setContent(elementsContainer);
        
        elementsContainer.setAlignment(Pos.TOP_CENTER);
        elementsContainer.getStyleClass().addAll("main_window_max_width", "main_window_null_pane");
        
        try {
            Files.list(currentFolder).forEach(c -> {
                if (Files.isDirectory(c)) {
                    if (ifDBFolder(c)) 
                        elementsContainer.getChildren().addAll(
                                new StartDialogOpenTabElement(c.toFile().getName(), outActListener)
                        );
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(StartDialogOpenTab.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean ifDBFolder(Path p) {
        final Path 
                publicKey = FileSystems.getDefault().getPath(p.toString(), XImgCrypto.PUBLIC_KEY_NAME),
                keystore = FileSystems.getDefault().getPath(p.toString(), XImgCrypto.KEYSTORE_NAME);
        return Files.isReadable(publicKey) && Files.isReadable(keystore);
    }
}
