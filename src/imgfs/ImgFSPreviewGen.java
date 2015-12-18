package imgfs;

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
    
    public static class PreviewSize {
        public final int 
                height, width;
        
        public final boolean
                isSquared;
        
        private final String 
                name;
        
        public PreviewSize(String s, int w, int h, boolean isSq) {
            name = s;
            width = w;
            height = h;
            isSquared = isSq;
        }
        
        @Override
        public String toString() {
            return name;
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
    
    protected class PreviewWorker implements Runnable {
        private final ConcurrentLinkedQueue<PreviewElement> 
                threadQueue = new ConcurrentLinkedQueue<>(), 
                processedQueue = new ConcurrentLinkedQueue<>();
        
        private final 
                Object syncObject;
        
        private volatile boolean 
                isExit = false,
                isWaiting = false,
                isDisplayProgress = false;
        
        private final PreviewGeneratorActionListener
                actionListenerX;
        
        private final ImgFSCrypto
                imCrypt;
        
        private final ImgFSImages
                imgConverter = new ImgFSImages();
        
        private final String 
                queneName;
        
        private String
                tempString = "";
        
        private final Connection
                myConn;

        public PreviewWorker(Object o, PreviewGeneratorActionListener al, ImgFSCrypto ic, String quene) {
            super();
            queneName       = quene;
            syncObject      = o;
            actionListenerX = al;
            imCrypt         = ic;
            if (myType == ImgFS.PreviewType.previews) {
                myConn          = ImgFS.getH2Connection();
                try {
                    myConn.setAutoCommit(false);
                } catch (SQLException ex) {
                    Logger.getLogger(ImgFSPreviewGen.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                myConn = null;
            }
        }
        
        public void setProgressDisplay(boolean b) {
            isDisplayProgress = b;
        }
        
        public boolean isWaiting() {
            return isWaiting;
        }
        
        public void exit() {
            isExit = true;
        }
        
        public void addElementToQueue(Path p) throws FileIsNotImageException, IOException {
            if (!Files.exists(p))           throw new IOException("addElementToQueue: file not found ["+p.toString()+"];");
            if (!Files.isRegularFile(p))    throw new IOException("addElementToQueue: it is not a file ["+p.toString()+"];");
            if (!Files.isReadable(p))       throw new IOException("addElementToQueue: cannot read file ["+p.toString()+"];");
            
            if (!isImage(p.toString()))     throw new FileIsNotImageException("addElementToQueue: file not an image ["+p.toString()+"];");
            
            final long fileSize = p.toFile().length();
            
            final PreviewElement pe = new PreviewElement();
            pe.setPath(p);
            pe.setLong("fileSize", fileSize); 
            pe.setLong("createTime", System.currentTimeMillis()); 
            
            threadQueue.add(pe);
        }

        @Override
        public void run() {
            while(true) {
                synchronized(syncObject) {
                    try {
                        isWaiting = true;
                        syncObject.wait();
                        isWaiting = false;
                    } catch (InterruptedException ex) { }
                }                
                
                if (isExit) {
                    return;
                }
                
                if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnStartThread(threadQueue.size(), this.hashCode()); });
                final WriteBatch threadBatch;
                synchronized(ImgFS.getDB(dbxName)) {
                    threadBatch = ImgFS.getDB(dbxName).createWriteBatch();
                }

                while (true) {
                    final PreviewElement pe = threadQueue.poll();
                    if (pe == null) {
                        try {
                            synchronized(ImgFS.getDB(dbxName)) {    
                                if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), "Writing preview to DB..."); });
                                ImgFS.getDB(dbxName).write(threadBatch, new WriteOptions().sync(true));
                                threadBatch.close();
                            }

                            if (myType == ImgFS.PreviewType.previews) {
                                if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), "Writing metadata to DB..."); });

                                try {
                                    final PreparedStatement ps = myConn.prepareStatement("INSERT INTO `images` VALUES (default, ?);");
                                    for (PreviewElement p : processedQueue) {
                                        ps.setBytes(1, p.getMD5());
                                        ps.addBatch();
                                    }
                                    ps.executeBatch();
                                    ps.clearWarnings();
                                    ps.close();
                                    myConn.commit();
                                } catch (SQLException ex) {
                                    Logger.getLogger(ImgFSPreviewGen.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                int counterA = 0;
                                final int queneCount = processedQueue.size();
                                if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), "Copying files..."); });
                                for (PreviewElement p : processedQueue) {
                                    counterA++;
                                    tempString = "Copying files "+counterA+" of "+queneCount+"...";
                                    if (counterA > 128) {
                                        if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), tempString); });
                                    }
                                    try {
                                        pushFile(p.getMD5(), p.getPath());
                                    } catch (Exception ex) {
                                        Logger.getLogger(ImgFSPreviewGen.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnComplete(this.hashCode()); });
                            break;
                        } catch (IOException ex) {
                            L("cannot close db; " + ex.getMessage());
                        }
                    }
                    
                    try {
                        final byte[] md5e = getFilePartMD5(pe.getFile().getAbsolutePath());
                        pe.setMD5(md5e);
                        final PreviewElement peDB = readEntry(imCrypt, md5e);
                        final Image im = peDB.getImage(imCrypt, prevSizes.get(prevSizesDefault).toString());
                        if (im != null) 
                            actionListenerX.OnPreviewGenerateComplete(im, peDB.getPath()); 

                    } catch (RecordNotFoundException ex) {
                        try {
                            final byte[] md5e = getFilePartMD5(pe.getFile().getAbsolutePath());
                            prevSizes.stream().forEach((c) -> {
                                try {
                                    imgConverter.setPreviewSize(c.width, c.height, c.isSquared);
                                    final byte preview[] = imgConverter.getPreviewFS(pe.getFile().getAbsolutePath());
                                    final byte previewCrypted[] = imCrypt.Crypt(preview);
                                    
                                    pe.setCryptedImageBytes(previewCrypted, c.toString());
                                } catch (IOException ex1) {
                                    L("cannot insert image from db; c=" + c.toString() + "; " + ex1.getMessage());
                                    if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnError(this.hashCode()); });
                                }    
                            });
                            
                            final Image im = pe.getImage(imCrypt, prevSizes.get(prevSizesDefault).toString());
                            if (im != null) {
                                actionListenerX.OnPreviewGenerateComplete(im, pe.getPath());
                                if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnNewItemGenerated(threadQueue.size(), pe.getPath(), this.hashCode(), queneName); });
                            } else {
                                if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnError(this.hashCode()); });
                            }

                            addEntryToBatch(threadBatch, imCrypt, md5e, pe);

                        } catch (Error e) {
                            L("cannot insert image from db; RTE; " + e.getMessage());
                            if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnError(this.hashCode()); });
                        } catch (RecordNotFoundException ex1) {
                            if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnError(this.hashCode()); });
                        } catch (IOException ex1) {
                            L("cannot insert image from db; " + ex1.getMessage());
                            if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnError(this.hashCode()); });
                        }
                    } catch (ClassNotFoundException | IOException ex) {
                        L("cannot load image from db; " + ex.getMessage());
                        if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnError(this.hashCode()); });
                    }
                }
            }
        }
        
        private void pushFile(byte[] md5, Path file) throws Exception {
            final String p = getPathString(md5);

            final byte[] nc = Files.readAllBytes(file);
            if (nc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFS: file too small;");

            final byte[] cc = imCrypt.Crypt(nc);
            if (cc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFS: strange crypt error;");

            final Path out = FileSystems.getDefault().getPath(p);
            Files.write(out, cc); 
        }
        
        @SuppressWarnings("ConvertToTryWithResources")
        private void addEntryToBatch(WriteBatch b, ImgFSCrypto c, byte[] md5b, PreviewElement e) throws IOException {
            if (e == null) throw new IOException("array is a null;");

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(e);
            oos.flush();

            final byte[] crypted = c.Crypt(baos.toByteArray());
            if (crypted == null) throw new IOException("Crypt() return null;");

            b.put(md5b, crypted);

            oos.close();
            baos.close();
            
            processedQueue.add(e);
        }

        @SuppressWarnings("ConvertToTryWithResources")
        public byte[] getFilePartMD5(String path) throws IOException {
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
                        return imCrypt.MD5(bb.array(), imCrypt.getSalt());
                    else {
                        final ByteBuffer bb_cutted = ByteBuffer.allocate(counter);
                        bb_cutted.put(bb.array(), 0, counter);
                        return imCrypt.MD5(bb_cutted.array(), imCrypt.getSalt());
                    }
                } else 
                    throw new IOException("cannot calculate MD5 for file ["+path+"]");
            } catch (IOException ex) {
                throw new IOException("cannot calculate MD5 for file ["+path+"], " + ex.getMessage());
            }
        }
        
        @SuppressWarnings("ConvertToTryWithResources")
        private boolean isImage(String path) {
            final String ext = FilenameUtils.getExtension(path);
            if (ext.length() < 2) return false;

            final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(ext);

            if (it.hasNext()) {
                final ImageReader r = it.next();
                try {
                    ImageInputStream stream = new FileImageInputStream(new File(path));
                    r.setInput(stream);
                    int width = r.getWidth(r.getMinIndex());
                    int height = r.getHeight(r.getMinIndex());
                    r.dispose();
                    stream.close();
                    if ((width > 0) && (height > 0)) return true;
                } catch (IOException e) {
                    return false;
                } finally { r.dispose(); }
            }
            return false;
        }  
    }

    private final ImgFS.PreviewType
            myType;
    
    private final ArrayList<PreviewWorker> 
            workersTreads = new ArrayList<>();
    
    private final CopyOnWriteArrayList<PreviewSize>
            prevSizes = new CopyOnWriteArrayList<>();
    
    private volatile int 
            workerBalanceCounter = 0,
            processorsCount = 0,
            prevSizesDefault = 0;
    
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
            final PreviewWorker pw = new PreviewWorker(this, actionListenerY, imCryptoY, dbxName);
            pw.setProgressDisplay(isDisplayProgress);
            workersTreads.add(pw);
            new Thread(pw).start();
            if (isDisplayProgress) ImgFS.getProgressListener().OnCreated(pw.hashCode());
        }
    }
    
    public void startJob() {
        if (prevSizes.isEmpty()) return;
        
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
        return workersTreads.stream().anyMatch((p) -> (p.isWaiting));
    }
    
    public synchronized void addFileToJob(Path p) throws FileIsNotImageException, IOException {
        workersTreads.get(workerBalanceCounter).addElementToQueue(p);
        workerBalanceCounter++;
        if (workerBalanceCounter >= processorsCount) workerBalanceCounter = 0;
    }

    private synchronized PreviewElement readEntry(ImgFSCrypto c, byte[] md5b) throws RecordNotFoundException, IOException, ClassNotFoundException {
        if (ImgFS.getDB(dbxName) == null) throw new IOException("database not opened;");
        
        final byte[] retnc = ImgFS.getDB(dbxName).get(md5b);
        if (retnc == null ) throw new RecordNotFoundException("");
        
        final byte[] ret = c.Decrypt(retnc);
        if (ret == null ) throw new IOException("Decrypt() return null value;");
        
        final ByteArrayInputStream bais = new ByteArrayInputStream(ret);
        final ObjectInputStream oos = new ObjectInputStream(bais);
        final PreviewElement retVal = (PreviewElement) oos.readObject();
        if (retVal == null ) throw new IOException("readObject() return null value;");
        
        return retVal;
    }
    
    private String getPathString(byte[] md5) throws IOException {
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

    public void addPreviewSize(String name, int w, int h, boolean squared) {
        prevSizes.add(new PreviewSize(name, w, h, squared));
    }
    
    public ImgFSPreviewGen(ImgFSCrypto c, ImgFS.PreviewType pt, String dbName, PreviewGeneratorActionListener al) {
        imCryptoY = c;
        actionListenerY = al;
        myType = pt;
        
        prevSizes.clear();
        switch (myType) {
            case cahce:
                dbxName = ImgFS.PreviewType.cahce.name();
                processorsCount = Runtime.getRuntime().availableProcessors();
                if (processorsCount > 4) processorsCount = 4;
                prevSizes.add(new PreviewSize("p120x120", 120, 120, false));
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
