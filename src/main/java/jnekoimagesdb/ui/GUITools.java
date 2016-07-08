package jnekoimagesdb.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GUITools {
    private static final Map<String, Image>
            iconsCache = new HashMap<>();

    public static Image loadIcon(String name) {
        if (iconsCache.containsKey(name)) {
            return iconsCache.get(name);
        } else {
            final Image img = new Image(new File("./style/icons/"+name+".png").toURI().toString());
            iconsCache.put(name, img);
            return img;
        }
    }
    
    public static Image loadImage(String name) {
        return new Image(new File("./style/"+name+".jpg").toURI().toString());
    }
    
    public static ImageView loadIconIW(String name) {
        return new ImageView(loadIcon(name));
    }
}
