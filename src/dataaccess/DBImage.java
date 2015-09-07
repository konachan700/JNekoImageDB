package dataaccess;

import java.awt.Container;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import jnekoimagesdb.JNekoImageDB;

public class DBImage {
    private final SQLiteFS SFS_preview;
    private final SQLite   SQL_meta;
    private final long     IID;
    
    public DBImage(SQLiteFS _SFS_preview, SQLite _SQL_meta, long _IID) {
        SFS_preview = _SFS_preview;
        SQL_meta = _SQL_meta;
        IID = _IID;
    }
    
    public int getSmallPrerviewToTempFile(String tmp_filename) {
        return getPrerviewToTempFile(tmp_filename, ImageEngine.PREVIEW_TYPE_SMALL);
    }
    
//    public int getMediumPrerviewToTempFile(String tmp_filename) {
//        return getPrerviewToTempFile(tmp_filename, ImageEngine.PREVIEW_TYPE_MEDIUM);
//    }
    
    public int getNSSmallPrerviewToTempFile(String tmp_filename) {
        return getPrerviewToTempFile(tmp_filename, ImageEngine.PREVIEW_TYPE_SMALL_NONSQUARED);
    }
    
    public int getPrerviewToTempFile(String tmp_filename, int xtype) {
        try {
            final BufferedImage b = getPrerview(xtype);
            if (b == null) return -1;
            ImageIO.write(b, "jpg", new FileOutputStream(tmp_filename));
            return 0;
        } catch (IOException e) {
            return -1;
        }
    }
    
    public BufferedImage getSmallPrerview() {
        return getPrerview(ImageEngine.PREVIEW_TYPE_SMALL);
    }
    
//    public BufferedImage getMediumPrerview() {
//        return getPrerview(ImageEngine.PREVIEW_TYPE_MEDIUM);
//    }
    
    public BufferedImage getNSSmallPrerview() {
        return getPrerview(ImageEngine.PREVIEW_TYPE_SMALL_NONSQUARED);
    }
    
    public synchronized BufferedImage getPrerview(int xtype) {
        try {
            PreparedStatement ps = SQL_meta.getConnection().prepareStatement("SELECT * FROM 'previews_list' WHERE idid=? AND imgtype=?;");
            ps.setLong(1, IID);
            ps.setInt(2, xtype);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                final long 
                        preview_id = rs.getLong("pdid");
                
                final byte[] img_t = SFS_preview.PopFile(preview_id);
                if (img_t == null) {
                    return null;
                }
                
                final ByteArrayInputStream img_b = new ByteArrayInputStream(img_t);
                final BufferedImage ret_img = ImageIO.read(img_b);
                
                final MediaTracker mediaTracker = new MediaTracker(new Container()); 
                mediaTracker.addImage(ret_img, 0); 
                mediaTracker.waitForAll();
                
                return ret_img;
            } else {
                return null;
            }
        } catch (SQLException | IOException | InterruptedException ex) {
            _L(ex.getMessage());
            return null;
        }
    }

    private void _L(String s) {
        //System.out.println(s);
        JNekoImageDB.L(s);
    }
}
