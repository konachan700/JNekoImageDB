package dataaccess;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javafx.scene.image.Image;

/*
    Основное назначение этого кеша - предзагрузка картинок при скролле вперед. Актуально только для очень слабых машин типа атомов-Z (у меня есть такой планшет).
    Предотвращает мерцание картинок, их "неровную" загрузку и прочие артефакты, возникающие при очень слабом проце.

    В дальнейшем этот велосипед надо переделать под нормальный, человеческий кеш
*/

public class ImageCache {
    private static final ArrayList<Image> 
            cache = new ArrayList<>();
    
    private static final ArrayList<Long> 
            cacheIODs = new ArrayList<>();
    
    private static int 
            cacheSize = 200;
    
    public static synchronized void PushImage(Image img, Long oid) {
        if (cache.size() > cacheSize) {
            cacheIODs.remove(0);
            cache.remove(0);
        }
        cache.add(img);
        cacheIODs.add(oid);
    }
    
    public static synchronized Image PopImage(ImageEngine IMG, DBImageX element) {
        if (cacheIODs.contains(element.prev_oid)) {
            int index = cacheIODs.indexOf(element.prev_oid);
            return cache.get(index);
        }
        
        byte[] buf = IMG.getThumbsFS().PopPrewievFile(element);
        if (buf != null) {
            final Image img = new Image(new ByteArrayInputStream(buf));
            PushImage(img, element.prev_oid);
            return img;
        } else {
            return null;
        }
    }
    
    public static synchronized void Preload(ImageEngine IMG, DBImageX element) {
        if (!cacheIODs.contains(element.prev_oid)) {
            byte[] buf = IMG.getThumbsFS().PopPrewievFile(element);
            if (buf != null) {
                final Image img = new Image(new ByteArrayInputStream(buf));
                PushImage(img, element.prev_oid);
            }  
        }
    }
    
    public static synchronized void setCacheSize(int size) {
        cacheSize = size;
        while (true) {
            if (cache.size() <= cacheSize) break;
            cacheIODs.remove(0);
            cache.remove(0);
        }
    }
}
