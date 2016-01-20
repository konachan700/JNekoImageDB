package imgfs;

import datasources.DSImage;
import datasources.HibernateUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.Session;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;

public class ImgFSPreviewGen {   
    public static final int
            FILE_PART_SIZE_FOR_CHECKING_MD5 = 1024 * 32,
            MINIMUM_IMAGE_SIZE = 1024;
    
    private static final String 
            FIXED_FIELD_PATH = "__path";
    
    public static interface PreviewGeneratorActionListener {
        public void OnPreviewGenerateComplete(Image im, Path path);
    }
    
    public static interface PreviewGeneratorProgressListener {
        public void OnStartThread(int itemsCount, int tID);
        public void OnNewItemGenerated(int itemsCount, Path p, int tID, String quene);
        public void OnError(int tID);
        public void OnComplete(int tID);
        public void OnCreated(int tID);
        public void OnInfoUpdate(int tID, String info);
    }
    
    public static class FileIsNotImageException extends IOException {
        public FileIsNotImageException(String s) {
            super(s);
        }
    }
    
    public static class RecordNotFoundException extends IOException {
        public RecordNotFoundException(String s) {
            super(s);
        }
    }
    
//    public static class PreviewSize {
//        public final int 
//                height, width;
//        
//        public final boolean
//                isSquared;
//        
//        private final String 
//                name;
//        
//        public PreviewSize(String s, int w, int h, boolean isSq) {
//            name = s;
//            width = w;
//            height = h;
//            isSquared = isSq;
//        }
//        
//        @Override
//        public String toString() {
//            return name;
//        }
//    }
    
    public static class BinaryImage implements Serializable {
        private byte[] cryptedImage = null;
        
        public BinaryImage() { }
        
        public BinaryImage(byte[] img) {
            cryptedImage = img;
        }
        
        public byte[] getBytes() {
            return cryptedImage;
        }
        
        public void setBytes(byte[] b) {
            cryptedImage = b;
        }
        
        public Image getImage(ImgFSCrypto crypt) {
            if (cryptedImage == null) return null;
            
            final byte[] decrypted = crypt.Decrypt(cryptedImage);
            if (decrypted == null) return null;
            
            final Image img = new Image(new ByteArrayInputStream(decrypted)); 
            return img;
        }
    }
    
    public static class PreviewElement implements Serializable {
        private final Map<String, String>       stringValues    = new HashMap<>();
        private final Map<String, Long>         longValues      = new HashMap<>();
        private final Map<String, BinaryImage>  previews        = new HashMap<>();
        private byte[] elementMD5 = null;

        public void setMD5(byte[] b) {
            elementMD5 = b;
        }
        
        public byte[] getMD5() {
            return elementMD5;
        }
        
        public byte[] getCryptedImageBytes(String name) {
            final BinaryImage b = previews.get(name);
            if (b == null) return null; 
            return b.getBytes();
        }
        
        public void setCryptedImageBytes(byte[] b, String name) {
            previews.put(name, new BinaryImage(b));
        }
        
        public Image getImage(ImgFSCrypto crypt, String name) {
            final BinaryImage b = previews.get(name);
            if (b == null) return null; 
            return b.getImage(crypt); 
        }
        
        public void setPath(Path p) {
            stringValues.put(FIXED_FIELD_PATH, p.toString());
        }
        
        public Path getPath() {
            return FileSystems.getDefault().getPath(stringValues.get(FIXED_FIELD_PATH)); 
        }
        
        public File getFile() {
            return getPath().toFile();
        }
        
        public String getFileName() {
            return getPath().getFileName().toString();
        }
        
        public void setLong(String name, long value) {
            longValues.put(name, value);
        }
        
        public long getLong(String name, long defaultVal) {
            if (longValues.containsKey(name)) return longValues.get(name); else return defaultVal;
        }
        
        public void setString(String name, String value) {
            if (name.startsWith("__")) return;
            stringValues.put(name, value);
        }
        
        public String getString(String name, String defaultVal) {
            if (name.startsWith("__")) return defaultVal;
            if (stringValues.containsKey(name)) return stringValues.get(name); else return defaultVal;
        }
    }
    
    private final ImgFS.PreviewType
            myType;
    
    private final ArrayList<ImgFSPreviewWorker> 
            workersTreads = new ArrayList<>();
    
//    private final CopyOnWriteArrayList<PreviewSize>
//            prevSizes = new CopyOnWriteArrayList<>();
    
    private volatile int 
            workerBalanceCounter = 0,
            processorsCount = 0;
    
    private final PreviewGeneratorActionListener
            actionListenerY;
    
    private final ImgFSCrypto
            imCryptoY;
    
    private final String
            dbxName, storeBasePath;
    
    private final File
            storeRootDirectory;
    
    public void init(boolean isDisplayProgress) throws IOException {       
        for (int i=0; i<processorsCount; i++) {
            final ImgFSPreviewWorker pw = new ImgFSPreviewWorker(this, actionListenerY, imCryptoY, dbxName, myType);
            pw.setProgressDisplay(isDisplayProgress);
            workersTreads.add(pw);
            new Thread(pw).start();
            if (isDisplayProgress) ImgFS.getProgressListener().OnCreated(pw.hashCode());
        }
    }
    
    public void startJob() {
        if (ImgFS.getPSizes().isPreviewSizesEmpty()) return;
        if (ImgFS.getPSizes().getPrimaryPreviewSize() == null) return;
        
        synchronized (this){
            this.notifyAll();
        }
    }
    
    public void killAll() {
        workersTreads.stream().forEach((p) -> {
            p.exit();
        });
        
        synchronized (this){
            this.notifyAll();
        }
    }
    
    public boolean isAnyWorkersActive() {
        return workersTreads.stream().anyMatch((p) -> (p.isWaitingThread()));
    }
    
    public synchronized void addFileToJob(Path p) throws FileIsNotImageException, IOException {
        workersTreads.get(workerBalanceCounter).addElementToQueue(p);
        workerBalanceCounter++;
        if (workerBalanceCounter >= processorsCount) workerBalanceCounter = 0;
    }

//    public void addPreviewSize(String name, int w, int h, boolean squared) {
//        prevSizes.add(new PreviewSize(name, w, h, squared));
//    }
    
    public ImgFSPreviewGen(ImgFSCrypto c, ImgFS.PreviewType pt, String dbName, PreviewGeneratorActionListener al) {
        imCryptoY = c;
        actionListenerY = al;
        myType = pt;
        
//        prevSizes.clear();
        switch (myType) {
            case cache:
                dbxName = ImgFS.PreviewType.cache.name();
                processorsCount = Runtime.getRuntime().availableProcessors();
                if (processorsCount > 4) processorsCount = 4;
//                prevSizes.add(new PreviewSize("p120x120", 120, 120, false));
                break;
            case previews:
                processorsCount = Runtime.getRuntime().availableProcessors();
                if (processorsCount > 16) processorsCount = 16;
                dbxName = ImgFS.PreviewType.previews.name();
                break;
            default:
                dbxName = "unknown";
        }
        
        storeBasePath = "." + File.separator + dbName + File.separator + "store";
        storeRootDirectory = new File(storeBasePath);
        storeRootDirectory.mkdirs();
        
        ImgFS.initIDB(dbxName);
    }

    public static final void L(String s) {
        System.out.println("Time: "+System.currentTimeMillis()+"; Message: "+s);
    }
}
