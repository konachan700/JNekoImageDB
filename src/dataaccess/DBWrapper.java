package dataaccess;

import albums.AlbumsCategory;
import static dataaccess.SQLite.QUOTE;
import java.awt.Container;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import jnekoimagesdb.JNekoImageDB;
import menulist.MenuGroupItem;
import org.apache.commons.io.FilenameUtils;
/*
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    TODO: ПРИДЕЛАТЬ СЧЕТЧИК ЗАПРОСОВ К БД!
*/

public class DBWrapper {
    private static volatile Crypto          xCrypto     = null;
    private static volatile SQLite          SQL         = null;
    private static volatile ImageEngine     IM          = null;
    private static volatile MenuGroupItem   MGI         = null;
    
    public static void setCrypto(Crypto c) {
        xCrypto = c;
    }
    
    public static void setSQLite(SQLite s) {
        SQL = s;
    }
    
    public static void setImageEngine(ImageEngine i) {
        IM = i;
    }
    
    public static void setMenuGroupItem2(MenuGroupItem m) {
        MGI = m;
    }
    
    
    
    
    public static BufferedImage getPrerview(DBImageX dbe) {
        try {
            final byte[] img_t = IM.getThumbsFS().PopFile(dbe.prev_oid);
            if (img_t == null) {
                return null;
            }

            final ByteArrayInputStream img_b = new ByteArrayInputStream(img_t);
            final BufferedImage ret_img = ImageIO.read(img_b);

            final MediaTracker mediaTracker = new MediaTracker(new Container()); 
            mediaTracker.addImage(ret_img, 0); 
            mediaTracker.waitForAll();

            return ret_img;
        } catch (IOException | InterruptedException ex) {
            _L("getPrerview ERROR: "+ex.getMessage());
            return null;
        }
    }
    
