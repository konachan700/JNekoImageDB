package imgfs;

import datasources.DSImage;
import datasources.HibernateUtil;
import static imgfs.ImgFSPreviewGen.FILE_PART_SIZE_FOR_CHECKING_MD5;
import static imgfs.ImgFSPreviewGen.L;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.hibernate.Session;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;

public class ImgFSPreviewWorker implements Runnable {
    private final ConcurrentLinkedQueue<ImgFSPreviewGen.PreviewElement> 
            threadQueue = new ConcurrentLinkedQueue<>(), 
            processedQueue = new ConcurrentLinkedQueue<>();

    private final 
            Object syncObject;

    private final CopyOnWriteArrayList<ImgFSPreviewGen.PreviewSize>
            prevSizes;  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ДОДЕЛАТЬ !!!!!!!!!!!!!

    private volatile boolean 
            isExit = false,
            isWaiting = false,
            isDisplayProgress = false;
    
    private volatile int
            prevSizesDefault = 0; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ДОДЕЛАТЬ !!!!!!!!!!!!!

    private final ImgFSPreviewGen.PreviewGeneratorActionListener
            actionListenerX;

    private final ImgFSCrypto
            imCrypt;

    private final ImgFSImages
            imgConverter = new ImgFSImages();

    private final String 
            queneName;

    private String
            tempString = "";

    private Session 
            hibSession = null;

    private final ImgFS.PreviewType
            myType;

    public ImgFSPreviewWorker(Object o, ImgFSPreviewGen.PreviewGeneratorActionListener al, 
            ImgFSCrypto ic, String quene, ImgFS.PreviewType t, CopyOnWriteArrayList<ImgFSPreviewGen.PreviewSize> ps) {
        super();
        queneName       = quene;
        syncObject      = o;
        actionListenerX = al;
        imCrypt         = ic;
        myType          = t;
        prevSizes       = ps;
    }

    public void setProgressDisplay(boolean b) {
        isDisplayProgress = b;
    }

    public boolean isWaitingThread() {
        return isWaiting;
    }

    public void exit() {
        isExit = true;
    }

    public void addElementToQueue(Path p) throws ImgFSPreviewGen.FileIsNotImageException, IOException {
        if (!Files.exists(p))           throw new IOException("addElementToQueue: file not found ["+p.toString()+"];");
        if (!Files.isRegularFile(p))    throw new IOException("addElementToQueue: it is not a file ["+p.toString()+"];");
        if (!Files.isReadable(p))       throw new IOException("addElementToQueue: cannot read file ["+p.toString()+"];");

        if (!imgConverter.isImage(p.toString()))     throw new ImgFSPreviewGen.FileIsNotImageException("addElementToQueue: file not an image ["+p.toString()+"];");

        final long fileSize = p.toFile().length();

        final ImgFSPreviewGen.PreviewElement pe = new ImgFSPreviewGen.PreviewElement();
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
            synchronized(ImgFS.getDB(queneName)) {
                threadBatch = ImgFS.getDB(queneName).createWriteBatch();
            }

            while (true) {
                final ImgFSPreviewGen.PreviewElement pe = threadQueue.poll();
                if (pe == null) {
                    try {
                        synchronized(ImgFS.getDB(queneName)) {    
                            if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), "Writing preview to DB..."); });
                            ImgFS.getDB(queneName).write(threadBatch, new WriteOptions().sync(true));
                            threadBatch.close();
                        }

                        if (myType == ImgFS.PreviewType.previews) {
                            if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), "Writing metadata to DB..."); });

                            hibSession = HibernateUtil.getNewSession();
                            HibernateUtil.beginTransaction(hibSession);
                            processedQueue.stream().map((p) -> new DSImage(p.getMD5())).forEach((di) -> {
                                hibSession.save(di);
                            });
                            HibernateUtil.commitTransaction(hibSession);
                            hibSession.close();

                            int counterA = 0;
                            final int queneCount = processedQueue.size();
                            if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), "Copying files..."); });
                            for (ImgFSPreviewGen.PreviewElement p : processedQueue) {
                                counterA++;
                                tempString = "Copying files "+counterA+" of "+queneCount+"...";
                                if (counterA > 128) {
                                    if (isDisplayProgress) Platform.runLater(() -> { ImgFS.getProgressListener().OnInfoUpdate(this.hashCode(), tempString); });
                                }
                                try {
                                    ImgFSDatastore.pushFile(p.getMD5(), p.getPath());
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
                    final ImgFSPreviewGen.PreviewElement peDB = readEntry(imCrypt, md5e);
                    final Image im = peDB.getImage(imCrypt, prevSizes.get(prevSizesDefault).toString());
                    if (im != null) 
                        actionListenerX.OnPreviewGenerateComplete(im, peDB.getPath()); 

                } catch (ImgFSPreviewGen.RecordNotFoundException ex) {
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
                    } catch (ImgFSPreviewGen.RecordNotFoundException ex1) {
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

    @SuppressWarnings("ConvertToTryWithResources")
    private void addEntryToBatch(WriteBatch b, ImgFSCrypto c, byte[] md5b, ImgFSPreviewGen.PreviewElement e) throws IOException {
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

    private synchronized ImgFSPreviewGen.PreviewElement readEntry(ImgFSCrypto c, byte[] md5b) throws ImgFSPreviewGen.RecordNotFoundException, IOException, ClassNotFoundException {
        if (ImgFS.getDB(queneName) == null) throw new IOException("database not opened;");
        
        final byte[] retnc = ImgFS.getDB(queneName).get(md5b);
        if (retnc == null ) throw new ImgFSPreviewGen.RecordNotFoundException("");
        
        final byte[] ret = c.Decrypt(retnc);
        if (ret == null ) throw new IOException("Decrypt() return null value;");
        
        final ByteArrayInputStream bais = new ByteArrayInputStream(ret);
        final ObjectInputStream oos = new ObjectInputStream(bais);
        final ImgFSPreviewGen.PreviewElement retVal = (ImgFSPreviewGen.PreviewElement) oos.readObject();
        if (retVal == null ) throw new IOException("readObject() return null value;");
        
        return retVal;
    }
}
