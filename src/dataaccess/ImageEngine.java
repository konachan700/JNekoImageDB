package dataaccess;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import jnekoimagesdb.JNekoImageDB;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

public class ImageEngine {
    public static final long 
            ALBUM_ID_DELETED = 99999,
            ALBUM_ID_FAVORITES = 99998;
        
    private final SQLite 
            SQL;
    
    private final SQLiteFS
            ImagesFS,
            ThumbsFS;
    
    private final Crypto
            CRYPT;

    public final static int
            SMALL_PREVIEW_SIZE      = 128,
            
            PREVIEW_TYPE_SMALL              = 1,
            PREVIEW_TYPE_SMALL_NONSQUARED   = 3,

            IMAGE_MAX_SIZE = 128000000;
    
    public ImageEngine(Crypto k, SQLite sql) {
        CRYPT       = k;
        SQL         = sql;
        ImagesFS    = new SQLiteFS(CRYPT, "images", SQL);
        ThumbsFS    = new SQLiteFS(CRYPT, "preview", SQL);
    }
    
    public SQLite getImgSQL() {
        return ImagesFS.GetSQL();
    }

    public DBImage getImages(long iid) {
        return new DBImage(ThumbsFS, iid);
    }
    
    public SQLiteFS getThumbsFS() {
        return ThumbsFS;
    }
    
    public ArrayList<Long> getImages(String _sql) {
        return ImagesFS.getImages(_sql);
    }
    
    public ArrayList<Long> getImagesEx(String _sql) {
        ArrayList<Long> al = ImagesFS.getImages(_sql);
        
        
        
        return al;
    }
    
    public long getIOPS_R() {
        return ImagesFS.getIOPS_R() + ThumbsFS.getIOPS_R();
    }
    
    public long getIOPS_W() {
        return ImagesFS.getIOPS_W() + ThumbsFS.getIOPS_W();
    }
    
    public long getImgCount() {
        return ImagesFS.getCount();
    }
    
    public boolean isMD5(byte[] md5w) {
        return ImagesFS.isMD5Present(md5w);
    }

    public int DownloadImageToFS(long oid, String path) {
        return ImagesFS.PopFile(oid, path);
    }
    
    public long UploadImage(String path) {
        return UploadImage(path, null);
    }
    
    public long UploadImage(String path, byte[] hash) {
        if (!isImage(path)) return -2;
        try {
            if (SQL.getConnection().isClosed()) return -3;
        } catch (SQLException ex) {
            return -4;
        }

        final long IID = ImagesFS.PushFileMT(path, hash);
        if (IID > 0) {
            DBWrapper.writeImageMetadataToDB(path, IID);
            
            final Map<String, BufferedImage> imgs = ResizeImage(path, SMALL_PREVIEW_SIZE, SMALL_PREVIEW_SIZE, null);
            
            final long small_p_id = ThumbsFS.PushFileMT(BIToBytes(imgs.get("squareImage")), null);
            if (small_p_id <= 0) return -6; else DBWrapper.addImageAndPreviewAssoc(IID, small_p_id, PREVIEW_TYPE_SMALL);
            
            final long small_pns_id = ThumbsFS.PushFileMT(BIToBytes(imgs.get("nonsquareImage")), null);
            if (small_pns_id <= 0) return -8; else DBWrapper.addImageAndPreviewAssoc(IID, small_pns_id, PREVIEW_TYPE_SMALL_NONSQUARED);

            return IID;
        }
        
        return -1;
    }

    public int ResizeImageSquare(String in_path, String tmp_file, int size) {
        try {
            final BufferedImage bi = ResizeImage(in_path, size, size, true);
            if (bi == null) return -1;
            ImageIO.write(bi, "jpg", new FileOutputStream(tmp_file));
            return 0;
        } catch (IOException e) {
            _L("ResizeImageSquare IO error");
            return -1;
        }
    }
    
    public byte[] ResizeImageSquare(String in_path, int size) {
        try {
            final BufferedImage bi = ResizeImage(in_path, size, size, true);
            if (bi == null) return null;
            final ByteArrayOutputStream baosForResizeNS = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baosForResizeNS);
            return baosForResizeNS.toByteArray();
        } catch (IOException e) {
            _L("ResizeImageSquare IO error");
            return null;
        }
    }

    public static byte[] ResizeImage(String in_path, int sizeW, int sizeH) {
        try {
            final BufferedImage bi = ResizeImage(in_path, sizeW, sizeH, false);
            if (bi == null) return null;
            final ByteArrayOutputStream baosForResizeNS = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baosForResizeNS);
            return baosForResizeNS.toByteArray();
        } catch (IOException e) {
             _L("ResizeImage IO error"); 
            return null;
        }
    }
    
    public static byte[] BIToBytes(BufferedImage bi) {
        try {
            final ByteArrayOutputStream baosForResizeNS = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baosForResizeNS);
            return baosForResizeNS.toByteArray();
        } catch (IOException e) {
             _L("ResizeImage IO error"); 
            return null;
        }
    }

    public static Map<String, BufferedImage> ResizeImage(String in_path, int sizeW, int sizeH, Object dummy) {
        final File img_file = new File(in_path);
        if (!img_file.canRead()) return null;
        try {
            final Image image2 = Toolkit.getDefaultToolkit().createImage(in_path);
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
            return null;
        }
    }
    
    public static BufferedImage ResizeImage(String in_path, int sizeW, int sizeH, boolean crop) {
        final File img_file = new File(in_path);
        if (!img_file.canRead()) return null;
        try {
            final Image image2 = Toolkit.getDefaultToolkit().createImage(in_path);
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
            return null;
        }
    }
    
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
                if ((width * height * 8) < IMAGE_MAX_SIZE) 
                    return !DBWrapper.isMD5InMetadata(path);
                else 
                    _L("WARNING: Image ["+path+"] too large! Height: "+height+"px; width: "+width+"px;");
            } catch (IOException e) {
                _L(e.getMessage());
            } finally { r.dispose(); }
        }
        
        return false;
    }

    private static void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s);
    }
}
