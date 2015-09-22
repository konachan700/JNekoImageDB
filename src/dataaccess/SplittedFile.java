package dataaccess;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import jnekoimagesdb.JNekoImageDB;

public class SplittedFile {
    public static volatile long
            MyIOPS_R  = 0,
            MyIOPS_W  = 0,
            MyIOPS_RE = 0,
            MyIOPS_WE = 0;
    
    @SuppressWarnings("UnnecessaryUnboxing")
    private final Timeline IOPS_TMR = new Timeline(new KeyFrame(Duration.millis(1000), ae -> {
        MyIOPS_RE = (MyIOPS_RE + new Long(MyIOPS_R).longValue()) / 2;
        MyIOPS_WE = (MyIOPS_WE + new Long(MyIOPS_W).longValue()) / 2;
        MyIOPS_W = 0;
        MyIOPS_R = 0;
    }));
    
    public final static long 
            ONE_PART_SIZE   = 1024 * 1024,
            SECTOR_SIZE     = 1024 * 8;
    
    public final static int
            ERRCODE_IO_EXCEPTION = -1,
            ERRCODE_INCORRECT_SECTOR_SIZE = -2,
            ERRCODE_DECRYPT_ERROR = -3,
            ERRCODE_INVALID_CRYPT_KEY = -4,
            ERRCODE_CRYPT_ERROR = -5,
            
            DB_FILE_EXT_LEN = 4; // (10^4) * ONE_PART_SIZE * SECTOR_SIZE = 30TB maximum db size
    
    public final static String
            DATABASE_FOLDER = "./database/";
    
    public int
            errcode = 0;
    
    private final Crypto fileCrypto;
    private final Map<Long, FileInputStream> rfiles = new LinkedHashMap<>();
    private final Map<Long, FileOutputStream> wfiles = new LinkedHashMap<>();
    
    private final byte 
            buf_t[] = new byte[(int)SECTOR_SIZE];
    
    private final File 
            dbdir = new File(DATABASE_FOLDER);
    
    public SplittedFile(Crypto k) {
        fileCrypto = k;
        IOPS_TMR.setCycleCount(Animation.INDEFINITE);
        IOPS_TMR.play();
    }
    
    public void CloseAllStreams() {
        rfiles.keySet().stream().forEach((l) -> {
            try {
                rfiles.get(l).close();
            } catch (IOException ex) { }
        }); 
        
        wfiles.keySet().stream().forEach((l) -> {
            try {
                wfiles.get(l).close();
            } catch (IOException ex) { }
        }); 
    }
    
    public synchronized int WriteFileSector(String filename, long sector_num, byte[] data) {
        errcode = 0;
        if (!fileCrypto.isValidKey()) {
            errcode = ERRCODE_INVALID_CRYPT_KEY;
            _L("SplittedFile ERRCODE_INVALID_CRYPT_KEY");
            return errcode;
        }
        
        final long fileNameTail = sector_num / ONE_PART_SIZE;
        
        try {
            final FileOutputStream fos;
            if (wfiles.containsKey(fileNameTail)) {
                fos = wfiles.get(fileNameTail);
            } else {
                __checkDir();
                fos = new FileOutputStream(__getFileName(filename, sector_num), true);
                wfiles.put(fileNameTail, fos);
            }
            
            for (int i=0; i<SECTOR_SIZE; i++) if(i < data.length) buf_t[i] = data[i]; else buf_t[i] = 0;
            final byte[] bx = fileCrypto.Crypt(buf_t);
            if (bx == null) {
                errcode = ERRCODE_CRYPT_ERROR;
                _L("SplittedFile ERRCODE_CRYPT_ERROR");
                return errcode;
            }

            final ByteBuffer b = ByteBuffer.wrap(bx, 0, bx.length); 
            final FileChannel f = fos.getChannel();
            f.position(sector_num * SECTOR_SIZE);
            f.write(b);
            MyIOPS_W = MyIOPS_W + SECTOR_SIZE;
           
            return 0;
        } catch (IOException ex) {
            _L(ex.getMessage());
            errcode = ERRCODE_IO_EXCEPTION;
        }
        return errcode;
    }
    