    public static synchronized ArrayList<DBImageX> getImagesX(long album_id, long start, long limit) {
        try {
            PreparedStatement ps;
            if ((album_id != -1)) {
                ps = SQL.getConnection().prepareStatement(
                        "SELECT " 
                                + "FS_preview_files.oid                 AS prev_oid, "
                                + "FS_preview_files.md5                 AS prev_md5, "
                                + "FS_preview_files.startSector         AS prev_startSector, "
                                + "FS_preview_files.sectorSize          AS prev_sectorSize, "
                                + "FS_preview_files.actualSize          AS prev_actualSize, "
                                + "FS_preview_files.fileName            AS prev_fileName, "
                                + "previews_list.oid                    AS pl_oid, "
                                + "previews_list.idid                   AS pl_idid, "
                                + "previews_list.pdid                   AS pl_pdid, "
                                + "previews_list.imgtype                AS pl_imgtype, "
                                + "images_albums.oid                    AS ia_oid, "
                                + "images_albums.imgoid                 AS ia_imgoid, "
                                + "images_albums.alboid                 AS ia_alboid "
                                +
                        "FROM "
                                + "previews_list "
                                + 
                        "LEFT JOIN "
                                + "FS_preview_files, "
                                + "images_albums "
                                +
                        "ON "
                                + "previews_list.pdid=FS_preview_files.oid "
                                + "AND "
                                + "previews_list.idid=images_albums.imgoid "
                                +
                        "WHERE "
                                + "previews_list.imgtype=? "
                                + ((album_id != -1) ?
                                  "AND "
                                + "images_albums.imgoid=? " : "")
                                + 
                        "LIMIT "
                                + "?,?; ");

                ps.setLong(1, ImageEngine.PREVIEW_TYPE_SMALL);
                ps.setLong(2, album_id);
                ps.setLong(3, start);
                ps.setLong(4, limit);
            } else {
                ps = SQL.getConnection().prepareStatement(
                        "SELECT " 
                                + "FS_preview_files.oid                 AS prev_oid, "
                                + "FS_preview_files.md5                 AS prev_md5, "
                                + "FS_preview_files.startSector         AS prev_startSector, "
                                + "FS_preview_files.sectorSize          AS prev_sectorSize, "
                                + "FS_preview_files.actualSize          AS prev_actualSize, "
                                + "FS_preview_files.fileName            AS prev_fileName, "
                                + "previews_list.oid                    AS pl_oid, "
                                + "previews_list.idid                   AS pl_idid, "
                                + "previews_list.pdid                   AS pl_pdid, "
                                + "previews_list.imgtype                AS pl_imgtype "
                                +
                        "FROM "
                                + "previews_list "
                                + 
                        "LEFT JOIN "
                                + "FS_preview_files "
                                +
                        "ON "
                                + "previews_list.pdid=FS_preview_files.oid "
                                +
                        "WHERE "
                                + "previews_list.imgtype=? "
                                + 
                        "LIMIT "
                                + "?,?; ");
                
                ps.setLong(1, ImageEngine.PREVIEW_TYPE_SMALL);
                ps.setLong(2, start);
                ps.setLong(3, limit);
            }
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                final ArrayList<DBImageX> db = new ArrayList<>();
                while (rs.next()) {
                    DBImageX dbe            = new DBImageX();
                    dbe.pl_idid             = rs.getLong("pl_idid");
                    dbe.pl_imgtype          = rs.getLong("pl_imgtype");
                    dbe.pl_oid              = rs.getLong("pl_oid");
                    dbe.pl_pdid             = rs.getLong("pl_pdid");
                    dbe.prev_actualSize     = rs.getLong("prev_actualSize");
                    dbe.prev_oid            = rs.getLong("prev_oid");
                    dbe.prev_sectorSize     = rs.getLong("prev_sectorSize");
                    dbe.prev_startSector    = rs.getLong("prev_startSector");
                    dbe.prev_fileName       = rs.getBytes("prev_fileName");
                    dbe.prev_md5            = rs.getBytes("prev_md5");
                    
                    if ((album_id != -1)) {
                        dbe.ia_alboid       = rs.getLong("ia_alboid");
                        dbe.ia_imgoid       = rs.getLong("ia_imgoid");
                        dbe.ia_oid          = rs.getLong("ia_oid");
                    } else {
                        dbe.ia_alboid       = -1;
                        dbe.ia_imgoid       = -1;
                        dbe.ia_oid          = -1;
                    }
                    
                    db.add(dbe);
                }
                return db;
            }
        } catch (SQLException ex) { 
            _L("getImagesX ERROR: "+ex.getMessage());
        }
        return null;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    public static synchronized int writeImageMetadataToDB(String path, long oid) {
        final String ext = FilenameUtils.getExtension(path);
        if (ext.length() < 2) return -1;
        
        final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(ext);

        if (it.hasNext()) {
            final ImageReader r = it.next();
            try {
                ImageInputStream stream = new FileImageInputStream(new File(path));
                r.setInput(stream);
                int 
                        width = r.getWidth(r.getMinIndex()),
                        height = r.getHeight(r.getMinIndex());
                double 
                        wh1 = ((double) width) / ((double) height);
                long
                        wh2 = width * height;
                
                PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO "+SQLite.QUOTE+"images_basic_meta"+SQLite.QUOTE+" VALUES(?, ?, ?, ?, ?, ?, ?);");
                final long tmr = new Date().getTime();
                ps.setLong(1, tmr);
                ps.setLong(2, oid);
                ps.setLong(3, width);
                ps.setLong(4, height);
                ps.setDouble(5, wh1);
                ps.setLong(6, wh2);
                ps.setBytes(7, Crypto.MD5(path.getBytes()));
                ps.execute();
                Sleep(2);
                r.dispose();
                
                return 0;
            } catch (IOException | SQLException e) {
                _L(e.getMessage());
            } finally { r.dispose(); }
        }
        return -1;
    }
    
