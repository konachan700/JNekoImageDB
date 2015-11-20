package imgfsgui;

import dataaccess.Lang;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class InfiniteFileList extends InfiniteListPane {
    public static final Image
            ITEM_SELECTED   = new Image(new File("./icons/selected.png").toURI().toString()),
            ITEM_ERROR      = new Image(new File("./icons/broken2.png").toURI().toString()),
            ITEM_NOTHING    = new Image(new File("./icons/empty.png").toURI().toString()),
            ICON_NOELEMENTS = new Image(new File("./icons/nondelete-48.png").toURI().toString()),
            ICON_CLOCK      = new Image(new File("./icons/clock48.png").toURI().toString()); 
    
    protected interface FileListItemActionListener {
        public void OnSelect(boolean isSelected, Path itemPath);
        public void OnOpen(Path itemPath);
    }
    
    protected class FileListItem extends Pane {
        private final ImageView 
            imageContainer = new ImageView(),
            selectedIcon = new ImageView(ITEM_SELECTED);
        
        private final VBox 
            imageVBox = new VBox(0);
                
        private final Label
            imageName = new Label();
        
        private Path
                currentPath = null;
        
        private final FileListItemActionListener
                actionListener;
        
        public final void setPath(Path p) {
            currentPath = p;
        }
        
        public final Path getPath() {
            return currentPath;
        }
        
        public final void setNullImage() {
            imageContainer.setImage(ITEM_NOTHING);
            imageContainer.setVisible(false);
            this.getChildren().clear();
        }
        
        public final void setImage(Image img) {
            imageContainer.setImage(img);
            imageContainer.setVisible(true);
            if (this.getChildren().isEmpty()) addAll();
        }

        public final void setName(String fname) {
            imageName.setText((fname.length() > 21) ? 
                (fname.substring(0, 8) + "..." + fname.substring(fname.length()-8 , fname.length())) : fname);
        }
        
        public final void setSelected(boolean s) {
            if (imageContainer.isVisible()) selectedIcon.setVisible(s);
        }
        
        public final boolean getSelected() {
            return selectedIcon.isVisible();
        }
        
        public FileListItem(FileListItemActionListener al) {
            super();
            
            actionListener = al;
            
            this.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
            this.getStyleClass().add("FileListItem");
            this.setMinSize(itemSize, itemSize);
            this.setMaxSize(itemSize, itemSize);
            this.setPrefSize(itemSize, itemSize);
            this.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 1) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        setSelected(!getSelected());
                        if (currentPath != null) actionListener.OnSelect(getSelected(), currentPath);
                    }
                } else {
                    if (currentPath != null) actionListener.OnOpen(currentPath);
                }
                event.consume();
            });

            imageContainer.getStyleClass().add("FileListItem_imageContainer");
            imageContainer.setFitHeight(itemSize);
            imageContainer.setFitWidth(itemSize);
            imageContainer.setPreserveRatio(true);
            imageContainer.setSmooth(true);
            imageContainer.setCache(false);
            imageContainer.setImage(ITEM_NOTHING);
                        
            imageVBox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
            imageVBox.getStyleClass().add("FileListItem_imageVBox");
            imageVBox.setMinSize(itemSize, itemSize);
            imageVBox.setMaxSize(itemSize, itemSize);
            imageVBox.setPrefSize(itemSize, itemSize);
            imageVBox.getChildren().add(imageContainer);
            
            imageName.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
            imageName.getStyleClass().add("FileListItem_imageName");
            imageName.setMaxSize(128, 16);
            imageName.setPrefSize(128, 16);
            imageName.setAlignment(Pos.CENTER);

            selectedIcon.getStyleClass().add("FileListItem_selectedIcon");
            selectedIcon.setVisible(false);
            
            addAll();
        }
        
        private void addAll() {
            this.getChildren().addAll(imageVBox, selectedIcon, imageName);
            imageVBox.relocate(0, 0);
            selectedIcon.relocate(10, 10);
            imageName.relocate(0, 100);
        }
    }
    
    protected class FileListItemPool {
        private final Map<Integer, FileListItem> 
                pool = new HashMap<>();
        
        private final FileListItemActionListener
                actionListener;
        
        public FileListItemPool(FileListItemActionListener al) { 
            actionListener = al;
        }
        
        public synchronized void shift(int shiftCount) {
            final Map<Integer, FileListItem> temporaryPool = new HashMap<>();
            final int poolSize = pool.size();
            if (shiftCount > 0) {
                for (int i=0; i<poolSize; i++) {
                    final int currVal = i - shiftCount;
                    if (currVal >= 0) temporaryPool.put(i, pool.get(currVal));
                }
            } else if (shiftCount < 0) {
                for (int i=0; i<poolSize; i++) {
                    final int currVal = i + Math.abs(shiftCount);
                    if (currVal < poolSize) temporaryPool.put(i, pool.get(currVal));
                }
            }
            pool.clear();
            pool.putAll(temporaryPool);
        }
        
        public synchronized FileListItem get(int id) {
            if (!pool.containsKey(id)) {
                final FileListItem fsi = new FileListItem(actionListener);
                pool.put(id, fsi);
            }
            return pool.get(id);
        }
        
        public synchronized FileListItem getNull(int id) {
            if (!pool.containsKey(id)) {
                final FileListItem fsi = new FileListItem(actionListener);
                pool.put(id, fsi);
            }
            pool.get(id).setName("");
            pool.get(id).setNullImage();     
            return pool.get(id);
        }

        public synchronized boolean elementExist(int id) {
            return pool.containsKey(id);
        }
        
        public synchronized int getSize() {
            return pool.size();
        }
    }
       
    private volatile int 
            itemSize                = 128,
            itemCountOnOneLine      = 0,
            itemCountOnOneColoumn   = 0,
            itemTotalCount          = 0,
            invisibleLines          = 2,
            waitCounter             = 0;
    
    private volatile boolean
            waitLock = false;
    
    private final InfiniteFileList
            THIS = this;
    
    private final HBox
            dummyInfoBox = new HBox(8),
            pleaseWaitBox = new HBox(8);
    
    private final Label 
            waitText = new Label();
    
    private File 
            currentFile = new File("G:\\#TEMP02\\Images\\Danbooru.p1\\danbooru_simple_bg"); //"G:\\#TEMP02\\Images\\Danbooru.p1\\danbooru_simple_bg");
    
    private ArrayList<Path>
            mainFileList = null;
    
    private final ArrayList<Path>
            selectedFileList = new ArrayList<>();
    
    private InfiniteFileListActionListener
            actionListenerIFL = null;
    
    private final FileListItemActionListener 
            actListener = new FileListItemActionListener() {
                @Override
                public void OnSelect(boolean isSelected, Path itemPath) {
                    if (isSelected) selectedFileList.add(itemPath); else selectedFileList.remove(itemPath);
                }

                @Override
                public void OnOpen(Path itemPath) {
                    if (actionListenerIFL != null) actionListenerIFL.OnLeftClick(itemPath);
                }
            };
    
    private final FileListItemPool
            itemPool = new FileListItemPool(actListener);
    
    private volatile double
            winWidth = 0,
            winVH = 0;
    
    private final InfiniteListPaneActionListener ilal = (int actionType, double windowWidth, double windowVisibleHeight, double windowTotalHeight) -> {
        switch (actionType) {
            case InfiniteListPane.ACTION_TYPE_V_RESIZE:
                winVH = windowVisibleHeight;
                int temporaryItemCountOnOneColoumn =  ((int)winVH) / itemSize;
                if (temporaryItemCountOnOneColoumn != itemCountOnOneColoumn) {
                    itemCountOnOneColoumn = temporaryItemCountOnOneColoumn;
                    itemTotalCount = itemCountOnOneColoumn * itemCountOnOneLine;
                }
                break;
            case InfiniteListPane.ACTION_TYPE_H_RESIZE:
                winWidth = windowWidth;
                final int temporaryItemCountOnOneLine = ((int)winWidth) / itemSize;
                if (temporaryItemCountOnOneLine != itemCountOnOneLine) {
                    itemCountOnOneLine = temporaryItemCountOnOneLine;
                    itemTotalCount = itemCountOnOneColoumn * itemCountOnOneLine;
                }
                break;
            case InfiniteListPane.ACTION_TYPE_SCROLL_DOWN:
                System.err.println("InfinityList.ACTION_TYPE_SCROLL_DOWN");
                break;
            case InfiniteListPane.ACTION_TYPE_SCROLL_UP:
                System.err.println("InfinityList.ACTION_TYPE_SCROLL_UP");
                break;
        }
        regenerateView(actionType);
    };

    public InfiniteFileList() {
        super();
        this.setRowSize(itemSize);
        this.setInvisibleItemsCount(invisibleLines);
        this.setAL(ilal); 
        
        dummyInfoBox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        dummyInfoBox.getStyleClass().add("FileListItem_dummyInfoBox");
        dummyInfoBox.setMinSize(48, 48);
        dummyInfoBox.setMaxSize(9999, 48);
        dummyInfoBox.setPrefSize(9999, 48);
        
        pleaseWaitBox.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        pleaseWaitBox.getStyleClass().add("FileListItem_dummyInfoBox");
        pleaseWaitBox.setMinSize(48, 48);
        pleaseWaitBox.setMaxSize(9999, 48);
        pleaseWaitBox.setPrefSize(9999, 48);
        
        waitText.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        waitText.getStyleClass().add("FileListItem_nullMessage");
        waitText.setMinSize(48, 48);
        waitText.setMaxSize(9999, 48);
        waitText.setPrefSize(9999, 48);
        waitText.setAlignment(Pos.CENTER_LEFT);
        
        final Label text = new Label("Нет элементов для отображения");
        text.getStylesheets().add(getClass().getResource(Lang.AppStyleCSS).toExternalForm());
        text.getStyleClass().add("FileListItem_nullMessage");
        text.setMinSize(48, 48);
        text.setMaxSize(9999, 48);
        text.setPrefSize(9999, 48);
        text.setAlignment(Pos.CENTER_LEFT);

        final ImageView icon = new ImageView(ICON_NOELEMENTS);
        icon.getStyleClass().add("FileListItem_selectedIcon");
        
        final ImageView iconClock = new ImageView(ICON_CLOCK);
        icon.getStyleClass().add("FileListItem_selectedIcon");

        dummyInfoBox.getChildren().addAll(icon, text);
        pleaseWaitBox.getChildren().addAll(iconClock, waitText);
        
        setPath(currentFile);
    }
    
    public final void setActionListener(InfiniteFileListActionListener al) {
        actionListenerIFL = al;
    }
    
    public final void setPath(File fl) {
        currentFile = fl;
        setWaitE();
        waitText.setText("Идет построение списка...");
        final Runnable fileListGenerator = () -> {
            try {
                final int count = getFilesCount(currentFile);
                if (count <= 0) {
                    Platform.runLater(() -> { 
                        setNull(); 
                        waitLock = false;
                    });
                    return;
                }

                mainFileList = generateFileList(count, currentFile);
                Platform.runLater(() -> { 
                    waitLock = false;
                    regenerateView(-1); 
                });
            } catch (IOException ex) {
                Platform.runLater(() -> { 
                    setNull(); 
                    waitLock = false;
                });
            }
        };

        final Thread t = new Thread(fileListGenerator);
        t.start();
    }
    
    private void regenerateView(int actionType) {
        if (waitLock) return;
        
        if ((itemTotalCount <= 0) || (mainFileList == null) || (mainFileList.isEmpty())) {
            setNull();
            return;
        }
        
        this.clearAll();
        //this.setDisableScroll(false);
        
        int 
                total = (invisibleLines * itemCountOnOneLine * 2) + (itemCountOnOneColoumn * itemCountOnOneLine),
                maxLines = mainFileList.size() / itemCountOnOneLine;
        
        long 
                firstItem = this.getCurrentRow() * itemCountOnOneLine;

        switch (actionType) {
            case InfiniteListPane.ACTION_TYPE_SCROLL_DOWN:
                itemPool.shift(itemCountOnOneLine);
                for (int a=0; a<total; a++) {
                    if (itemPool.elementExist(a)) {
                        this.addItem(itemPool.get(a));
                    } else {
                        if (THIS.isMinimum()) {
                            this.addItem(itemPool.getNull(a));
                        } else {
                            int currentIndex = a + ((int)firstItem - itemCountOnOneLine);
                            loadImage(mainFileList.get(currentIndex), itemPool.get(a), a, currentIndex);
                            if (selectedFileList.contains(itemPool.get(a).getPath())) itemPool.get(a).setSelected(true);
                            this.addItem(itemPool.get(a));
                        }
                    }
                }
                break;
            case InfiniteListPane.ACTION_TYPE_SCROLL_UP:
                itemPool.shift(-1 * itemCountOnOneLine);
                for (int a=0; a<total; a++) {
                    if (itemPool.elementExist(a)) {
                        this.addItem(itemPool.get(a));
                    } else {
                        int currentIndex = a + ((int)firstItem - itemCountOnOneLine);
                        if (currentIndex >= mainFileList.size()) 
                            this.addItem(itemPool.getNull(a)); 
                        else {
                            loadImage(mainFileList.get(currentIndex), itemPool.get(a), a, currentIndex);
                            if (selectedFileList.contains(itemPool.get(a).getPath())) itemPool.get(a).setSelected(true);
                            this.addItem(itemPool.get(a));
                        }
                    }
                }
                break;
            default:
                regenerateFull(total, firstItem);
                break;
        }
        if ((maxLines - itemCountOnOneColoumn) > 0) this.setScrollMax(maxLines - itemCountOnOneColoumn);
    }
    
    private void regenerateFull(int total, long firstItem) {
        if (waitLock) return;
        this.setDisableScroll(false);
        System.err.println("REGEN FULL");
        int counter = 0;
        for (int i=0; i<total; i++) {
            int currentIndex = counter + (int)firstItem;
            if ((currentIndex >= mainFileList.size()) || ((THIS.isMinimum()) && ((i < itemCountOnOneLine)))) {
                itemPool.getNull(i);
            } else {
                loadImage(mainFileList.get(currentIndex), itemPool.get(i), i, currentIndex);
                if (selectedFileList.contains(itemPool.get(i).getPath())) itemPool.get(i).setSelected(true);
                counter++;
            }

            this.addItem(itemPool.get(i));
        }
    }
    
    private void loadImage(Path p, FileListItem f, int localItemIndex, int pathIndex) {
        f.setName(p.getFileName().toString());
        f.setPath(p);
        if (actionListenerIFL != null) {
            final Image img = actionListenerIFL.OnImageNedded(p);
            if (img != null)
                f.setImage(img);
            else
                f.setImage(ITEM_ERROR);
        } else 
            f.setImage(ITEM_ERROR); 
    }
    
    private void setNull() {
        this.clearAll();
        this.setDisableScroll(true);
        this.addItem(dummyInfoBox);
    }
    
    private void setWaitE() {
        waitLock = true;
        this.clearAll();
        this.setDisableScroll(true);
        this.addItem(pleaseWaitBox);
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    private int getFilesCount(File f) throws IOException {
        final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(f.getAbsolutePath()));
        int counter = 0;
        for (Path p : stream) counter++;
        stream.close();
        return counter;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    private ArrayList<Path> generateFileList(int count, File f) throws IOException {
        // На первый взгляд странное решение, ведь можно использовать DirectoryStream.Filter. Но нет, тут проход по списку с фильтрами один, а в случае DirectoryStream.Filter их будет два.
        // Памяти жрет намного больше, безусловно, но это сильно лучше, чем сканирование директории в 100к файлов за время 20-30 сек.
        long tmr = System.currentTimeMillis();
        final ArrayList<Path> 
                alt = new ArrayList<>(count);
        
        DirectoryStream<Path> stream2 = Files.newDirectoryStream(Paths.get(f.getAbsolutePath()));
        for (Path p : stream2) alt.add(p); 
        stream2.close(); 
        
        System.err.println("get dir list: " + (System.currentTimeMillis() - tmr));
        tmr = System.currentTimeMillis();

        final ArrayList<Path> al1 = new ArrayList<>(count);
        final ArrayList<Path> al2 = new ArrayList<>(count);
        alt.stream().map((a) -> {
            if (Files.isDirectory(a)) al1.add(a);
            waitCounter = setWaitStr(waitCounter, a);
            return a;
        }).filter((a) -> (Files.isRegularFile(a))).forEach((a) -> {
            al2.add(a);
            waitCounter = setWaitStr(waitCounter, a);
        });
        
        final ArrayList<Path> 
                al = new ArrayList<>(count);
        al.addAll(al1);
        al.addAll(al2);
        
        System.err.println("sort file list: " + (System.currentTimeMillis() - tmr));
        return al;
    }  
    
    private int setWaitStr(int counter, Path s) {
        counter++;
        if (counter > 500) {
            final String fname = s.getFileName().toString();
            final String fn = ((fname.length() > 40) ? (fname.substring(0, 20) + "..." + fname.substring(fname.length()-17 , fname.length())) : fname);
            Platform.runLater(() -> { waitText.setText("Построение списка файлов ["+fn+"]"); });
            counter = 0;
        }
        return counter;
    }
}