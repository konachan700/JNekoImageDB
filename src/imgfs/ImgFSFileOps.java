package imgfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImgFSFileOps {
    public static final int
            FILE_PART_SIZE_FOR_CHECKING_MD5 = 1024 * 32;
    
    
    
    public static byte[] getFilePartMD5(String path) throws IOException {
        try {
            final FileInputStream fis = new FileInputStream(path);
            FileChannel fc = fis.getChannel();
            fc.position(0);
            final ByteBuffer bb = ByteBuffer.allocate(FILE_PART_SIZE_FOR_CHECKING_MD5);
            int counter = fc.read(bb);
            if (counter > 0) {
                if (counter == FILE_PART_SIZE_FOR_CHECKING_MD5) 
                    return ImgFSCrypto.MD5(bb.array());
                else {
                    final ByteBuffer bb_cutted = ByteBuffer.allocate(counter);
                    bb_cutted.put(bb.array(), 0, counter);
                    return ImgFSCrypto.MD5(bb_cutted.array());
                }
            } else 
                throw new IOException("cannot calculate MD5 for file ["+path+"]");
        } catch (IOException ex) {
            throw new IOException("cannot calculate MD5 for file ["+path+"], " + ex.getMessage());
        }
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public static byte[] readFile(ImgFSRecord fsElement) {
        try {
            final File imgFile = fsElement.getFile();
            final FileInputStream imgStream = new FileInputStream(imgFile);
            
            try {
                final FileChannel fc = imgStream.getChannel();
                fc.position(0);

                final ByteBuffer bb = ByteBuffer.allocate((int) fsElement.getActualSize());
                final int readed = fc.read(bb);
                if (readed != ((int) fsElement.getActualSize())) {
                    imgStream.close();
                    return null;
                }

                fc.close();
                imgStream.close();
                return bb.array();
            } catch (IOException ex) {
                Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    imgStream.close();
                } catch (IOException ex1) {
                    Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImgFS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