    public synchronized byte[] ReadFileSector(String filename, long sector_numx) {
        errcode = 0;
        if (!fileCrypto.isValidKey()) {
            errcode = ERRCODE_INVALID_CRYPT_KEY;
            _L("SplittedFile ERRCODE_INVALID_CRYPT_KEY");
            return null;
        }
        
        final long fileNameTail = sector_numx / ONE_PART_SIZE;
        final long sector_num = sector_numx - (fileNameTail * ONE_PART_SIZE);
        
        try {
            final FileInputStream fis;
            if (rfiles.containsKey(fileNameTail)) {
                fis = rfiles.get(fileNameTail);
            } else {
                __checkDir();
                fis = new FileInputStream(__getFileName(filename, sector_numx));
                rfiles.put(fileNameTail, fis);
            }
            
            FileChannel fc = fis.getChannel();
            fc.position(sector_num * SECTOR_SIZE);

            ByteBuffer bb = ByteBuffer.allocate((int)SECTOR_SIZE);
            int counter = fc.read(bb);
            if (counter != SECTOR_SIZE) {
                errcode = ERRCODE_INCORRECT_SECTOR_SIZE;
                _L("SplittedFile ERRCODE_INCORRECT_SECTOR_SIZE "+counter);
                return null;
            } else {
                final byte[] ret_crypto = fileCrypto.Decrypt(bb.array());
                if (ret_crypto == null) {
                    errcode = ERRCODE_DECRYPT_ERROR;
                    _L("SplittedFile ERRCODE_DECRYPT_ERROR");
                    return null;
                }
                
                MyIOPS_R = MyIOPS_R + SECTOR_SIZE;
                return ret_crypto;
            }

        } catch (IOException ex) {
            _L(ex.getMessage());
            errcode = ERRCODE_IO_EXCEPTION;
        }
        return null;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public synchronized static byte[] getFileMD5MT(String file) {
        int readCount;
        final byte sectorBuffer[] = new byte[(int)SplittedFile.SECTOR_SIZE];
        final ByteArrayOutputStream md5ez = new ByteArrayOutputStream();
        
        try {
            final FileInputStream fis = new FileInputStream(file);
            while(true) {
                Arrays.fill(sectorBuffer, (byte)0);
                readCount = fis.read(sectorBuffer, 0, (int)SplittedFile.SECTOR_SIZE);
                MyIOPS_R = MyIOPS_R + SECTOR_SIZE;
                if (readCount == -1) break;
                md5ez.write(Crypto.MD5(sectorBuffer)); 
            }
            fis.close();
            return Crypto.MD5(md5ez.toByteArray());
        } catch (IOException ex) {
            _L(ex.getMessage());
        }
        return null;
    }
    
    public synchronized static byte[] getFileMD5MT(byte[] file, long sectorSize) {
        final ByteArrayOutputStream md5ez = new ByteArrayOutputStream();
        
        for (long i=0; i<sectorSize; i++) {
            try {
                final byte sectorBuffer[] = Arrays.copyOfRange(file, (int)(i*SplittedFile.SECTOR_SIZE), (int)((i+1)*SplittedFile.SECTOR_SIZE));
                md5ez.write(Crypto.MD5(sectorBuffer));
                MyIOPS_R = MyIOPS_R + SECTOR_SIZE;
            } catch (IOException ex) {
                Logger.getLogger(FSEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
        return Crypto.MD5(md5ez.toByteArray());
    }
    
    private void __checkDir() {
        dbdir.mkdir();
    }

    private String __getFileName(String filename, long sector_num) {
        final long fileNameTail = sector_num / ONE_PART_SIZE;
        return DATABASE_FOLDER + filename + "." + __Right("0000000" + fileNameTail, DB_FILE_EXT_LEN);
    }
    
    private String __Right(String s, int count) {
        return s.substring(s.length() - count);
    }
    
    private static void _L(String s) {
        //System.out.println(s);
        JNekoImageDB.L(s);
    }
}
