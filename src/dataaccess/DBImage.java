package dataaccess;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class DBImage {
    private final SQLiteFS SFS_preview;
    private final long     IID;
    
    public DBImage(SQLiteFS _SFS_preview, long _IID) {
        SFS_preview = _SFS_preview;
        IID = _IID;
    }
    
    public int getSmallPrerviewToTempFile(String tmp_filename) {
        return getPrerviewToTempFile(tmp_filename, ImageEngine.PREVIEW_TYPE_SMALL);
    }

    public int getNSSmallPrerviewToTempFile(String tmp_filename) {
        return getPrerviewToTempFile(tmp_filename, ImageEngine.PREVIEW_TYPE_SMALL_NONSQUARED);
    }
    
    public int getPrerviewToTempFile(String tmp_filename, int xtype) {
        try {
            final BufferedImage b = DBWrapper.getPrerview(xtype, IID, SFS_preview);
            if (b == null) return -1;
            ImageIO.write(b, "jpg", new FileOutputStream(tmp_filename));
            return 0;
        } catch (IOException e) {
            return -1;
        }
    }
    
    public BufferedImage getSmallPrerview() {
        return DBWrapper.getPrerview(ImageEngine.PREVIEW_TYPE_SMALL, IID, SFS_preview);
    }

    public BufferedImage getNSSmallPrerview() {
        return DBWrapper.getPrerview(ImageEngine.PREVIEW_TYPE_SMALL_NONSQUARED, IID, SFS_preview);
    }
}
