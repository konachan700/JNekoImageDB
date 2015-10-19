package dataaccess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import jnekoimagesdb.JNekoImageDB;

public class FSEngine {   
    public static final int
            TYPE_JPEG = 1,
            TYPE_PNG = 2;
    
    public static final String
            SQL_FIELD_START_SECTOR = "startSector",
            SQL_FIELD_SECTOR_COUNT = "sectorSize",
            SQL_FIELD_LAST_SECTOR = "lastSector", 
            SQL_FIELD_ACTUAL_SIZE = "actualSize", 
            
            MAP_FIELD_MY_ID = "myID",
            
            SQL_DEFAULT_FILENAME = "NULL-PREVIEW",
            
            SQL_TABLE_PREFIX = "FS_",
            SQL_TABLE_SUFFIX = "_files",
            
            SQL_SELECT_ALL_FROM = "SELECT * FROM ",
            SQL_ORDER_BY_X1 = " ORDER BY oid DESC LIMIT 0,1;", 
            SQL_INSERT_INTO = "INSERT INTO ",
            SQL_ADS_VALUES = " VALUES(?, ?, ?, ?, ?, ?);",
            SQL_MD5_PRESENT_WHERE_X1 = " WHERE md5=?;";
    
    private final DBEngine 
            SQL;
    
    private final SplittedFile
            FILE;
    
//    private final Crypto 
//            fileCrypto;

    private final String 
            DBNameE;

    public FSEngine(Crypto k, String dbname, DBEngine sql) {
        DBNameE = dbname;
//        fileCrypto = k;
        FILE = new SplittedFile(k);
        SQL = sql;

        SQL.ExecuteSQL("CREATE TABLE if not exists "+DBEngine.QUOTE+"FS_"+DBNameE+"_files"+DBEngine.QUOTE+" (oid bigint not null primary key, md5 BINARY(16), startSector bigint, sectorSize int, actualSize int, fileType int);");
    }

