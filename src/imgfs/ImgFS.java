package imgfs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.image.Image;
import jnekoimagesdb.JNekoImageDB;

public class ImgFS {
    public static interface PreviewGeneratorActionListener {
        public void OnPreviewGenerateComplete(Image im, Path path);
    }
    
    public static class FileIsNotImageException extends IOException {
        public FileIsNotImageException(String s) {
            super(s);
        }
    }
    
    private final File 
            myPath = new File("."),
            databaseFileForFSPreviews,
            databaseFileForFullImages,
            databaseH2File,
            randomPool,
            masterKey;
    
    private final String
            dbName;
    
    private final ImgFSH2
            database = new ImgFSH2();
    
    private final ImgFSCrypto
            cryptModule;
    
    private FileOutputStream
            mainDBW;
//            fsPreviewDBW;
    
    public static final long
            DATABASE_SECTOR_SIZE = 1024 * 8;
    
    public static final int
           QUERY_COUNT_IN_BATCH = 250;
    
    public final Map<Integer, ConcurrentLinkedQueue<ImgFSRecord>>
            currentJobFSPrev = new HashMap<>(),
            currentJobTemporaryFSPrev = new HashMap<>();
    
    private ImgFSRecord 
            lastJobElement = null;
    
    private volatile int 
            currentJobFSPrevCounter = 0,
            currentGlobalLastSectorForFSPreview = 0;
    
    private final ArrayList<ImgFSPreviewsFSReporter> 
            actionListener = new ArrayList<>();
    
    private final ArrayList<PreviewGeneratorActionListener> 
            previewAL = new ArrayList<>();
    
    public ImgFS(String dbname) {
        dbName = dbname;
        
        databaseH2File            = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db");
        databaseFileForFSPreviews = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db." + ImgFSRecord.FS_PREVIEW);
        databaseFileForFullImages = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db." + ImgFSRecord.FULL_IMAGE);
        randomPool                = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db.R");
        masterKey                 = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db.M");
        
        cryptModule = new ImgFSCrypto();
        ImgFSImages.setPreviewCompleteListener(previewAL);
    }
    
    public void addPreviewActionListener(PreviewGeneratorActionListener al) {
        previewAL.add(al);
    }
    
    public void removePreviewActionListener(PreviewGeneratorActionListener al) {
        previewAL.remove(al);
    }
    
    public File getRandomPoolFile() {
        return randomPool;
    }
    
    public File getMasterKeyFile() {
        return masterKey;
    }
    
    public void init() throws IOException {
        cryptModule.genSecureRandomSalt(randomPool);
        cryptModule.genMasterKey(masterKey);
        cryptModule.genMasterKeyAES();
        
        final File myFolder = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator);
        if (!myFolder.mkdir())
            if (!myFolder.exists()) throw new IOException("init: cannot create db folder ["+dbName+"];");
        
        mainDBW = new FileOutputStream(databaseFileForFullImages, true);
//        fsPreviewDBW = new FileOutputStream(databaseFileForFSPreviews, true);
            
        try {
            database.h2DatabaseConnect(databaseH2File.getAbsolutePath(), cryptModule.getPasswordFromMasterKey());
        } catch (SQLException ex) {
            throw new IOException("init: cannot connect to database ["+dbName+"], " + ex.getMessage());
        }
        
        final int processorsCount = Runtime.getRuntime().availableProcessors();
        for (int i=0; i<processorsCount; i++) {
            currentJobFSPrev.put(i, new ConcurrentLinkedQueue<>());
            currentJobTemporaryFSPrev.put(i, new ConcurrentLinkedQueue<>());
        }
        
