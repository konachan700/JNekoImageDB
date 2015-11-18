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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class FileList extends InfinityList {
    public static final Image
            ITEM_SELECTED = new Image(new File("./icons/selected.png").toURI().toString()),
            ITEM_ERROR    = new Image(new File("./icons/broken2.png").toURI().toString()),
            ITEM_NOTHING  = new Image(new File("./icons/empty.png").toURI().toString()); 
    
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
        
        public final void setNullImage() {
            imageContainer.setImage(ITEM_NOTHING);
        }
        
        public final void setImage(Image img) {
            imageContainer.setImage(img);
        }
        
        public final void setName(String fname) {
            imageName.setText((fname.length() > 21) ? 
                (fname.substring(0, 8) + "..." + fname.substring(fname.length()-8 , fname.length())) : fname);
        }
        
        public final void setSelected(boolean s) {
            selectedIcon.setVisible(s);
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
            
            imageVBox.getStyleClass().add("FileListItem_imageVBox");
            imageVBox.setMinSize(itemSize, itemSize);
            imageVBox.setMaxSize(itemSize, itemSize);
            imageVBox.setPrefSize(itemSize, itemSize);
            imageVBox.getChildren().add(imageContainer);
            
            imageName.getStyleClass().add("FileListItem_imageName");
            imageName.setMaxSize(128, 16);
            imageName.setPrefSize(128, 16);
            imageName.setAlignment(Pos.CENTER);
            
            selectedIcon.getStyleClass().add("FileListItem_selectedIcon");
            selectedIcon.setVisible(false);
            
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
        
        public synchronized FileListItem get(int id) {
            if (!pool.containsKey(id)) {
                final FileListItem fsi = new FileListItem(actionListener);
                fsi.setName("filename #"+id);
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
        
        public synchronized void clearImages() {
            final Set<Integer> s = pool.keySet();
            s.stream().forEach((i) -> {
                pool.get(i).setNullImage();
            });
        }
        
        public synchronized void clearNames() {
            final Set<Integer> s = pool.keySet();
            s.stream().forEach((i) -> {
                pool.get(i).setName("");
            });
        }
        
        public synchronized int getSize() {
            return pool.size();
        }
    }
       
    private volatile int 
            itemSize = 128,
            itemCountOnOneLine = 0,
            itemCountOnOneColoumn = 0,
            itemTotalCount = 0,
            currentLine = 0;
    
    private File 
            currentFile = new File("G:\\#TEMP02\\Images\\Danbooru.p1\\danbooru_simple_bg");
    
    private ArrayList<Path>
            mainFileList = null;
    
    private final ArrayList<Path>
            selectedFileList = new ArrayList<>();
    
    private final FileListItemActionListener 
            actListener = new FileListItemActionListener() {
                @Override
                public void OnSelect(boolean isSelected, Path itemPath) {
                    if (isSelected) selectedFileList.add(itemPath); else selectedFileList.remove(itemPath);
                }

                @Override
                public void OnOpen(Path itemPath) {
                    
                }
            };
    
    private final FileListItemPool
            itemPool = new FileListItemPool(actListener);
    
    private volatile double
            winWidth = 0,
            winTH = 0,
            winVH = 0;
    
    private final InfinityListActionListener ilal = (int actionType, double windowWidth, double windowVisibleHeight, double windowTotalHeight) -> {
        switch (actionType) {
            case InfinityList.ACTION_TYPE_V_RESIZE:
                winTH = windowTotalHeight;
                winVH = windowVisibleHeight;
                int temporaryItemCountOnOneColoumn =  ((int)winVH) / itemSize;
                if (temporaryItemCountOnOneColoumn != itemCountOnOneColoumn) {
                    itemCountOnOneColoumn = temporaryItemCountOnOneColoumn;
                    itemTotalCount = itemCountOnOneColoumn * itemCountOnOneLine;
                    regenerateView();
                }
                break;
            case InfinityList.ACTION_TYPE_H_RESIZE:
                winWidth = windowWidth;
                final int temporaryItemCountOnOneLine = ((int)winWidth) / itemSize;
                if (temporaryItemCountOnOneLine != itemCountOnOneLine) {
                    itemCountOnOneLine = temporaryItemCountOnOneLine;
                    itemTotalCount = itemCountOnOneColoumn * itemCountOnOneLine;
                    regenerateView();
                }
                break;
            case InfinityList.ACTION_TYPE_SCROLL_DOWN:
                System.err.println("InfinityList.ACTION_TYPE_SCROLL_DOWN");
                currentLine++;
                regenerateView();
                break;
            case InfinityList.ACTION_TYPE_SCROLL_UP:
                System.err.println("InfinityList.ACTION_TYPE_SCROLL_UP");
                currentLine--;
                regenerateView();
                break;
        }
    };

    public FileList() {
        super();
        this.setAL(ilal); 
    }
    
    public final void setPath(File f) {
        try {
            final int count = getFilesCount(f);
            if (count <= 0) {
                setNull();
                return;
            }
            
            mainFileList = generateFileList(count, f);
            
            
        } catch (IOException ex) {
            setNull();
        }
    }
    
    private void regenerateView() {
        if (itemTotalCount <= 0) return;
        
        int counter = 0;
        this.clearAll();
        
        for (int i=0; i<(itemCountOnOneLine * 2); i++, counter++) {
            if (currentLine == 0) 
                if (i < itemCountOnOneLine) { this.addItem(itemPool.getNull(counter)); } else { this.addItem(itemPool.get(counter)); }
            else 
                this.addItem(itemPool.get(counter));
        }
        
        for (int i=0; i<itemTotalCount; i++, counter++) {
            this.addItem(itemPool.get(counter));
        }
        
        for (int i=0; i<(itemCountOnOneLine * 2); i++, counter++) {
            this.addItem(itemPool.get(counter));
        }
    }
    
    private void setNull() {
        
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
        final ArrayList<Path> al = new ArrayList<>(count);
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(f.getAbsolutePath()));
        for (Path p : stream) {
            al.add(p);
        }
        stream.close();   
        return al;
    }
    
    
    
    
    
    
    
}
