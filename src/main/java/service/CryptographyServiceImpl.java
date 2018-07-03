package service;

import org.apache.commons.codec.binary.Hex;
import proto.CryptographyService;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CryptographyServiceImpl implements CryptographyService {
    public enum EncryptType {
        AES128, AES256, DISABLE
    }

    private static CryptographyService cryptographyService = null;

    private final String salt;
    private final byte[] hash;
    private final EncryptType encryptType;

    private CryptographyServiceImpl(String password, String salt, EncryptType encryptType) {
		this.salt = salt;
		this.encryptType = encryptType;
        this.hash = sha512(password.getBytes());
    }

    public static CryptographyService getInstance(String password, String salt, EncryptType encryptType) {
        if (cryptographyService == null) {
            cryptographyService = new CryptographyServiceImpl(password, salt, encryptType);
        }
        return cryptographyService;
    }

    public String toHex(byte[] b) {
        return Hex.encodeHexString(b).toLowerCase();
    }

    private byte[] sha256(byte[] b) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            md.update(b);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not allowed on this PC\nError: " + ex.getMessage());
        }
    }

    private byte[] sha512(byte[] b) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            md.update(b);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-512 not allowed on this PC\nError: " + ex.getMessage());
        }
    }

    private byte[] aes256Decrypt(byte[] value) {
        final byte[] key = Arrays.copyOfRange(hash, 0, 32);
        final byte[] iv = Arrays.copyOfRange(hash, 32, 48);
        return aesDecrypt(value, key, iv);
    }

    private byte[] aes256Encrypt(byte[] value) {
        final byte[] key = Arrays.copyOfRange(hash, 0, 32);
        final byte[] iv = Arrays.copyOfRange(hash, 32, 48);
        return aesEncrypt(value, key, iv);
    }

    private byte[] aes128Decrypt(byte[] value) {
        final byte[] key = Arrays.copyOfRange(hash, 0, 16);
        final byte[] iv = Arrays.copyOfRange(hash, 32, 48);
        return aesDecrypt(value, key, iv);
    }

    private byte[] aes128Encrypt(byte[] value) {
        final byte[] key = Arrays.copyOfRange(hash, 0, 16);
        final byte[] iv = Arrays.copyOfRange(hash, 32, 48);
        return aesEncrypt(value, key, iv);
    }

    private byte[] aesDecrypt(byte[] value, byte[] key, byte[] iv) {
        try {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(value);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("AES error\nError: " + e.getClass().getSimpleName() + "; Message: " + e.getMessage());
        }
    }

    private byte[] aesEncrypt(byte[] value, byte[] key, byte[] iv) {
        try {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(value);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("AES error\nError: " + e.getClass().getSimpleName() + "; Message: " + e.getMessage());
        }
    }

    @Override
    public byte[] encrypt(byte[] open) {
        switch (encryptType) {
        case AES128:
            return aes128Encrypt(open);
        case AES256:
            return aes256Encrypt(open);
        case DISABLE:
            return open;
        default:
            throw new IllegalStateException("Crypt algo not supported or null");
        }
    }

    @Override
    public byte[] decrypt(byte[] encrypted) {
        switch (encryptType) {
        case AES128:
            return aes128Decrypt(encrypted);
        case AES256:
            return aes256Decrypt(encrypted);
        case DISABLE:
            return encrypted;
        default:
            throw new IllegalStateException("Crypt algo not supported or null");
        }
    }

	@Override
	public byte[] hash(byte[] data, int iteration) {
    	byte[] hashTemp = Arrays.copyOf(data, data.length);
    	for (int i=0; i<iteration; i++) {
			hashTemp = hash(hashTemp);
		}
		return hashTemp;
	}

    @Override
    public byte[] hash(byte[] data) {
        return sha512(data);
    }

    @Override public byte[] getAuthData() {
        return hash;
    }

    private String genName(int iter, int start, int size) {
		final byte[] newHash = hash(getAuthData(), iter);
		return Hex.encodeHexString(Arrays.copyOfRange(newHash, start, start + size)).toLowerCase();
	}

	@Override public String getNameForMainDb() {
		return genName(16, 0, 10);
	}

	@Override public String getNameForMetadataDb() {
		return genName(15, 0, 10);
	}

	@Override public String getNameForCacheDb() {
		return genName(14, 8, 10);
	}

	@Override public void dispose() { }
}
