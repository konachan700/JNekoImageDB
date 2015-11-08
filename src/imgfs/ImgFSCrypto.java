package imgfs;

import dataaccess.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jnekoimagesdb.JNekoImageDB;

public class ImgFSCrypto {
    private byte[] 
            randomPool = null,
            masterKey = null,
            masterKeyAES = null;
    
    private static final String
            AES_CRYPT = "AES",
            AES_MODE = "AES/ECB/NoPadding",
            AES_MODE2 = "SunJCE",
            SHA256 = "SHA-256",
            MD5 = "MD5";

    public boolean genMasterKey(File f) {
        if (f.exists() && f.canRead() && (f.length() == 4096)) {
            try {
                final FileInputStream fis = new FileInputStream(f);
                masterKey = new byte[4096];
                int c = fis.read(masterKey);
                fis.close();
                if (c == 4096) {
                    return true;
                }
            } catch (IOException ex) { }
        }

        final SecureRandom sr = new SecureRandom();
        masterKey = new byte[4096];
        sr.nextBytes(masterKey);
        
        try {
            final FileOutputStream fos = new FileOutputStream(f);
            fos.write(masterKey);
            fos.close();
            _L(Lang.ERR_Crypto_new_master_key_generated);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    public boolean genSecureRandomSalt(File ff) {
        if (ff.exists() && ff.canRead() && (ff.length() == 4096)) {
            try {
                final FileInputStream fis = new FileInputStream(ff);
                randomPool = new byte[4096];
                final int readed = fis.read(randomPool);
                if (readed == 4096) {
                    fis.close();
                    return true;
                } else {
                    fis.close();
                    randomPool = null;
                }
            } catch (IOException ex) { }
        }
        
        final SecureRandom sr = new SecureRandom();
        randomPool = new byte[4096];
        sr.nextBytes(randomPool);
        
        try {
            final FileOutputStream fos = new FileOutputStream(ff);
            fos.write(randomPool);
            fos.close();
            _L(Lang.ERR_Crypto_new_salt_generated);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    public boolean genMasterKeyAES() {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(SHA256);
            md.update(randomPool);
            md.update(masterKey);
            masterKeyAES = md.digest();
            if (masterKeyAES != null) return true;
        } catch (NoSuchAlgorithmException ex) { }
        return false;
    }

    public boolean isValidKey() {
        if (masterKeyAES == null) return false;
        return true;
    }
    
    public byte[] Crypt(byte[] value) {
        if (masterKeyAES == null) return null;
        return AESCrypt(value, masterKeyAES);
    }
    
    public byte[] Decrypt(byte[] value) {
        if (masterKeyAES == null) return null;
        return AESDecrypt(value, masterKeyAES);
    }
    
    public String getPasswordFromMasterKey() {
        final StringBuilder sb = new StringBuilder();
        for (byte b : masterKeyAES) {
            sb.append(Integer.toHexString((int) b));
        }
        return sb.substring(0);
    }
    
    public static byte[] MD5(byte[] unsafe) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(MD5);
            md.update(unsafe);
            final byte[] dig = md.digest();
            return dig;
        } catch (NoSuchAlgorithmException ex) { }
        return null;
    }
    
    public static byte[] SHA256(byte[] unsafe) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(SHA256);
            md.update(unsafe);
            final byte[] dig = md.digest();
            return dig;
        } catch (NoSuchAlgorithmException ex) { }
        return null;
    }

    private byte[] AESDecrypt(byte[] value, byte[] password) { // как сделать AES-256 без сторонних библиотек и без IV\Padding, я так и не допер. Просто лень.
        try {
            final byte[] pwd = Arrays.copyOf(MD5(password), 16);
            final SecretKey key = new SecretKeySpec(pwd, AES_CRYPT);
            final Cipher cipher = Cipher.getInstance(AES_MODE, AES_MODE2);
            cipher.init(Cipher.DECRYPT_MODE, key);
            final byte[] decrypted = cipher.doFinal(value);
            return decrypted;
        } 
        catch (NoSuchAlgorithmException ex)         { _L("__AESDecrypt: NoSuchAlgorithmException"    ); } 
        catch (NoSuchProviderException ex)          { _L("__AESDecrypt: NoSuchProviderException"     ); } 
        catch (NoSuchPaddingException ex)           { _L("__AESDecrypt: NoSuchPaddingException"      ); } 
        catch (InvalidKeyException ex)              { _L("__AESDecrypt: InvalidKeyException"         ); } 
        catch (IllegalBlockSizeException ex)        { _L("__AESDecrypt: IllegalBlockSizeException"   ); } 
        catch (BadPaddingException ex)              { _L("__AESDecrypt: BadPaddingException"         ); }
        return null;
    }
    
    private byte[] AESCrypt(byte[] value, byte[] password) {
        try {
            final byte[] pwd = Arrays.copyOf(MD5(password), 16);
            final SecretKey key = new SecretKeySpec(pwd, AES_CRYPT);
            final Cipher cipher = Cipher.getInstance(AES_MODE, AES_MODE2);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            final byte[] encrypted = cipher.doFinal(value);
            return encrypted;
        } 
        catch (NoSuchAlgorithmException ex)         { _L("__AESCrypt: NoSuchAlgorithmException"     ); } 
        catch (NoSuchPaddingException ex)           { _L("__AESCrypt: NoSuchPaddingException"       ); } 
        catch (NoSuchProviderException ex)          { _L("__AESCrypt: NoSuchProviderException"      ); } 
        catch (InvalidKeyException ex)              { _L("__AESCrypt: InvalidKeyException"          ); } 
        catch (IllegalBlockSizeException ex)        { _L("__AESCrypt: IllegalBlockSizeException"    ); } 
        catch (BadPaddingException ex)              { _L("__AESCrypt: BadPaddingException"          ); }
        return null;
    }
    
    public byte[] align16b(byte[] b) {
        int 
                sz = b.length / 16,
                tail = b.length % 16;
        if (tail != 0) sz++;
        
        final byte fn[] = new byte[sz*16];
        for (int i=0; i<(sz*16); i++) if (i < b.length) fn[i] = b[i]; else fn[i] = 0;
        return fn;
    }
    
    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s); 
    }
}
