package imgfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import jnekoimagesdb.JNekoImageDB;

public class ImgFSCrypto {
    public static final int
            RSA_KEY_LEN = 4096;
    
    private static volatile boolean
            isAES256Enable = true;
    
    private static class CryptInfoData implements Serializable {
        public byte[]
                masterKey256    = null, 
                IV128           = null,
                salt128         = null;
        
        public CryptInfoData() {}
    }
    
    private static class CryptInfo {        
        private final CryptInfoData
                cid;

        public CryptInfo(CryptInfoData c) {
            cid = c;
        }
        
        public CryptInfo() {
            final SecureRandom sr = new SecureRandom();
            cid = new CryptInfoData();
            
            final byte[] 
                IV              = new byte[1024 * 4], 
                masterKey       = new byte[1024 * 4], 
                salt            = new byte[1024 * 4];
            
            sr.nextBytes(masterKey); 
            sr.nextBytes(salt); 
            sr.nextBytes(IV);
            
            cid.IV128            = MD5(IV);
            cid.salt128          = MD5(salt); 
            if (isAES256Enable)
                cid.masterKey256 = SHA256(masterKey);
            else
                cid.masterKey256 = MD5(masterKey);
        }
        
        public CryptInfoData getData() {
            return cid;
        }
        
        private byte[] MD5(byte[] unsafe) {
            final MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                md.update(unsafe);
                return md.digest();
            } catch (NoSuchAlgorithmException ex) { }
            return null;
        }
        
