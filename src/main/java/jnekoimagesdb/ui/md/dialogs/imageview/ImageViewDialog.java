package jnekoimagesdb.ui.md.dialogs.imageview;

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.scene.image.Image;
import jnekoimagesdb.domain.DSImageIDListCache;
import jnekoimagesdb.ui.md.dialogs.FullDialog;
import jnekoimagesdb.ui.md.dialogs.MessageBox;
import jnekoimagesdb.ui.md.menu.Menu;
import jnekoimagesdb.ui.md.menu.MenuGroup;
import jnekoimagesdb.ui.md.menu.MenuItem;

public class ImageViewDialog extends FullDialog {
    private DSImageIDListCache.ImgType
            imageType = DSImageIDListCache.ImgType.All;

    private final ImageViewDialogTabImage
            imgTab = new ImageViewDialogTabImage();
    
    private final ImageViewDialogTabAlbums
            albumsTab = new ImageViewDialogTabAlbums();
    
    private DSImageIDListCache
            currentCache = null;
    
    private int 
            currentIID = 0;

    public ImageViewDialog(int xSize, int ySize) {
        super(xSize, ySize, false);
        this.setResizable(true);
        
        final Menu mn = new Menu(
                new MenuGroup(
                        "Меню изображения", "menu_group_container_red", "header_icon_images",
                        new MenuItem("Просмотр картинки", (c) -> {
                            this.setPanel(imgTab.getTopPanel());
                            this.setMainContent(imgTab);
                        }).defaultSelected(),
                        new MenuItem("Теги", (c) -> {
                            
                        }),
                        new MenuItem("Альбомы", (c) -> {
                            albumsTab.refresh(imgTab.getDSImage());
                            this.setPanel(albumsTab.getTopPanel());
                            this.setMainContent(albumsTab);
                        }),
                        new MenuItem("Свойства", (c) -> {
                            
                        })
                ),
                new MenuGroup(
                        "Дополнительно", "menu_group_container_red", "header_icon_images",
                        new MenuItem("Стегхайд", (c) -> {
                            
                        }),
                        new MenuItem("Отправить на e-mail", (c) -> {
                            
                        })
                )
        );
        this.setMenu(mn);
        

        
        
        
        
        this.setPanel(imgTab.getTopPanel());
        this.setMainContent(imgTab);
    }
    

    
    
    
    

    
    public final void show(int iID, DSImageIDListCache.ImgType type) {
        currentIID = iID;
        imageType = type;
        if (imageType != null) 
            currentCache = DSImageIDListCache.getStatic(imageType);
        else 
            currentCache = DSImageIDListCache.getAll();
        
        final Image img;
        if (currentCache != null) 
            img = currentCache.getImage(currentIID);
        else
            img = DSImageIDListCache.getAll().getImage(currentIID);
         
        if (img == null) {
            MessageBox.show("Не удалось открыть изображение, ошибка данных.");
            return;
        } else {
            imgTab.setIID(currentIID); 
            imgTab.setCache(currentCache);
            imgTab.setImage(img);
        }
        this.show();
    }
}
