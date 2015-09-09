package dataaccess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import jnekoimagesdb.JNekoImageDB;

public class SQLiteFS {   
    private final SQLite 
            SQL;// = new SQLite();
    
    private final SplittedFile
            FILE;
    
    private final Crypto 
            fileCrypto;

    private final String 
            DBNameE;

    public SQLiteFS(Crypto k, String dbname, SQLite sql) {
        DBNameE = dbname;
        fileCrypto = k;
        FILE = new SplittedFile(k);
        SQL = sql;

        SQL.ExecuteSQL("CREATE TABLE if not exists "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" (oid bigint not null primary key, md5 BINARY(16), startSector bigint, sectorSize int, actualSize int, fileName BINARY(512));");
    }

    public SQLite GetSQL() {
        return SQL;
    }
    
    public synchronized Image PopImage(long oid) {
        final byte[] b = PopFile(oid);
        if (b == null) return null;
        
        final ByteArrayInputStream bb = new ByteArrayInputStream(b);
        return new Image(bb);
    }
    
    public long getIOPS_R() {
        return SplittedFile.MyIOPS_RE;
    }
    
    public long getIOPS_W() {
        return SplittedFile.MyIOPS_WE;
    }

    public long getCount() {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT COUNT(oid) FROM "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" WHERE oid NOT IN (SELECT DISTINCT imgoid FROM images_albums)"); //("SELECT COUNT(oid) FROM 'FS_"+DBNameE+"_files';");
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    long sz  = rs.getLong("COUNT(oid)");
                    if (sz > 0) return sz;
                }
            }
        } catch (SQLException ex) { }
        return -1;
    }
    
    public ArrayList<Long> getImages(String _sql) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT oid FROM "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" WHERE oid NOT IN (SELECT DISTINCT imgoid FROM images_albums) " + _sql);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Long> all = new ArrayList<>();
                while (rs.next()) {
                    all.add(rs.getLong("oid"));
                }
                return all;
            }
        } catch (SQLException ex) {
            return null;
        }
        
        return null;
    } 
    
    public boolean isMD5Present(byte[] md5) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" WHERE md5=?;");
            ps.setBytes(1, md5);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    long act_size  = rs.getLong("actualSize");
                    if (act_size > 0) return true;
                }
            }
        } catch (SQLException ex) { }
        return false;
    }
    
    public synchronized byte[] PopFile(long oid) {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory < (64 * 1024 * 1024)) {
            _L("Free RAM too small");
            return null;
        }
        
        byte md5[];
        final ByteArrayOutputStream md5e = new ByteArrayOutputStream();
        final ByteArrayOutputStream read_buf = new ByteArrayOutputStream();
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" WHERE oid=?;");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    final long 
                            st_sector = rs.getLong("startSector"),
                            sz_sector = rs.getLong("sectorSize"),
                            act_size  = rs.getLong("actualSize");

                    final byte[] md5_sql = 
                            rs.getBytes("md5");

                    if ((sz_sector <= 0) || (act_size <= 0)) {
                        _L("Invalid database record; null file;");
                        return null;
                    }

                    for (long i=st_sector; i<(st_sector + sz_sector); i++) {
                        final byte[] buf_w = FILE.ReadFileSector(DBNameE, (int)i);
                        read_buf.write(buf_w); 
                        md5e.write(Crypto.MD5(buf_w));
                    }

                    md5 = Crypto.MD5(md5e.toByteArray());
                    md5e.reset();
                    if (!Arrays.equals(md5, md5_sql)) {
                        _L("Invalid database record; md5 incorrect");
                        return null;
                    }

                    final byte[] ret = new byte[(int)act_size];
                    System.arraycopy(read_buf.toByteArray(), 0, ret, 0, (int)act_size);
                    read_buf.reset();

                    return ret;
                }
            }
        } catch (IOException | SQLException ex) {
            _L(ex.getMessage());
            return null;
        }
        
        return null;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public synchronized int PopFile(long oid, String path) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" WHERE oid=?;");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    final long 
                            st_sector = rs.getLong("startSector"),
                            sz_sector = rs.getLong("sectorSize"),
                            act_size  = rs.getLong("actualSize");

                    final String 
                            fileNameOut = new String(fileCrypto.Decrypt(rs.getBytes("fileName"))).trim();

                    final byte[] md5_sql = 
                            rs.getBytes("md5");

                    if ((sz_sector <= 0) || (act_size <= 0)) {
                        _L("Invalid database record; null file;");
                        return -1;
                    }

                    byte md5[];
                    final ByteArrayOutputStream 
                            md5e = new ByteArrayOutputStream(),
                            fileOut = new ByteArrayOutputStream();
                    try {
                        final FileOutputStream fos = new FileOutputStream(new File(path).getAbsolutePath() + File.separator + fileNameOut);
                        for (long i=st_sector; i<(st_sector + sz_sector); i++) {
                            final byte[] buf_w = FILE.ReadFileSector(DBNameE, (int)i);
                            fileOut.write(buf_w); 
                            md5e.write(Crypto.MD5(buf_w));
                        }

                        fos.write(fileOut.toByteArray(), 0, (int) act_size);
                        fos.close();
                        fileOut.reset();

                        md5 = Crypto.MD5(md5e.toByteArray());
                        //md5e.reset();
                        if (!Arrays.equals(md5, md5_sql)) {
                            _L("Invalid database record; md5 incorrect");
                            return -1;
                        }

                        return 0;
                    } catch (FileNotFoundException ex) { 
                        _L("Cannot read/write file ["+path+"]");
                        return -1;
                    }
                }
            }
            return -1;
        } catch (IOException | SQLException ex) {
            _L(ex.getMessage());
            return -1;
        }
    }

    public synchronized long PushFileMT(byte[] file) {
        final long realSize = file.length; 
        final long sectorSize = (realSize / SplittedFile.SECTOR_SIZE) + (((realSize % SplittedFile.SECTOR_SIZE) > 0) ? 1 : 0);
        final byte[] md5Hash = getFileMD5MT(file, sectorSize);

        final Map<String, Long> allocInfo = allocateDiskSpaceMT(sectorSize, realSize, "NULL-PREVIEW", md5Hash);
        if (allocInfo == null) {
            _L("PushFileMT->allocateDiskSpaceMT(): [NULL] cannot allocate disk space.");
            return -1;
        }
        
        final long startSector = allocInfo.get("lastSector");
        final long myID = allocInfo.get("myID");
        byte sectorBuffer[];
        
        for (long i=startSector, j=0; i<(startSector+sectorSize); i++, j++) {
            sectorBuffer = Arrays.copyOfRange(file, (int)(j*SplittedFile.SECTOR_SIZE), (int)((j+1)*SplittedFile.SECTOR_SIZE));
            FILE.WriteFileSector(DBNameE, (int)(i), sectorBuffer);
        }
        
        return myID;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public synchronized long PushFileMT(String fileName) {
        final File file = new File(fileName);
        if ((!file.canRead()) || (!file.isFile())) {
            _L("PushFileMT(): ["+fileName+"] no a regular file.");
            return -1;
        }
        
        final long realSize = file.length();
        final long sectorSize = (realSize / SplittedFile.SECTOR_SIZE) + (((realSize % SplittedFile.SECTOR_SIZE) > 0) ? 1 : 0);
        final byte[] md5Hash = getFileMD5MT(fileName);

        final Map<String, Long> allocInfo = allocateDiskSpaceMT(sectorSize, realSize, file.getName(), md5Hash);
        if (allocInfo == null) {
            _L("PushFileMT->allocateDiskSpaceMT(): ["+fileName+"] cannot allocate disk space.");
            return -1;
        }
        
        final long startSector = allocInfo.get("lastSector");
        final long myID = allocInfo.get("myID");
        
        try {
            final FileInputStream fis = new FileInputStream(fileName);
            byte[] sectorBuffer = new byte[(int) SplittedFile.SECTOR_SIZE];
            int readCount;
            
            for (long i=startSector; i<(startSector+sectorSize); i++) {
                Arrays.fill(sectorBuffer, (byte)0);
                readCount = fis.read(sectorBuffer, 0, (int)SplittedFile.SECTOR_SIZE);
                if (readCount == -1) break;
                FILE.WriteFileSector(DBNameE, (int)(i), sectorBuffer);
            }

            fis.close();
            
            return myID;
        } catch (IOException ex) {
            _L("PushFileMT():"+ex.getMessage());
            return -1;
        }
    }
    
    @SuppressWarnings("SleepWhileHoldingLock")
    private synchronized Map<String, Long> allocateDiskSpaceMT(long sectorSize, long realSize, String name, byte[] md5Hash) {
        final long lastSector = getLastSector();
        if (lastSector < 0) return null;
        
        try { 
            PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" VALUES(?, ?, ?, ?, ?, ?);");
            final long tmr = new Date().getTime();
            ps.setLong(1, tmr);
            ps.setBytes(2, md5Hash);
            ps.setLong(3, lastSector); 
            ps.setLong(4, sectorSize);
            ps.setLong(5, realSize);
            ps.setBytes(6, fileCrypto.Crypt(align16b(name.getBytes())));
            ps.execute();
            
            final Map<String, Long> retVal = new HashMap<>();
            retVal.put("lastSector", lastSector);
            retVal.put("myID", tmr);
            
            try { Thread.sleep(2); } catch (InterruptedException ex) {  }
            
            return retVal;
        } catch (SQLException  ex) {
            _L(ex.getMessage());
            return null;
        }
    }
    
    public static byte[] getFileMD5MT(String file) {
        return SplittedFile.getFileMD5MT(file);
    }
    
    public static byte[] getFileMD5MT(byte[] file, long sectorSize) {
        return SplittedFile.getFileMD5MT(file, sectorSize);
    }

    private byte[] align16b(byte[] b) {
        int 
                sz = b.length / 16,
                tail = b.length % 16;
        if (tail != 0) sz++;
        
        final byte fn[] = new byte[sz*16];
        for (int i=0; i<(sz*16); i++) if (i < b.length) fn[i] = b[i]; else fn[i] = 0;
        return fn;
    }

    private synchronized long getLastSector() {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+SQLite.QUOTE+"FS_"+DBNameE+"_files"+SQLite.QUOTE+" ORDER BY oid DESC LIMIT 0,1;");
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    final long 
                            st_sector = rs.getLong("startSector"),
                            sz_sector = rs.getLong("sectorSize");
                    return st_sector + sz_sector;
                } else 
                    return 0;
            }
        } catch (SQLException ex) {
            _L(ex.getMessage());
            return -1;
        }
        return -1;
    }
    
    private static void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s);
    }
}
