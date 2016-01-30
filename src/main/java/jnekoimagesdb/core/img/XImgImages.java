package jnekoimagesdb.core.img;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import jnekoimagesdb.ui.JNekoImageDB;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

public class XImgImages {
    private volatile int 
            previewHeight = 120,
            previewWidth = 120;
    
    private volatile Scalr.Method
            quality = Scalr.Method.SPEED;
    
    private boolean
            isSquaredFSPreview = false;
    
    public XImgImages() { }
    
    public void setPreviewSize(int w, int h, boolean s) {
        previewWidth = w;
        previewHeight = h;
        isSquaredFSPreview = s;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public boolean isImage(String path) {
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
    
    @SuppressWarnings("ConvertToTryWithResources")
    public byte[] getPreviewFS(String in_path) throws IOException {
        final BufferedImage image = intResizeImage(in_path, previewWidth, previewHeight, isSquaredFSPreview);
        if (image == null) 
            throw new IOException("getPreviewFS: cannot create preview for image ["+in_path+"]!");;
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        final byte retVal[] = baos.toByteArray();
        
        baos.close();
        return retVal;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public byte[] getPreviewFS(java.awt.Image inImg) throws IOException {
        final BufferedImage image = intResizeImage(inImg, previewWidth, previewHeight, isSquaredFSPreview);
        if (image == null) 
            throw new IOException("getPreviewFS: cannot create preview for image [RAW_BITMAP]!");;
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        final byte retVal[] = baos.toByteArray();
        
        baos.close();
        return retVal;
    }
    
    private BufferedImage intResizeImage(java.awt.Image inImg, int sizeW, int sizeH, boolean crop) {
        final int 
                w_size = inImg.getWidth(null),
                h_size = inImg.getHeight(null);

        final BufferedImage image = new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = image.createGraphics();
        g2d.setBackground(new Color(255, 255, 255)); 
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillRect(0, 0, w_size, h_size);
        g2d.drawImage(inImg, 0, 0, null);
        g2d.dispose();

        final BufferedImage out_img, crop_img;
        if (crop) {
            if (w_size > h_size) {
                final double 
                        in_k  = ((double) w_size) / ((double) h_size),
                        out_k = ((double) sizeW) / ((double) sizeH);

                out_img = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_HEIGHT, ((in_k < out_k) ? ((int)(sizeH * out_k)) : (sizeH)), Scalr.OP_ANTIALIAS);
                final int 
                        out_x = (out_img.getWidth() - sizeW) / 2,
                        out_y = (out_img.getHeight() - sizeH) / 2;

                crop_img = Scalr.crop(out_img, out_x, out_y, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            } else {
                final double 
                        in_k  = ((double) h_size) / ((double) w_size),
                        out_k = ((double) sizeH) / ((double) sizeW);

                out_img = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_WIDTH, ((in_k < out_k) ? ((int)(sizeW * out_k)) : (sizeW)), Scalr.OP_ANTIALIAS);
                final int 
                        out_x = (out_img.getWidth() - sizeW) / 2,
                        out_y = (out_img.getHeight() - sizeH) / 2;

                crop_img = Scalr.crop(out_img, out_x, out_y, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            }

            return crop_img;
        } else {
            out_img = Scalr.resize(image, quality, Scalr.Mode.AUTOMATIC, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            return out_img;
        }
    }
    
    private BufferedImage intResizeImage(String in_path, int sizeW, int sizeH, boolean crop) {
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
                    final double 
                            in_k  = ((double) w_size) / ((double) h_size),
                            out_k = ((double) sizeW) / ((double) sizeH);
                    
                    out_img = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_HEIGHT, ((in_k < out_k) ? ((int)(sizeH * out_k)) : (sizeH)), Scalr.OP_ANTIALIAS);
                    final int 
                            out_x = (out_img.getWidth() - sizeW) / 2,
                            out_y = (out_img.getHeight() - sizeH) / 2;

                    crop_img = Scalr.crop(out_img, out_x, out_y, sizeW, sizeH, Scalr.OP_ANTIALIAS);
                } else {
                    final double 
                            in_k  = ((double) h_size) / ((double) w_size),
                            out_k = ((double) sizeH) / ((double) sizeW);
                    
                    out_img = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_WIDTH, ((in_k < out_k) ? ((int)(sizeW * out_k)) : (sizeW)), Scalr.OP_ANTIALIAS);
                    final int 
                            out_x = (out_img.getWidth() - sizeW) / 2,
                            out_y = (out_img.getHeight() - sizeH) / 2;
                    
                    crop_img = Scalr.crop(out_img, out_x, out_y, sizeW, sizeH, Scalr.OP_ANTIALIAS);
                }
                
                return crop_img;
            } else {
                out_img = Scalr.resize(image,quality, Scalr.Mode.AUTOMATIC, sizeW, sizeH, Scalr.OP_ANTIALIAS);
                return out_img;
            }
        } catch (InterruptedException e) {
            _L(e.getMessage());
            return null;
        }
    }
    
    private Map<String, BufferedImage> intResizeImage(String in_path, int sizeW, int sizeH) {
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
                out_img2 = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_HEIGHT, sizeH, Scalr.OP_ANTIALIAS);
                crop_img = Scalr.crop(out_img2, sizeW, sizeW, Scalr.OP_ANTIALIAS);
            } else {
                out_img2 = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_WIDTH, sizeW, Scalr.OP_ANTIALIAS);
                crop_img = Scalr.crop(out_img2, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            }
            
            out_img = Scalr.resize(out_img2, quality, Scalr.Mode.AUTOMATIC, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            
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
