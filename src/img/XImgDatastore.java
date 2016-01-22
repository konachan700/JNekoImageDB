package img;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.image.Image;
import javax.xml.bind.DatatypeConverter;
import jnekoimagesdb.Lang;
import org.iq80.leveldb.DB;

public class XImgDatastore {
    public static final int
            FILE_PART_SIZE_FOR_CHECKING_MD5 = 1024 * 32,
            MINIMUM_IMAGE_SIZE = 1024;
    
    private static XImgCrypto
            dsCrypto;
    
    private static String
            databaseName, storeBasePath;
    
    private static File
            storeRootDirectory;
    
    private static volatile boolean 
            isNotInit = true;
    
    public static void init(XImgCrypto c, String dbname) throws Exception {
        dsCrypto = c;
        databaseName = dbname;
        storeBasePath = "." + File.separator + databaseName + File.separator + "store";
        storeRootDirectory = new File(storeBasePath);
        isNotInit = false;
    }
    
    public static String getDBName() {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        return databaseName;
    }

    public static void removeFile(byte[] md5) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final String p = getPathString(md5);
        final Path out = FileSystems.getDefault().getPath(p);
        Files.delete(out); 
    }
    
    public static void saveFile(byte[] md5, Path savePath) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final byte[] out = getFile(md5);
        Files.write(savePath, out);
    }
    
    public static Image getImage(byte[] md5) throws IOException {
        final byte[] b = getFile(md5);
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);
        return new Image(bais);
    }
    
    public static byte[] getFile(byte[] md5) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final String p = getPathString(md5);
        final Path out = FileSystems.getDefault().getPath(p);
        
        final byte[] nc = Files.readAllBytes(out);
        if (nc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: file too small;");
        
        final byte[] cc = dsCrypto.Decrypt(nc);
        if (cc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: strange crypt error;");
        
        return cc;
    }

    public static void pushFile(byte[] md5, Path file) throws Exception {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final String p = getPathString(md5);
        
        final byte[] nc = Files.readAllBytes(file);
        if (nc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: file too small;");
        
        final byte[] cc = dsCrypto.Crypt(nc);
        if (cc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: strange crypt error;");
        
        final Path out = FileSystems.getDefault().getPath(p);
        Files.write(out, cc); 
    }
    
    public static String getPathString(byte[] md5) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        if (md5.length != 16) throw new IOException("ImgFSDatastore: Input data is not correct;");
        
        if (!storeRootDirectory.mkdirs())
            if (!storeRootDirectory.exists()) throw new IOException("ImgFSDatastore: Cannot create root directory;");
        
        final String md5s = DatatypeConverter.printHexBinary(md5).toLowerCase();
        final String dir = storeBasePath + File.separator + md5s.substring(0, 2) + File.separator + md5s.substring(2, 4);
        final File tdir = new File(dir);
        if (!tdir.mkdirs())
            if (!tdir.exists()) throw new IOException("ImgFSDatastore: Cannot create end directory;");
                
        final String path = dir + File.separator + md5s.substring(4);
        return path;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public static byte[] getFilePartMD5(String path) throws IOException {
        try {
            final FileInputStream fis = new FileInputStream(path);
            FileChannel fc = fis.getChannel();
            fc.position(0);
            final ByteBuffer bb = ByteBuffer.allocate(FILE_PART_SIZE_FOR_CHECKING_MD5);
            int counter = fc.read(bb);
            fc.close();
            fis.close();
            if (counter > 0) {
                if (counter == FILE_PART_SIZE_FOR_CHECKING_MD5) 
                    return dsCrypto.MD5(bb.array(), dsCrypto.getSalt());
                else {
                    final ByteBuffer bb_cutted = ByteBuffer.allocate(counter);
                    bb_cutted.put(bb.array(), 0, counter);
                    return dsCrypto.MD5(bb_cutted.array(), dsCrypto.getSalt());
                }
            } else 
                throw new IOException("cannot calculate MD5 for file ["+path+"]");
        } catch (IOException ex) {
            throw new IOException("cannot calculate MD5 for file ["+path+"], " + ex.getMessage());
        }
    }
    
    public static XImgPreviewGen.PreviewElement readCacheEntry(byte[] md5b) throws XImgPreviewGen.RecordNotFoundException, IOException, ClassNotFoundException {
        if (XImg.getDB(XImg.PreviewType.cache.name()) == null) throw new IOException("database not opened;");
        
        final DB db = XImg.getDB(XImg.PreviewType.cache.name());
        byte[] retnc;
        synchronized (db) {
            retnc = db.get(md5b);
            if (retnc == null ) throw new XImgPreviewGen.RecordNotFoundException("");
        }
        
        final byte[] ret = dsCrypto.Decrypt(retnc);
        if (ret == null ) throw new IOException("Decrypt() return null value;");
        
        final ByteArrayInputStream bais = new ByteArrayInputStream(ret);
        final ObjectInputStream oos = new ObjectInputStream(bais);
        final XImgPreviewGen.PreviewElement retVal = (XImgPreviewGen.PreviewElement) oos.readObject();
        if (retVal == null ) throw new IOException("readObject() return null value;");
        
        return retVal;
    }
}