        for (int i=0; i<processorsCount; i++) {
            final ImgFSRunnable thread = new ImgFSRunnable(this, ImgFSRecord.FS_PREVIEW, i);
            final Thread th = new Thread(thread);
            th.setDaemon(true);
            th.start();
        }
    }
    
    public void close() {
        try {
            database.h2Close();
            mainDBW.close();
//            fsPreviewDBW.close();
        } catch (IOException ex) {
            Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void addFileToJob(String path, int type) throws FileIsNotImageException, IOException {
        if (!database.isConnected())
            throw new IOException("addFileToJob: database not connected!");
        
        if (!currentJobFSPrev.containsKey(type))
            throw new IOException("addFileToJob: type ["+type+"] not exist!");
        
        final File jobFile = new File(path);
        if (!jobFile.exists())      throw new IOException("addFileToJob: file ["+path+"] not exist!");
        if (!jobFile.isFile())      throw new IOException("addFileToJob: file ["+path+"] not regular file!");
        if (!jobFile.canRead())     throw new IOException("addFileToJob: file ["+path+"] not readable!");
        if (jobFile.length() <= 0)  throw new IOException("addFileToJob: file ["+path+"] has null size!");
        
        if (!ImgFSImages.isImage(jobFile.getAbsolutePath()))
            throw new FileIsNotImageException("addFileToJob: file ["+path+"] is not image!");
        
        final byte[] fileMD5 = ImgFSFileOps.getFilePartMD5(jobFile.getAbsolutePath());
        
        //final long startSector = (lastJobElement == null) ? (database.getLastSectorForFSPreviews() + 1) : (lastJobElement.getEndSector() + 1);
        //final long sectorSize = getSectorSize(jobFile.length());
        //final long endSector = startSector + sectorSize;
        
//        _L("startSector="+startSector);
        
        
        switch (type) {
            case ImgFSRecord.FS_PREVIEW:
                final ImgFSRecord jobElement = new ImgFSRecord(0, 0, 0, 0, fileMD5, type, jobFile);
                
                if (database.isMD5NotPresentInFSPreviews(fileMD5)) {
                    final ImgFSRecord fsElement = database.getFSPreviewsRecord(fileMD5);
                    if (fsElement != null) {
                        final Image img = readFSPreviewRecord(fsElement);
                        if (img == null) throw new IOException("null image");
                        for (ImgFS.PreviewGeneratorActionListener al : previewAL) {
                            final Path p = FileSystems.getDefault().getPath(jobFile.getAbsolutePath());
                            al.OnPreviewGenerateComplete(img, p); 
                            //_L("al.OnPreviewGenerateComplete "+p.toString());
                        }
                        return;
                    } else {
                        throw new IOException("null image element");
                    }
                }
                
                currentJobFSPrev.get(currentJobFSPrevCounter).add(jobElement);
                lastJobElement = jobElement;
                currentJobFSPrevCounter++;
                if (currentJobFSPrevCounter >= currentJobFSPrev.size()) currentJobFSPrevCounter = 0;
                break;
            case ImgFSRecord.FULL_IMAGE:
                
                
                break;
            default:
                break;
        }
    }
    
    protected ImgFSRecord getNextFSPreviewRecord(int threadID) {
        return currentJobFSPrev.get(threadID).poll();
    }
    
    public void addActionListener(ImgFSPreviewsFSReporter al) {
        actionListener.add(al);
    }
    
    public void removeActionListener(ImgFSPreviewsFSReporter al) {
        actionListener.remove(al);
    }
    
    public void progressOk(int threadID, int type) {
        Platform.runLater(() -> {
            actionListener.stream().forEach((al) -> {
                al.OnAllItemsInThreadReady(threadID, type);
            }); 
        });
    }
    
    public void progressInfo(int threadID, ImgFSRecord currRecord) {
        Platform.runLater(() -> {
            actionListener.stream().forEach((al) -> {
                al.OnItemReady(lastJobElement, threadID);
            });
        });
    }
    
    protected synchronized void insertFSPreviewsToDB(int threadID, boolean immed) {
        if ((currentJobTemporaryFSPrev.size() > QUERY_COUNT_IN_BATCH) || (immed == true)) {
            final ArrayList<ImgFSRecord> tempList = new ArrayList<>();
            while (true) {
                final ImgFSRecord element = currentJobTemporaryFSPrev.get(threadID).poll();
                if (element != null) tempList.add(element); else break;
            }
            //_L("tempList="+tempList.size());
            
            try {
                database.writeRecords(tempList);
            } catch (IOException ex) {
                Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void commitJob() {
        currentGlobalLastSectorForFSPreview = (int)(database.getLastSectorForFSPreviews() + 1);
        this.notifyAll();
    }

    @SuppressWarnings("ConvertToTryWithResources")
    protected void writeFSPreviewRecord(ImgFSRecord fsElement, int threadID) throws IOException {
        final FileOutputStream fos = new FileOutputStream(databaseFileForFSPreviews, true);
        final FileChannel fc = fos.getChannel();

        final byte preview[] = ImgFSImages.getPreviewFS(fsElement.getFile().getAbsolutePath());
        fsElement.setStartSector(currentGlobalLastSectorForFSPreview);
        fsElement.setActualSize(preview.length);
        fsElement.setSectorSize((preview.length / DATABASE_SECTOR_SIZE) + (((preview.length % DATABASE_SECTOR_SIZE) == 0) ? 0 : 1)); 
        fsElement.setEndSector(fsElement.getSectorSize() + fsElement.getStartSector()); 
        
        final byte previewNonCrypted[] = new byte[(int)(fsElement.getSectorSize()*DATABASE_SECTOR_SIZE)];
        Arrays.fill(previewNonCrypted, (byte) 0);
        System.arraycopy(preview, 0, previewNonCrypted, 0, preview.length);

        currentGlobalLastSectorForFSPreview += fsElement.getSectorSize();
        final byte previewImg[] = cryptModule.Crypt(cryptModule.align16b(previewNonCrypted));
        final ByteBuffer bbOut = ByteBuffer.wrap(previewImg);
        
        fc.position(fsElement.getStartSector() * DATABASE_SECTOR_SIZE);
        fc.write(bbOut);
        currentJobTemporaryFSPrev.get(threadID).add(fsElement);

        fc.close();
        fos.close();
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    protected Image readFSPreviewRecord(ImgFSRecord fsElement) throws IOException {
        //_L("READ "+fsElement.toString());
        
        final FileInputStream fis = new FileInputStream(databaseFileForFSPreviews);
        final FileChannel fc = fis.getChannel();
        ByteBuffer bbIn = ByteBuffer.allocate((int)(fsElement.getSectorSize() * DATABASE_SECTOR_SIZE));
        fc.position(fsElement.getStartSector() * DATABASE_SECTOR_SIZE);
        fc.read(bbIn);
        final byte uncrypt[] = cryptModule.Decrypt(bbIn.array());
        //final byte cutted[] = Arrays.copyOfRange(uncrypt, 0, (int)fsElement.getActualSize());
        final Image img = new Image(new ByteArrayInputStream(uncrypt));
        
        fc.close();
        fis.close();
        
        return img;
    }

    private long getSectorSize(long actualSize) {
        long tail = actualSize % DATABASE_SECTOR_SIZE;
        if (tail == 0) return (actualSize / DATABASE_SECTOR_SIZE); else return ((actualSize / DATABASE_SECTOR_SIZE) + 1); 
    }
    
    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s);
    }
}
