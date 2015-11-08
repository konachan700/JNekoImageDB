package imgfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import jnekoimagesdb.JNekoImageDB;

public class ImgFS {
    private final File 
            myPath          = new File("."),
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
            mainDBW,
            fsPreviewDBW;
    
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
            currentJobFSPrevCounter = 0;
    
    private final ArrayList<ImgFSPreviewsFSReporter> 
            actionListener = new ArrayList<>();
    
    public ImgFS(String dbname) {
        dbName = dbname;
        
        databaseH2File            = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db.H");
        databaseFileForFSPreviews = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db." + ImgFSRecord.FS_PREVIEW);
        databaseFileForFullImages = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db." + ImgFSRecord.FULL_IMAGE);
        randomPool                = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db.R");
        masterKey                 = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db.M");
        
        cryptModule = new ImgFSCrypto();
        cryptModule.genSecureRandomSalt(randomPool);
        cryptModule.genMasterKey(masterKey);
        cryptModule.genMasterKeyAES();
    }
    
    public File getRandomPoolFile() {
        return randomPool;
    }
    
    public File getMasterKeyFile() {
        return masterKey;
    }
    
    public void init() throws IOException {
        if (!new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator).mkdir())
            throw new IOException("init: cannot create db folder ["+dbName+"];");
        
        mainDBW = new FileOutputStream(databaseFileForFullImages, true);
        fsPreviewDBW = new FileOutputStream(databaseFileForFSPreviews, true);
            
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
            fsPreviewDBW.close();
        } catch (IOException ex) {
            Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void addFileToJob(String path, int type) throws IOException {
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
            throw new IOException("addFileToJob: file ["+path+"] is not image!");
        
        final byte[] fileMD5 = ImgFSFileOps.getFilePartMD5(jobFile.getAbsolutePath());
        
        final long startSector = (lastJobElement == null) ? (database.getLastSectorForFSPreviews() + 1) : (lastJobElement.getEndSector() + 1);
        final long sectorSize = getSectorSize(jobFile.length());
        final long endSector = startSector + sectorSize;
        
        final ImgFSRecord jobElement = new ImgFSRecord(startSector, endSector, sectorSize, jobFile.length(), fileMD5, type, jobFile);
        switch (type) {
            case ImgFSRecord.FS_PREVIEW:
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
        if ((currentJobFSPrev.size() > QUERY_COUNT_IN_BATCH) || (immed == true)) {
            final ArrayList<ImgFSRecord> tempList = new ArrayList<>();
            while (true) {
                final ImgFSRecord element = currentJobFSPrev.get(threadID).poll();
                if (element != null) tempList.add(element); else break;
            }
            try {
                database.writeRecords(tempList);
            } catch (IOException ex) {
                Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void commitJob() {
        this.notifyAll();
    }

    @SuppressWarnings("ConvertToTryWithResources")
    protected void writeFSPreviewRecord(ImgFSRecord fsElement, int threadID) throws IOException {
        try {
            final FileChannel fc = fsPreviewDBW.getChannel();
            try {
                final byte previewImg[] = cryptModule.Crypt(ImgFSImages.getPreviewFS(fsElement.getFile().getAbsolutePath()));
                final ByteBuffer bbOut = ByteBuffer.wrap(previewImg);
                fc.position(fsElement.getStartSector() * DATABASE_SECTOR_SIZE);
                fc.write(bbOut);
                currentJobTemporaryFSPrev.get(threadID).add(fsElement);
            } catch (IOException ex) {
                fc.close();
                throw new IOException("error while write preview ["+fsElement.getFile().getAbsolutePath()+"] do DB, " + ex.getMessage());
            }

            fc.close();
        } catch (IOException ex) {
            throw new IOException("error while write preview ["+fsElement.getFile().getAbsolutePath()+"] do DB, " + ex.getMessage());
        }
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
