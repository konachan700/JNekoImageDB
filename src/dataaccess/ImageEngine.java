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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
            SQL;// = new SQLite();
    
    private final ImageEngineSQLWrappers
            wrappers;
    
    private final SQLiteFS
            ImagesFS,
            ThumbsFS;
    
    private final Crypto
            CRYPT;
    
//    private byte[]
//            lastMD5 = null;
    
//    private final static ByteArrayOutputStream 
//            baosForResizeNS = new ByteArrayOutputStream();
    
    public final static int
            SMALL_PREVIEW_SIZE      = 128,
//            MEDIUM_PREVIEW_SIZE     = 256,
//            LARGE_PREVIEW_SIZE_W    = 1024,
//            LARGE_PREVIEW_SIZE_H    = 768,
            
            PREVIEW_TYPE_SMALL              = 1,
//            PREVIEW_TYPE_MEDIUM             = 2,
            PREVIEW_TYPE_SMALL_NONSQUARED   = 3,
            
//            IMAGE_MAX_HEIGHT = 4200,
//            IMAGE_MAX_WIDTH = 4200,
            IMAGE_MAX_SIZE = 96000000;
    
    public ImageEngine(Crypto k, SQLite sql) {
        CRYPT       = k;
        SQL         = sql;
        
        ImagesFS    = new SQLiteFS(CRYPT, "images", SQL);
        ThumbsFS    = new SQLiteFS(CRYPT, "preview", SQL);

        SQL.ExecuteSQL("CREATE TABLE if not exists 'previews_list'(oid int not null primary key, idid int, pdid int, imgtype int);");
        SQL.ExecuteSQL("CREATE TABLE if not exists 'images_albums'(oid int not null primary key, imgoid int, alboid int);");
        
        wrappers = new ImageEngineSQLWrappers(SQL);
    }
    
    public SQLite getImgSQL() {
        return ImagesFS.GetSQL();
    }
    
    public ImageEngineSQLWrappers getWr() {
        return wrappers;
    }
    
    public DBImage getImages(long iid) {
        return new DBImage(ThumbsFS, SQL, iid);
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
        if (!isImage(path)) return -2;
        try {
            if (SQL.getConnection().isClosed()) return -3;
        } catch (SQLException ex) {
            return -4;
        }

        final long IID = ImagesFS.PushFileMT(path);
        if (IID > 0) {
            final long small_p_id = ThumbsFS.PushFileMT(ResizeImageSquare(path, SMALL_PREVIEW_SIZE));
            if (small_p_id <= 0) return -6; else __addImageAndPreviewAssoc(IID, small_p_id, PREVIEW_TYPE_SMALL);

            final long small_pns_id = ThumbsFS.PushFileMT(ResizeImage(path, SMALL_PREVIEW_SIZE, SMALL_PREVIEW_SIZE));
            if (small_pns_id <= 0) return -8; else __addImageAndPreviewAssoc(IID, small_pns_id, PREVIEW_TYPE_SMALL_NONSQUARED);

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
//            baosForResizeNS.reset();
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
//            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            baosForResizeNS.reset();
            final ByteArrayOutputStream baosForResizeNS = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baosForResizeNS);
            return baosForResizeNS.toByteArray();
        } catch (IOException e) {
             _L("ResizeImage IO error"); 
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

            final BufferedImage out_img;
            if (crop) {
                final BufferedImage temp_img1;           
                if (w_size > h_size) {
                    final int middle = (w_size / 2) - (h_size / 2);
                    temp_img1 = image.getSubimage(middle, 0, h_size, h_size);
                } else {
                    final int middle = (h_size / 2) - (w_size / 2);
                    temp_img1 = image.getSubimage(0, middle, w_size, w_size);
                }

                out_img = Scalr.resize(temp_img1, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            } else {
                out_img = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, sizeW, sizeH, Scalr.OP_ANTIALIAS);
            }

            return out_img;
        } catch (InterruptedException e) {
            return null;
        }
    }
    
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
                if ((width * height * 8) < IMAGE_MAX_SIZE) return true; else _L("WARNING: Image ["+path+"] too large! Height: "+height+"px; width: "+width+"px;");
            } catch (IOException e) {
                _L(e.getMessage());
            } finally { r.dispose(); }
        }
        
        return false;
    }
    
    private synchronized int __addImageAndPreviewAssoc(long imgID, long previewID, int type) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO 'previews_list' VALUES(?, ?, ?, ?);");
            final long tmr = new Date().getTime();
            ps.setLong(1, tmr);
            ps.setLong(2, imgID);
            ps.setLong(3, previewID);
            ps.setLong(4, type);
            ps.execute();
            return 0;
        } catch (SQLException ex) { _L(ex.getMessage()); }
        return -1;
    }

    private static void _L(String s) {
        //System.out.println(s);
        JNekoImageDB.L(s);
    }
}
