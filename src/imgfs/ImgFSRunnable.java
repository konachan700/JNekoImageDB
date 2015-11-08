package imgfs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnekoimagesdb.JNekoImageDB;

public class ImgFSRunnable implements Runnable {
    private final int myID, myType;
    private final ImgFS myFS;
    
    public ImgFSRunnable(ImgFS fs, int type, int id) {
        super();
        myID = id;
        myType = type;
        myFS = fs;
    }
    
    @Override
    public void run() {
        while (true) {
            if (!processElement())
                try { 
                    synchronized (myFS){ myFS.wait(); } 
                } catch (InterruptedException ex) { }
        }
    }
    
    private boolean processElement() {
        switch (myType) {
            case ImgFSRecord.FS_PREVIEW:
                final ImgFSRecord record = myFS.getNextFSPreviewRecord(myID); 
                if (record == null) {
                    myFS.insertFSPreviewsToDB(myID, true);
                    myFS.progressOk(myID, myType); 
                    return false;
                }
                processFSPreview(record);
                myFS.progressInfo(myID, record);
                return true;
            case ImgFSRecord.FULL_IMAGE:
                
                
                return false;
            default:
                return false;
        }
    }
    
    private void processFSPreview(ImgFSRecord record) {
        try {
            try { // Это тут действительно необходимо, поскольку размер изображений непредсказуем и падения происходят, как ни крути.
                myFS.writeFSPreviewRecord(record, myID);
            } catch (Error ee) {
                _L("Runtime error in thread #" + myID + ", " + ee.getMessage());
                return;
            }
            myFS.insertFSPreviewsToDB(myID, false); 
        } catch (IOException ex) {
            Logger.getLogger(ImgFSRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s); 
    }
}
