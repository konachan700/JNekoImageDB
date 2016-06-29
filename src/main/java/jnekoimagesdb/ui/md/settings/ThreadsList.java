package jnekoimagesdb.ui.md.settings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import jnekoimagesdb.core.threads.UThreadStatusListener;

public class ThreadsList extends ScrollPane implements UThreadStatusListener {
    public final static String
            CSS_FILE = new File("./style/style-gmd-albums.css").toURI().toString();
    
    private static ThreadsList 
            threads = null;
    
    private long
            taskCounter = 0;
    
    private final Map<String, ThreadsListElement>
            elements = new HashMap<>();
    
    private final VBox 
            rootContainer = new VBox();
    
    public ThreadsList() {
        super();
        
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setFitToHeight(false);
        this.setFitToWidth(true);
        this.getStylesheets().add(CSS_FILE);
        this.getStyleClass().addAll("albums_max_width", "albums_max_height", "albums_element_textarera");
        
        rootContainer.setAlignment(Pos.TOP_CENTER);
        rootContainer.getStyleClass().addAll("albums_max_width", "albums_x_root_pane", "albums_h_space");
        
        this.setContent(rootContainer);
    }

    public static void create() {
        if (threads == null) threads = new ThreadsList();
    }
    
    public static ThreadsList get() {
        if (threads == null) threads = new ThreadsList();
        return threads;
    }

    @Override
    public void OnThreadCreated(String id) {
        if (elements.containsKey(id)) return;
        final ThreadsListElement tle = new ThreadsListElement();
        tle.SetText("Поток [" + id + "] создан.");
        elements.put(id, tle);
        rootContainer.getChildren().add(tle);
    }

    @Override
    public void OnThreadPaused(String id) {
        if (!elements.containsKey(id)) return;
        elements.get(id).SetText("Поток [" + id + "] бездействует.");
    }

    @Override
    public void OnThreadStarted(String id) {
        if (!elements.containsKey(id)) return;
        elements.get(id).SetText("Поток [" + id + "] запущен.");
    }

    @Override
    public void OnThreadBroken(String id) {
        if (!elements.containsKey(id)) return;
        elements.get(id).SetText("Поток [" + id + "] завершен из-за ошибки.");
    }

    @Override
    public void OnThreadError(String id) {
        if (!elements.containsKey(id)) return;
        elements.get(id).SetText("Поток [" + id + "] получил исключение.");
    }

    @Override
    public void OnThreadStop(String id) {
        if (!elements.containsKey(id)) return;
        elements.get(id).SetText("Поток [" + id + "] остановлен.");
    }

    @Override
    public void OnThreadStatusReport(String id, long executeTime, long upTime) {
        if (!elements.containsKey(id)) return;
        taskCounter++;
        elements.get(id).SetText("Поток [" + id + "] работает "+((System.currentTimeMillis()-upTime)/1000)+" секунд.");
    }

    @Override
    public void OnThreadLETDetected(String id) {
        if (!elements.containsKey(id)) return;
        elements.get(id).SetText("Поток [" + id + "] завис, ждем перезапуска...");
    }
}
