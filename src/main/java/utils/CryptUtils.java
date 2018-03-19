package utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.HeadlessException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CryptUtils {
    private final static String SALT = "2yfg3 8i&^TF^*D &^$DTYvtuif867f&TV^Rxc76$UD^U yitC%&V^&tr87x1&TF^Rct7f7&C6c6rc7tv87h7i b08b67f6cE%Y#D^VvtC^DF%&RC^U%ivi6vtvv";

    public static byte[] sha256(byte[] b) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(SALT.getBytes());
            md.update(b);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not allowed on this PC\nError: " + ex.getMessage());
        }
    }

    public static byte[] sha512(byte[] b) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(SALT.getBytes());
            md.update(b);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-512 not allowed on this PC\nError: " + ex.getMessage());
        }
    }

    public static byte[] aes256Decrypt(byte[] value, String password) {
        final byte[] hash = sha512(password.getBytes());
        final byte[] key = Arrays.copyOfRange(hash, 0, 32);
        final byte[] iv = Arrays.copyOfRange(hash, 32, 48);
        //System.out.println("aes256Decrypt " + Hex.encodeHexString(hash));
        return aes256Decrypt(value, key, iv);
    }

    public static byte[] aes256Encrypt(byte[] value, String password) {
        final byte[] hash = sha512(password.getBytes());
        final byte[] key = Arrays.copyOfRange(hash, 0, 32);
        final byte[] iv = Arrays.copyOfRange(hash, 32, 48);
        //System.out.println("aes256Encrypt " + Hex.encodeHexString(hash));
        return aes256Encrypt(value, key, iv);
    }

    public static byte[] aes256Decrypt(byte[] value, byte[] key, byte[] iv) {
        try {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(value);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("AES256 error\nError: " + e.getClass().getSimpleName() + "; Message: " + e.getMessage());
        }
    }

    public static byte[] aes256Encrypt(byte[] value, byte[] key, byte[] iv) {
        try {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(value);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("AES256 error\nError: " + e.getClass().getSimpleName() + "; Message: " + e.getMessage());
        }
    }
}
