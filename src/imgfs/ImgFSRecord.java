package imgfs;

import java.io.File;

public class ImgFSRecord {
    public static final int 
            FS_PREVIEW = 1,
            FULL_IMAGE = 2;
    
    private long 
            startSector,
            endSector,
            sectorSize,
            actualSize;
    
    private final int
            imgType;
    
    private final byte
            md5[];
    
    private final File
            imageFile;
            
    
    public ImgFSRecord(long start, long end, long ss, long as, byte md5e[], int type, File f) {
        md5             = md5e;
        startSector     = start;
        endSector       = end;
        sectorSize      = ss;
        actualSize      = as;
        imgType         = type;
        imageFile       = f;
    }
    
    @Override
    public String toString() {
        return "startSector="+startSector+"; endSector="+endSector+"; sectorSize="+sectorSize+"; actualSize="+actualSize+";";
    }
    
    public File getFile() {
        return imageFile;
    }
    
    public int getImgType() {
        return imgType;
    }
    
    public void setStartSector(long l) {
        startSector = l;
    }
    
    public long getStartSector() {
        return startSector;
    }
    
    public void setEndSector(long l) {
        endSector = l;
    }
    
    public long getEndSector() {
        return endSector;
    }
    
    public void setSectorSize(long l) {
        sectorSize = l;
    }
    
    public long getSectorSize() {
        return sectorSize;
    }
    
    public void setActualSize(long l) {
        actualSize = l;
    }
    
    public long getActualSize() {
        return actualSize;
    }
    
    public byte[] getMD5() {
        return md5;
    }
}