    public static synchronized boolean isMD5InMetadata(String path) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"images_basic_meta"+SQLite.QUOTE+" WHERE fn_md5=?;");
            ps.setBytes(1, Crypto.MD5(path.getBytes()));
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    long act_size  = rs.getLong("oid");
                    if (act_size > 0) return true;
                }
            }
        } catch (SQLException ex) { }
        return false;
    }
    
    public static synchronized int addImageAndPreviewAssoc(long imgID, long previewID, int type) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO "+SQLite.QUOTE+"previews_list"+SQLite.QUOTE+" VALUES(?, ?, ?, ?);");
            final long tmr = new Date().getTime();
            ps.setLong(1, tmr);
            ps.setLong(2, imgID);
            ps.setLong(3, previewID);
            ps.setLong(4, type);
            ps.execute();
            Sleep(2);
            return 0;
        } catch (SQLException ex) { _L(ex.getMessage()); }
        return -1;
    }
    
    public static synchronized void addNewAlbumGroup(String name) {
            try {
                PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO "+SQLite.QUOTE+"AlbumsGroup"+SQLite.QUOTE+" VALUES(?, ?, 1);");
                final long tmr = new Date().getTime();
                ps.setLong(1, tmr);
                ps.setBytes(2, xCrypto.Crypt(xCrypto.align16b(name.getBytes())));
                ps.execute();
                
            } catch (SQLException ex) { }
    }
    
    public static synchronized BufferedImage getPrerview(int xtype, long IID, SQLiteFS SFS_preview) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"previews_list"+SQLite.QUOTE+" WHERE idid=? AND imgtype=?;");
            ps.setLong(1, IID);
            ps.setInt(2, xtype);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
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
                } else
                    return null;
            } else {
                return null;
            }
        } catch (SQLException | IOException | InterruptedException ex) {
            _L("getPrerview ERROR: "+ex.getMessage());
            return null;
        }
    }
    
    public static void WriteAPPSettingsString(String optName, String value) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO "+SQLite.QUOTE+"StringSettings"+SQLite.QUOTE+" VALUES(?, ?);");
            ps.setString(1, optName);
            ps.setString(2, value); 
            ps.execute();
