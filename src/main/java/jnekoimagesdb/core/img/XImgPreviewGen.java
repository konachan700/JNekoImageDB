package jnekoimagesdb.core.img;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

public class XImgPreviewGen {   
    public static final int
            FILE_PART_SIZE_FOR_CHECKING_MD5 = 1024 * 32,
            MINIMUM_IMAGE_SIZE = 1024;
    
    private static final String 
            FIXED_FIELD_PATH = "__path";

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
}
