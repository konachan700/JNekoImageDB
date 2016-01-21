package img;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

public class XImgPreviewGen {   
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
        
        public Image getImage(XImgCrypto crypt) {
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
        
        public Image getImage(XImgCrypto crypt, String name) {
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
    
    private final XImg.PreviewType
            myType;
    
    private final ArrayList<XImgPreviewWorker> 
            workersTreads = new ArrayList<>();

    private volatile int 
            workerBalanceCounter = 0,
            processorsCount = 0;
    
    private final PreviewGeneratorActionListener
            actionListenerY;
    
    private final XImgCrypto
            imCryptoY;
    
    private final String
            dbxName, storeBasePath;
    
    private final File
            storeRootDirectory;
    
    public void init(boolean isDisplayProgress) throws IOException {       
        for (int i=0; i<processorsCount; i++) {
            final XImgPreviewWorker pw = new XImgPreviewWorker(this, actionListenerY, imCryptoY, dbxName, myType);
            pw.setProgressDisplay(isDisplayProgress);
            workersTreads.add(pw);
            new Thread(pw).start();
            if (isDisplayProgress) XImg.getProgressListener().OnCreated(pw.hashCode());
        }
    }
    
    public void startJob() {
        if (XImg.getPSizes().isPreviewSizesEmpty()) return;
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) return;
        
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

    public XImgPreviewGen(XImgCrypto c, XImg.PreviewType pt, String dbName, PreviewGeneratorActionListener al) {
        imCryptoY = c;
        actionListenerY = al;
        myType = pt;
        
        switch (myType) {
            case cache:
                dbxName = XImg.PreviewType.cache.name();
                processorsCount = Runtime.getRuntime().availableProcessors();
                if (processorsCount > 4) processorsCount = 4;
                break;
            case previews:
                processorsCount = Runtime.getRuntime().availableProcessors();
                if (processorsCount > 16) processorsCount = 16;
                dbxName = XImg.PreviewType.previews.name();
                break;
            default:
                dbxName = "unknown";
        }
        
        storeBasePath = "." + File.separator + dbName + File.separator + "store";
        storeRootDirectory = new File(storeBasePath);
        storeRootDirectory.mkdirs();
        
        XImg.initIDB(dbxName);
    }

    public static final void L(String s) {
        System.out.println("Time: "+System.currentTimeMillis()+"; Message: "+s);
    }
}
