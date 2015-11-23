package imgfs;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import jnekoimagesdb.JNekoImageDB;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

public class ImgFSImages {
    public static int 
            previewHeight = 120,
            previewWidth = 120;
    
    public static boolean
            isSquaredFSPreview = false;
    
    private static ArrayList<ImgFS.PreviewGeneratorActionListener> 
            alFSPreview = null;
    
    @SuppressWarnings("ConvertToTryWithResources")
    public static boolean isImage(String path) {
        final String ext = FilenameUtils.getExtension(path);
        if (ext.length() < 2) return false;
        
        final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(ext);

        if (it.hasNext()) {
            final ImageReader r = it.next();
            try {
                ImageInputStream stream = new FileImageInputStream(new File(path));
                r.setInput(stream);
                int width = r.getWidth(r.getMinIndex());
                int height = r.getHeight(r.getMinIndex());
                r.dispose();
                stream.close();
                if ((width > 0) && (height > 0)) return true;
            } catch (IOException e) {
                return false;
            } finally { r.dispose(); }
        }
        return false;
    }
    
    public static void setPreviewCompleteListener(ArrayList<ImgFS.PreviewGeneratorActionListener> al) {
        alFSPreview = al;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public static byte[] getPreviewFS(String in_path) throws IOException {
        final BufferedImage image = intResizeImage(in_path, previewWidth, previewHeight, isSquaredFSPreview);
        if (image == null) 
            throw new IOException("getPreviewFS: cannot create preview for image ["+in_path+"]!");;
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        final byte retVal[] = baos.toByteArray();
        
        if (alFSPreview != null) {
            final Image img = new Image(new ByteArrayInputStream(retVal));
            final String path = in_path;
            for (ImgFS.PreviewGeneratorActionListener al : alFSPreview) {
                //Platform.runLater(() -> {
                    al.OnPreviewGenerateComplete(img, FileSystems.getDefault().getPath(path)); 
                //});
            }
        }
        
        baos.close();
        return retVal;
    }
    
    private static BufferedImage intResizeImage(String in_path, int sizeW, int sizeH, boolean crop) {
        final File img_file = new File(in_path);
        if (!img_file.canRead()) return null;
        try {
            final java.awt.Image image2 = Toolkit.getDefaultToolkit().createImage(in_path);
            final MediaTracker mediaTracker = new MediaTracker(new Container()); 
            mediaTracker.addImage(image2, 0); 
            mediaTracker.waitForAll();

            final int 
                    w_size = image2.getWidth(null),
                    h_size = image2.getHeight(null);
            
            final BufferedImage image = new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2d = image.createGraphics();
            g2d.setBackground(new Color(255, 255, 255)); 
            g2d.setColor(new Color(255, 255, 255));
            g2d.fillRect(0, 0, w_size, h_size);
            g2d.drawImage(image2, 0, 0, null);
            g2d.dispose();

            final BufferedImage out_img, crop_img;
            if (crop) {
                if (w_size > h_size) {
                    out_img = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_HEIGHT, sizeH, Scalr.OP_ANTIALIAS);
                    crop_img = Scalr.crop(out_img, sizeW, sizeW, Scalr.OP_ANTIALIAS);
                } else {
                    out_img = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, sizeW, Scalr.OP_ANTIALIAS);
                    crop_img = Scalr.crop(out_img, sizeW, sizeH, Scalr.OP_ANTIALIAS);
                }
                
                return crop_img;
            } else {
                out_img = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, sizeW, sizeH, Scalr.OP_ANTIALIAS);
                return out_img;
            }
        } catch (InterruptedException e) {
            _L(e.getMessage());
            return null;
        }
    }
    
    private static Map<String, BufferedImage> intResizeImage(String in_path, int sizeW, int sizeH) {
        final File img_file = new File(in_path);
        if (!img_file.canRead()) return null;
        try {
            final java.awt.Image image2 = Toolkit.getDefaultToolkit().createImage(in_path);
            final MediaTracker mediaTracker = new MediaTracker(new Container()); 
            mediaTracker.addImage(image2, 0); 
            mediaTracker.waitForAll();

            final int 
                    w_size = image2.getWidth(null),
                    h_size = image2.getHeight(null);
            
            final BufferedImage image = new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2d = image.createGraphics();
            g2d.setBackground(new Color(255, 255, 255)); 
            g2d.setColor(new Color(255, 255, 255));
            g2d.fillRect(0, 0, w_size, h_size);
            g2d.drawImage(image2, 0, 0, null);
            g2d.dispose();

            final Map<String, BufferedImage> 
                    imgs = new HashMap<>();
            
            final BufferedImage 
                    out_img, 
                    crop_img,
                    out_img2;
            
            if (w_size > h_size) {
                out_img2 = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_HEIGHT, sizeH, Scalr.OP_ANTIALIAS);
                crop_img = Scalr.crop(out_img2, sizeW, sizeW, Scalr.OP_ANTIALIAS);
            } else {
                out_img2 = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, sizeW, Scalr.OP_ANTIALIAS);
                crop_img = Scalr.crop(out_img2, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            }
            
            out_img = Scalr.resize(out_img2, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            
            imgs.put("squareImage", crop_img);
            imgs.put("nonsquareImage", out_img);
            
            return imgs;
        } catch (InterruptedException e) {
            _L(e.getMessage());
            return null;
        }
    }
    
    private static void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s); 
    }
}
