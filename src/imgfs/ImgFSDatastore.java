package imgfs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.bind.DatatypeConverter;
import jnekoimagesdb.Lang;

public class ImgFSDatastore {
    public static final int
            MINIMUM_IMAGE_SIZE = 1024;
    
    private static ImgFSCrypto
            dsCrypto;
    
    private static String
            databaseName, storeBasePath;
    
    private static File
            storeRootDirectory;
    
    private static volatile boolean 
            isNotInit = true;
    
    public static void init(ImgFSCrypto c, String dbname) throws Exception {
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
}