    public DBEngine GetSQL() {
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
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT COUNT(oid) FROM "+DBEngine.QUOTE+"FS_"+DBNameE+"_files"+DBEngine.QUOTE+" WHERE oid NOT IN (SELECT DISTINCT imgoid FROM images_albums)"); //("SELECT COUNT(oid) FROM 'FS_"+DBNameE+"_files';");
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    long sz  = rs.getLong("COUNT(oid)");
                    if (sz > 0) return sz;
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException ex) { }
        return -1;
    }
    
    public ArrayList<Long> getImages(String _sql) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT oid FROM "+DBEngine.QUOTE+"FS_"+DBNameE+"_files"+DBEngine.QUOTE+" WHERE oid NOT IN (SELECT DISTINCT imgoid FROM images_albums) " + _sql);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<Long> all = new ArrayList<>();
                while (rs.next()) {
                    all.add(rs.getLong("oid"));
                }
                rs.close();
                ps.close();
                
                return all;
            }
        } catch (SQLException ex) {
            return null;
        }
        
        return null;
    } 
    
    public synchronized boolean isMD5Present(byte[] md5) {
        final StringBuilder sql_q = new StringBuilder();
        sql_q.append(SQL_SELECT_ALL_FROM).append(DBEngine.QUOTE).append(SQL_TABLE_PREFIX).append(DBNameE)
                .append(SQL_TABLE_SUFFIX).append(DBEngine.QUOTE).append(SQL_MD5_PRESENT_WHERE_X1);
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement(sql_q.substring(0));
            ps.setBytes(1, md5);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    long act_size  = rs.getLong(SQL_FIELD_ACTUAL_SIZE);
                    if (act_size > 0) {
                        rs.close();
                        ps.close();
                        return true;
                    }
                }
                rs.close();
            }
            ps.close();
        } catch (SQLException ex) { }
        return false;
    }
    
    public synchronized byte[] PopPrewievFile(DBImageX dbe) {
        try {
            byte md5[];
            final ByteArrayOutputStream md5e = new ByteArrayOutputStream();
            final ByteArrayOutputStream read_buf = new ByteArrayOutputStream();

            final long 
                    st_sector = dbe.prev_startSector,
                    sz_sector = dbe.prev_sectorSize,
                    act_size  = dbe.prev_actualSize;

            final byte[] md5_sql = dbe.prev_md5;

            if ((sz_sector <= 0) || (act_size <= 0)) {
                _L(Lang.ERR_FSEngine_null_file);
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
                _L(Lang.ERR_FSEngine_incorrect_md5_for_record);
                return null;
            }

            final byte[] ret = new byte[(int)act_size];
            System.arraycopy(read_buf.toByteArray(), 0, ret, 0, (int)act_size);
            read_buf.reset();

            //_L("Object size = "+ret.length);
            return ret;
        } catch (IOException ex) {
            //_L(ex.getMessage());
            return null;
        }
    }
    
    
    public synchronized byte[] PopFile(long oid) {
        byte md5[];
        final ByteArrayOutputStream md5e = new ByteArrayOutputStream();
        final ByteArrayOutputStream read_buf = new ByteArrayOutputStream();
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+DBEngine.QUOTE+"FS_"+DBNameE+"_files"+DBEngine.QUOTE+" WHERE oid=?;");
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
                        _L(Lang.ERR_FSEngine_null_file);
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
                        _L(Lang.ERR_FSEngine_incorrect_md5_for_record);
                        return null;
                    }

                    final byte[] ret = new byte[(int)act_size];
                    System.arraycopy(read_buf.toByteArray(), 0, ret, 0, (int)act_size);
                    read_buf.reset();
                    
                    ps.close();
                    rs.close();

                    return ret;
                }
            }
        } catch (IOException | SQLException ex) {
            //_L(ex.getMessage());
            return null;
        }
        
        return null;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public synchronized int PopFile(long oid, String path) {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM "+DBEngine.QUOTE+"FS_"+DBNameE+"_files"+DBEngine.QUOTE+" WHERE oid=?;");
            ps.setLong(1, oid);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    final long 
                            st_sector = rs.getLong("startSector"),
                            sz_sector = rs.getLong("sectorSize"),
                            act_size  = rs.getLong("actualSize");

                    final String ext;
                    switch (rs.getInt("fileType")) {
                        case TYPE_JPEG:
                            ext = ".jpg";
                            break;
                        case TYPE_PNG:
                            ext = ".png";
                            break;
                        default:
                            ext = ".bin";
                    }
                    
                    final String fileNameOut = oid + ext;
                    final byte[] md5_sql = rs.getBytes("md5");

                    if ((sz_sector <= 0) || (act_size <= 0)) {
                        _L(Lang.ERR_FSEngine_null_file);
                        return -1;
                    }
                    
                    byte md5[];
                    final ByteArrayOutputStream 
                            md5e = new ByteArrayOutputStream(),
                            fileOut = new ByteArrayOutputStream();
                    try {
                        //_L(new File(path).getAbsolutePath() + File.separator + fileNameOut);
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
                            _L(Lang.ERR_FSEngine_incorrect_md5_for_record);
                            return -1;
                        }
                        
                        ps.close();
                        rs.close();

                        return 0;
                    } catch (FileNotFoundException ex) { 
                        _L(Lang.ERR_FSEngine_cannot_read_write_file + " ["+path+"]");
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

    public synchronized long PushFileMT(byte[] file, byte[] md5HashX) {
        final long realSize = file.length; 
        final long sectorSize = (realSize / SplittedFile.SECTOR_SIZE) + (((realSize % SplittedFile.SECTOR_SIZE) > 0) ? 1 : 0);
        final byte[] md5Hash = (md5HashX != null) ? md5HashX : getFileMD5MT(file, sectorSize);

        final Map<String, Long> allocInfo = allocateDiskSpaceMT(sectorSize, realSize, SQL_DEFAULT_FILENAME, md5Hash);
        if (allocInfo == null) {
            _L(Lang.ERR_FSEngine_no_disk_space);
            return -1;
        }
        
        final long startSector = allocInfo.get(SQL_FIELD_LAST_SECTOR);
        final long myID = allocInfo.get(MAP_FIELD_MY_ID);
        byte sectorBuffer[];
        
        for (long i=startSector, j=0; i<(startSector+sectorSize); i++, j++) {
            sectorBuffer = Arrays.copyOfRange(file, (int)(j*SplittedFile.SECTOR_SIZE), (int)((j+1)*SplittedFile.SECTOR_SIZE));
            FILE.WriteFileSector(DBNameE, (int)(i), sectorBuffer);
        }

        return myID;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public synchronized long PushFileMT(String fileName, byte[] md5HashX) {
        final File file = new File(fileName);
        if ((!file.canRead()) || (!file.isFile())) {
            //_L("PushFileMT(): ["+fileName+"] no a regular file.");
            return -1;
        }
        
        final long realSize = file.length();
        final long sectorSize = (realSize / SplittedFile.SECTOR_SIZE) + (((realSize % SplittedFile.SECTOR_SIZE) > 0) ? 1 : 0);
        final byte[] md5Hash = (md5HashX != null) ? md5HashX : getFileMD5MT(fileName);

        final Map<String, Long> allocInfo = allocateDiskSpaceMT(sectorSize, realSize, file.getName(), md5Hash);
        if (allocInfo == null) {
            //_L("PushFileMT->allocateDiskSpaceMT(): ["+fileName+"] cannot allocate disk space.");
            return -1;
        }
        
        final long startSector = allocInfo.get(SQL_FIELD_LAST_SECTOR);
        final long myID = allocInfo.get(MAP_FIELD_MY_ID);
        
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
            //_L(ex.getMessage());
            return -1;
        }
    }
    
    @SuppressWarnings("SleepWhileHoldingLock")
    private synchronized Map<String, Long> allocateDiskSpaceMT(long sectorSize, long realSize, String name, byte[] md5Hash) {
        final long lastSector = getLastSector();
        if (lastSector < 0) return null;
        
        final StringBuilder sql_q = new StringBuilder();
        sql_q.append(SQL_INSERT_INTO).append(DBEngine.QUOTE).append(SQL_TABLE_PREFIX).append(DBNameE).append(SQL_TABLE_SUFFIX).append(DBEngine.QUOTE).append(SQL_ADS_VALUES);
        
        try { 
            PreparedStatement ps = SQL.getConnection().prepareStatement(sql_q.substring(0));
            final long tmr = new Date().getTime();
            ps.setLong(1, tmr);
            ps.setBytes(2, md5Hash);
            ps.setLong(3, lastSector); 
            ps.setLong(4, sectorSize);
            ps.setLong(5, realSize);
            
            int ftype = 0;
            if ((name.toLowerCase().endsWith(".jpg")) || (name.toLowerCase().endsWith(".jpeg"))) ftype = TYPE_JPEG;
            if (name.toLowerCase().endsWith(".png")) ftype = TYPE_PNG;
            
            ps.setInt(6, ftype);
            ps.execute();
            
            final Map<String, Long> retVal = new HashMap<>();
            retVal.put(SQL_FIELD_LAST_SECTOR, lastSector);
            retVal.put(MAP_FIELD_MY_ID, tmr);
            
            try { Thread.sleep(2); } catch (InterruptedException ex) {  }
            ps.close();
            
            return retVal;
        } catch (SQLException  ex) {
            //_L(ex.getMessage());
            return null;
        }
    }
    
    public static byte[] getFileMD5MT(String file) {
        return SplittedFile.getFileMD5MT(file);
    }
    
    public static byte[] getFileMD5MT(byte[] file, long sectorSize) {
        return SplittedFile.getFileMD5MT(file, sectorSize);
    }

//    private byte[] align16b(byte[] b) {
//        int 
//                sz = b.length / 16,
//                tail = b.length % 16;
//        if (tail != 0) sz++;
//        
//        final byte fn[] = new byte[sz*16];
//        for (int i=0; i<(sz*16); i++) if (i < b.length) fn[i] = b[i]; else fn[i] = 0;
//        return fn;
//    }

    private synchronized long getLastSector() {
        final StringBuilder sql_q = new StringBuilder();
        sql_q.append(SQL_SELECT_ALL_FROM).append(DBEngine.QUOTE).append(SQL_TABLE_PREFIX).append(DBNameE).append(SQL_TABLE_SUFFIX).append(DBEngine.QUOTE).append(SQL_ORDER_BY_X1);
        
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement(sql_q.substring(0));
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    final long 
                            st_sector = rs.getLong(SQL_FIELD_START_SECTOR),
                            sz_sector = rs.getLong(SQL_FIELD_SECTOR_COUNT);
                    return st_sector + sz_sector;
                } else 
                    return 0;
            }
        } catch (SQLException ex) {
            //_L(ex.getMessage());
            return -1;
        }
        return -1;
    }
    
//    private String nullTrim(String t) {
//        int pos = t.indexOf(0);
//        if (pos > 0) return t.substring(0, pos); else return t;
//    }
    
    private static void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s);
    }
}
