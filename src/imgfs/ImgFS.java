package imgfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import jnekoimagesdb.JNekoImageDB;
import org.apache.commons.io.FilenameUtils;

public class ImgFS {
    private final File 
            myPath          = new File("."),
            databaseFileForFSPreviews,
            databaseFileForFullImages,
            databaseH2File;
    
    private final String
            dbName;
    
    private final ImgFSH2
            database = new ImgFSH2();
    
    private final ImgFSCrypto
            cryptModule;
    
    public static final long
            DATABASE_SECTOR_SIZE = 1024 * 8;
    
    private long
            IO_R = 0;
    
    public static final int
            FILE_PART_SIZE_FOR_CHECKING_MD5 = 1024 * 32;
    

    
    
    private final Map<Integer, ArrayList<ImgFSRecord>>
            currentJob = new HashMap<>();
    
    
    
    public ImgFS(ImgFSCrypto c, String dbname) {
        dbName = dbname;
        cryptModule = c;
        
        databaseH2File            = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db.0");
        databaseFileForFSPreviews = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db." + ImgFSRecord.FS_PREVIEW);
        databaseFileForFullImages = new File(myPath.getAbsoluteFile() + File.separator + dbName + File.separator + "db." + ImgFSRecord.FULL_IMAGE);
        
        currentJob.put(ImgFSRecord.FS_PREVIEW, new ArrayList<>());
        currentJob.put(ImgFSRecord.FULL_IMAGE, new ArrayList<>()); 
    }
    
    public void init() throws IOException {
        if (!new File(dbName).mkdir())
            throw new IOException("init: cannot create db folder ["+dbName+"];");
            
        try {
            database.h2DatabaseConnect(databaseH2File.getAbsolutePath(), cryptModule.getPasswordFromMasterKey());
        } catch (SQLException ex) {
            throw new IOException("init: cannot connect to database ["+dbName+"], " + ex.getMessage());
        }
    }
    
    public void close() {
        database.h2Close();
    }
    
    public synchronized void addFileToCurrentJob(String path, int type) throws IOException {
        if (!database.isConnected())
            throw new IOException("addFileToCurrentJob: database not connected!");
        
        if (!currentJob.containsKey(type))
            throw new IOException("addFileToCurrentJob: type ["+type+"] not exist!");
        
        final File jobFile = new File(path);
        if (!jobFile.exists())      throw new IOException("addFileToCurrentJob: file ["+path+"] not exist!");
        if (!jobFile.isFile())      throw new IOException("addFileToCurrentJob: file ["+path+"] not regular file!");
        if (!jobFile.canRead())     throw new IOException("addFileToCurrentJob: file ["+path+"] not readable!");
        if (jobFile.length() <= 0)  throw new IOException("addFileToCurrentJob: file ["+path+"] has null size!");
        
        if (!isImage(jobFile.getAbsolutePath()))
            throw new IOException("addFileToCurrentJob: file ["+path+"] is not image!");
        
        final byte[] fileMD5 = getFilePartMD5(jobFile.getAbsolutePath());
        if (fileMD5 == null)
            throw new IOException("addFileToCurrentJob: cannot calculate MD5 for file ["+path+"]!");
        
        final ArrayList<ImgFSRecord> currentJobList = currentJob.get(type);
        final long startSector = (currentJobList.isEmpty()) ? 1 : (currentJobList.get((currentJobList.size() - 1)).getEndSector() + 1);
        final long sectorSize = getSectorSize(jobFile.length());
        final long endSector = startSector + sectorSize;
        
        final ImgFSRecord jobElement = new ImgFSRecord(startSector, endSector, sectorSize, jobFile.length(), fileMD5, type, jobFile);
        currentJobList.add(jobElement);
    }
    
    public synchronized void commitJob(int type) throws IOException {
        if (!database.isConnected())
            throw new IOException("commitJob: database not connected!");
        
        if (!currentJob.containsKey(type))
            throw new IOException("commitJob: type ["+type+"] not exist!");
        
        final ArrayList<ImgFSRecord> currentJobList = currentJob.get(type);
        
        
        
        
        currentJobList.clear();
    }
    
    
    
    
    
    

    
    private byte[] getFilePartMD5(String path) {
        try {
            final FileInputStream fis = new FileInputStream(path);
            FileChannel fc = fis.getChannel();
            fc.position(0);
            final ByteBuffer bb = ByteBuffer.allocate(FILE_PART_SIZE_FOR_CHECKING_MD5);
            int counter = fc.read(bb);
            if (counter > 0) {
                IO_R += counter;
                if (counter == FILE_PART_SIZE_FOR_CHECKING_MD5) 
                    return ImgFSCrypto.MD5(bb.array());
                else {
                    final ByteBuffer bb_cutted = ByteBuffer.allocate(counter);
                    bb_cutted.put(bb.array(), 0, counter);
                    return ImgFSCrypto.MD5(bb_cutted.array());
                }
            } else 
                return null;
        } catch (IOException ex) {
            _L(ex.getMessage());
            return null;
        }
    }
    
    private long getSectorSize(long actualSize) {
        long tail = actualSize % DATABASE_SECTOR_SIZE;
        if (tail == 0) return (actualSize / DATABASE_SECTOR_SIZE); else return ((actualSize / DATABASE_SECTOR_SIZE) + 1); 
    }
    
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
    
    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s);
    }
}