//            SQL.getConnection().commit();
        } catch (SQLException ex) {
            try {
                PreparedStatement ps = SQL.getConnection().prepareStatement("UPDATE "+SQLite.QUOTE+"StringSettings"+SQLite.QUOTE+" SET xvalue=? WHERE xname=?;");
                ps.setString(1, value);
                ps.setString(2, optName);
                ps.execute();
//                SQL.getConnection().commit();
            } catch (SQLException ex1) {
                _L("WriteAPPSettingsString ERROR: "+ex.getMessage());
            }
        }
    }
    
    public static String ReadAPPSettingsString(String optName) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"StringSettings"+SQLite.QUOTE+" WHERE xname=?;");
            ps.setString(1, optName); 
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    final String retval = rs.getString("xvalue");
                    return retval;
                }
            }
        } catch (SQLException ex) {
            _L("ReadAPPSettingsString ERROR: "+ex.getMessage());
        }
        return "";
    }
    
    @SuppressWarnings("UnnecessaryUnboxing")
    public static boolean isImageLiked(long imgOID) {
        ArrayList<Long> al = DBWrapper.getGroupsByImageOID(imgOID);
        if (al == null) return false;
        return al.stream().anyMatch((l) -> (l == ImageEngine.ALBUM_ID_FAVORITES));
    }
    
    @SuppressWarnings("UnnecessaryUnboxing")
    public static boolean isImageDeleted(long imgOID) {
        ArrayList<Long> al = DBWrapper.getGroupsByImageOID(imgOID);
        if (al == null) { return false;}
        return al.stream().anyMatch((l) -> (l == ImageEngine.ALBUM_ID_DELETED));
    }
    
    public static int setImageDeleted(long imgOID, boolean del) {
        if (del) {
            return DBWrapper.setImageGroupID(imgOID, ImageEngine.ALBUM_ID_DELETED);
        } else {
            return DBWrapper.delImageGroupID(imgOID, ImageEngine.ALBUM_ID_DELETED);
        }
    }
    
    public static int setImageLiked(long imgOID, boolean like) {
        if (like) {
            return DBWrapper.setImageGroupID(imgOID, ImageEngine.ALBUM_ID_FAVORITES);
        } else {
            return DBWrapper.delImageGroupID(imgOID, ImageEngine.ALBUM_ID_FAVORITES);
        }
    }
    
    public static synchronized int delImageGroupID(long imgOID, long groupOID) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("DELETE FROM "+SQLite.QUOTE+"images_albums"+SQLite.QUOTE+" WHERE imgoid=? AND alboid=?;");
            ps.setLong(1, imgOID);
            ps.setLong(2, groupOID);
            ps.execute();
            return 0;
        } catch (SQLException ex) { 
            _L("delImageGroupID ERROR: "+ex.getMessage());
        } 
        
        return -1;  
    }
    
    public static synchronized long getImagesCountInAlbum(long oid) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT COUNT(oid) FROM "+SQLite.QUOTE+"images_albums"+SQLite.QUOTE+" WHERE alboid=?;");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    long sz = rs.getLong("COUNT(oid)");
                    return sz;
                    }
            }
        } catch (SQLException ex) { 
            _L("getImagesCountInAlbum ERROR: "+ex.getMessage());
        }
        return -1;
    }
    
    public static synchronized ArrayList<Long> getGroupsByImageOID(long oid) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"images_albums"+SQLite.QUOTE+" WHERE imgoid=?;");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Long> all = new ArrayList<>();
                while (rs.next()) {
                    all.add(rs.getLong("alboid"));
                }
                
                return all;
            }
        } catch (SQLException ex) { 
            _L("getGroupsByImageOID ERROR: "+ex.getMessage()); 
        }
        return null;
    }
    
    public static synchronized ArrayList<Long> getImagesByGroupOID(long oid, int limStart, int limCount) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"images_albums"+SQLite.QUOTE+" WHERE alboid=? ORDER BY oid ASC LIMIT "+limStart+","+limCount+";");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Long> all = new ArrayList<>();
                while (rs.next()) {
                    all.add(rs.getLong("imgoid"));
                }
                return all;
            }
        } catch (SQLException ex) { 
            _L("getImagesByGroupOID ERROR: "+ex.getMessage());
        }
        return null;
    }
    
    @SuppressWarnings("SleepWhileHoldingLock")
    public static synchronized int setImageGroupID(long imgOID, long groupOID) {
        ArrayList<Long> al = getGroupsByImageOID(imgOID);
        if (al == null) return -1;
        for (long l : al) {
            if (l == groupOID) return -2;
        }
        
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO "+SQLite.QUOTE+"images_albums"+SQLite.QUOTE+" VALUES(?, ?, ?);");
            final long tmr = new Date().getTime();
            ps.setLong(1, tmr);
            ps.setLong(2, imgOID);
            ps.setLong(3, groupOID);
            ps.execute();
            Sleep(2);
            return 0;
        } catch (SQLException ex) { 
            _L("setImageGroupID ERROR: "+ex.getMessage());
        } 
        
        return -1;
    }
    
    public static synchronized int addPreviewAssoc(long imgID, byte[] md5) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO "+SQLite.QUOTE+"previews_files"+SQLite.QUOTE+" VALUES(?, ?, ?);");
            final long tmr = new Date().getTime();
            ps.setLong(1, tmr);
            ps.setLong(2, imgID);
            ps.setBytes(3, md5);
            ps.execute();
            Sleep(2);
            return 0;
        } catch (SQLException ex) { 
            _L("addPreviewAssoc ERROR: "+ex.getMessage());
        }
        return -1;
    }
    
    public static synchronized long getIDByMD5(byte[] md5r) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"previews_files"+SQLite.QUOTE+" WHERE md5=?;");
            ps.setBytes(1, md5r);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    final long idid = rs.getLong("idid");
                    if (idid > 0)
                        return idid;
                    else 
                        return -1;
                    }
            }
            return -1;
        } catch (SQLException ex) {
            _L("getIDByMD5 ERROR: "+ex.getMessage());
            return -1;
        }
    }
        
    public static synchronized ArrayList<AlbumsCategory> getAlbumsGroupsID() {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"AlbumsGroup"+SQLite.QUOTE+" ORDER BY oid DESC;");
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<AlbumsCategory> alac = new ArrayList<>();
                while (rs.next()) {
                    AlbumsCategory ac = new AlbumsCategory(new String(xCrypto.Decrypt(rs.getBytes("groupName"))).trim(), rs.getLong("oid"), rs.getInt("state"));
                    alac.add(ac);
                }
                return alac;
            }
        } catch (SQLException ex) {
            _L("getAlbumsGroupsID ERROR: "+ex.getMessage());
            return null;
        }
        
        return null;
    }
    
    public static synchronized int saveAlbumsCategoryChanges(String name, int state, long ID) {
        try { 
            PreparedStatement ps = SQL.getConnection().prepareStatement("UPDATE "+SQLite.QUOTE+"AlbumsGroup"+SQLite.QUOTE+" SET groupName=?, state=? WHERE oid=?;");
            ps.setBytes(1, xCrypto.Crypt(xCrypto.align16b(name.getBytes())));
            ps.setInt(2, state);
            ps.setLong(3, ID);
            ps.execute();
            Sleep(2);
            return 0;
        } catch (SQLException  ex) {
            _L("saveAlbumsCategoryChanges ERROR: "+ex.getMessage());
            return -1;
        }
    }
    
    public static void Sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException ex) { }
    }
    
    private static void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s);
    }
}