        private byte[] SHA256(byte[] b) {
            final MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
                md.update(cid.salt128);
                md.update(b);
                md.update(cid.salt128);
                return md.digest();
            } catch (NoSuchAlgorithmException ex) { return null; }
        }
        
        public byte[] getIV() {
            return cid.IV128;
        }
        
        public byte[] getKey256() {
            return cid.masterKey256;
        }
        
        public String getPassword() {
            return DatatypeConverter.printHexBinary(SHA256(cid.masterKey256));
        }
    }
    
    public static interface CryptActionListener {
        public byte[] PrivateKeyRequired();
    }
    
    private CryptInfo 
            CI = null;
    
    private RSAPublicKey 
            pubKey = null;
    
    private RSAPrivateKey
            privKey = null;
    
    private File
            privateKeyFile, publicKeyFile, keystoreFile;
    
    private final CryptActionListener
            actionListener;
    
    private boolean 
            notInit = true;
    
    public ImgFSCrypto(CryptActionListener c) { 
        actionListener = c;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public void init(String dbname) throws Exception {
        publicKeyFile  = new File("." + File.separator + dbname + File.separator + "public.key");
        privateKeyFile = new File("." + File.separator + dbname + File.separator + "private.key");
        
        isAES256Enable = isAES256Support();
        
        if (!publicKeyFile.exists()) {
            final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(RSA_KEY_LEN);
            final KeyPair kp = kpg.generateKeyPair();

            pubKey = (RSAPublicKey) kp.getPublic();
            privKey = (RSAPrivateKey) kp.getPrivate();
            
            final X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubKey.getEncoded());
            final FileOutputStream fos1 = new FileOutputStream(publicKeyFile);
            fos1.write(x509EncodedKeySpec.getEncoded());
            fos1.close();
            
            final PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privKey.getEncoded());
            final FileOutputStream fos2 = new FileOutputStream(privateKeyFile);
            fos2.write(pkcs8EncodedKeySpec.getEncoded());
            fos2.close();
        } else {
            final byte[] priv, pub;
            if (!privateKeyFile.exists()) 
                priv = actionListener.PrivateKeyRequired();
            else 
                priv = Files.readAllBytes(FileSystems.getDefault().getPath(privateKeyFile.getAbsolutePath()));
            if (priv == null) throw new IOException("Private key cannot be null");
            
            pub = Files.readAllBytes(FileSystems.getDefault().getPath(publicKeyFile.getAbsolutePath()));
            if (pub == null) throw new IOException("Public key cannot be null");
            
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pub);
            final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(priv);
            
            pubKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            privKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
        }
        
        keystoreFile = new File("." + File.separator + dbname + File.separator + "keystore.bin");
        final CryptInfo ci;
        if (!keystoreFile.exists() || (keystoreFile.length() <= 0)) {
            ci = new CryptInfo();
            writeKeystore(ci.getData(), keystoreFile);
        } else {
            final CryptInfoData cid = readKeystore(keystoreFile);
            ci = new CryptInfo(cid);
        }
        
        CI = ci;
        notInit = false;
    }
    
    public byte[] getSalt() {
        return MD5(CI.getData().salt128);
    }
    
    public String getPassword() {
        if (notInit) return null; else return CI.getPassword();
    }
    
    public byte[] Crypt(byte[] value) {
        if (notInit) return null;
        try {
            return AESCrypt256(value, CI.getData().masterKey256, CI.getData().IV128);
        } catch (Exception ex) {
            Logger.getLogger(ImgFSCrypto.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } 
    }
    
    public byte[] Decrypt(byte[] value) {
        if (notInit) return null;
        try {
            return AESDecrypt256(value, CI.getData().masterKey256, CI.getData().IV128);
        } catch (Exception ex) {
            Logger.getLogger(ImgFSCrypto.class.getName()).log(Level.SEVERE, null, ex);
            _L("Crypt error: "+ex.getMessage());
            return null;
        }
    }
    
    private boolean isAES256Support() {
        final SecureRandom sr = new SecureRandom();
        final byte[] tkey = sr.generateSeed(32);
        final byte[] tIV = sr.generateSeed(16);
        final byte[] tdata = sr.generateSeed(32);
        
        try {
            final byte[] outdata = AESCrypt256(tdata, tkey, tIV);
            return (outdata.length > 0);
        } catch (Exception ex) {
            _L("AES256 not supported. Please, install the patch: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html");
            return false;
        }
    }
    
    private CryptInfoData readKeystore(File f) throws Exception {
        final byte[] retNC = Files.readAllBytes(FileSystems.getDefault().getPath(f.getAbsolutePath()));
        final byte[] ret = RSADecrypt(retNC);

        final ByteArrayInputStream bais = new ByteArrayInputStream(ret);
        final ObjectInputStream oos = new ObjectInputStream(bais);
        
        final CryptInfoData ci = (CryptInfoData) oos.readObject();
        
        oos.close();
        bais.close();
        
        return ci;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    private void writeKeystore(CryptInfoData e, File f) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(e);
        oos.flush();
        
        final byte[] nonCrypted = baos.toByteArray();
        final byte[] crypted = RSACrypt(nonCrypted);

        Files.write(FileSystems.getDefault().getPath(f.getAbsolutePath()), crypted, StandardOpenOption.CREATE);
        
        oos.close();
        baos.close();
    }
    
    private byte[] RSADecrypt(byte[] ct) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
        
        final int 
                iterCount = (RSA_KEY_LEN / 8),
                arrayLen = ct.length;

        for (int i=0; i<arrayLen; i=i+iterCount) {
            baos.write(rsaCipher.doFinal(Arrays.copyOfRange(ct, i, i+iterCount)));
        }
        
        final byte[] crypted = baos.toByteArray();
        baos.close();
        return crypted;
    }
    
    private byte[] RSACrypt(byte[] ct) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
        
        final int 
                iterCount = (RSA_KEY_LEN / 8) - 16, 
                arrayLen = ct.length;

        for (int i=0; i<arrayLen; i=i+iterCount) {
            baos.write(rsaCipher.doFinal(Arrays.copyOfRange(ct, i, i+iterCount)));
        }
        
        final byte[] crypted = baos.toByteArray();
        baos.close();
        return crypted;
    }

    public byte[] MD5(byte[] ... unsafe) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            for (byte[] b : unsafe) md.update(b);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) { }
        return null;
    }
        
    public byte[] MD5(byte[] unsafe) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(unsafe);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) { }
        return null;
    }
    
    private byte[] AESDecrypt256(byte[] value, byte[] key, byte[] iv) throws Exception { 
        final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(value);
    }
    
    private byte[] AESCrypt256(byte[] value, byte[] key, byte[] iv) throws Exception {
        final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        return cipher.doFinal(value);
    }

    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s); 
    }
}
