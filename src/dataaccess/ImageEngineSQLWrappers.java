package dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageEngineSQLWrappers {
    private final SQLite SQL;
    
    public ImageEngineSQLWrappers(SQLite s) {
        SQL = s;
    }
    
    public ArrayList<Long> getGroupsByImageOID(long oid) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM 'images_albums' WHERE imgoid=?;");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Long> all = new ArrayList<>();
                while (rs.next()) {
                    all.add(rs.getLong("alboid"));
                }
                
                return all;
            }
        } catch (SQLException ex) { System.out.println("err()="+ex.getMessage()+"; "); }
        return null;
    }
    
    public ArrayList<Long> getImagesByGroupOID(long oid, int limStart, int limCount) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM 'images_albums' WHERE alboid=? ORDER BY oid ASC LIMIT "+limStart+","+limCount+";");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Long> all = new ArrayList<>();
                while (rs.next()) {
                    all.add(rs.getLong("imgoid"));
                }
                return all;
            }
        } catch (SQLException ex) { }
        return null;
    }
    
    public long getImagesCountInAlbum(long oid) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT COUNT(oid) FROM 'images_albums' WHERE alboid=?;");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                long sz = rs.getLong("COUNT(oid)");
                return sz;
            }
        } catch (SQLException ex) { }
        return -1;
    }
    
    
    
    
    
    
    
    public synchronized int delImageGroupID(long imgOID, long groupOID) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("DELETE FROM 'images_albums' WHERE imgoid=? AND alboid=?;");
            ps.setLong(1, imgOID);
            ps.setLong(2, groupOID);
            ps.execute();
            return 0;
        } catch (SQLException ex) { } 
        
        return -1;  
    }
    
    @SuppressWarnings("UnnecessaryUnboxing")
    public boolean isImageLiked(long imgOID) {
        ArrayList<Long> al = getGroupsByImageOID(imgOID);
        if (al == null) return false;
        for (long l : al) if (l == ImageEngine.ALBUM_ID_FAVORITES) return true; 
        return false;
    }
    
    @SuppressWarnings("UnnecessaryUnboxing")
    public boolean isImageDeleted(long imgOID) {
        ArrayList<Long> al = getGroupsByImageOID(imgOID);
        if (al == null) { return false;}
        for (long l : al) if (l == ImageEngine.ALBUM_ID_DELETED) return true;
        return false;
    }
    
    public int setImageDeleted(long imgOID, boolean del) {
        if (del) {
            return setImageGroupID(imgOID, ImageEngine.ALBUM_ID_DELETED);
        } else {
            return delImageGroupID(imgOID, ImageEngine.ALBUM_ID_DELETED);
        }
    }
    
    public int setImageLiked(long imgOID, boolean like) {
        if (like) {
            return setImageGroupID(imgOID, ImageEngine.ALBUM_ID_FAVORITES);
        } else {
            return delImageGroupID(imgOID, ImageEngine.ALBUM_ID_FAVORITES);
        }
    }
    
    @SuppressWarnings("SleepWhileHoldingLock")
    public synchronized int setImageGroupID(long imgOID, long groupOID) {
        ArrayList<Long> al = getGroupsByImageOID(imgOID);
        if (al == null) return -1;
        for (long l : al) {
            if (l == groupOID) return -2;
        }
        
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO 'images_albums' VALUES(?, ?, ?);");
            final long tmr = new Date().getTime();
            ps.setLong(1, tmr);
            ps.setLong(2, imgOID);
            ps.setLong(3, groupOID);
            ps.execute();
            Thread.sleep(2); // необходимо для того, чтобы все ID были уникальными. Костыль.
            return 0;
        } catch (SQLException | InterruptedException ex) { } 
        
        return -1;
    }
}
