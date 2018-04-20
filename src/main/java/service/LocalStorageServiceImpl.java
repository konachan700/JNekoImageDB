package service;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.Hex;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import proto.CryptographyService;
import proto.LocalStorageService;
import proto.UseStorageDirectory;

public class LocalStorageServiceImpl implements UseStorageDirectory, LocalStorageService {
    private final ReentrantLock lock = new ReentrantLock();

    private final MVStore mvStore;
    private final CryptographyService cryptographyService;
    private final File cacheFile;
    private final MVMap<String, byte[]> cacheStorage;

    public LocalStorageServiceImpl(CryptographyService cryptographyService) {
        this.cryptographyService = cryptographyService;
        cacheFile = getFile("cache.kv");

        mvStore = new MVStore.Builder()
                .fileName(cacheFile.getAbsolutePath())
                .encryptionKey(Hex.encodeHex(cryptographyService.getAuthData()))
                .autoCommitDisabled()
                .cacheSize(64)
                .open();
        cacheStorage = mvStore.openMap("cache-local");
    }

    private String getKey(byte[] hash, int w, int h) {
        final byte[] t = (Hex.encodeHexString(hash) + "-" + w + "-" + h).getBytes();
        return Hex.encodeHexString(cryptographyService.hash(t));
    }

    @Override
    public synchronized byte[] getCacheItem(byte[] hash, int w, int h) {
        final String key = getKey(hash, w, h);
        final byte[] encrypted = cacheStorage.get(key);
        if (encrypted == null || encrypted.length < 16) return null;
        return cryptographyService.decrypt(encrypted);
    }

    @Override
    public synchronized void storeCacheItem(byte[] hash, byte[] data, int w, int h) {
        final String key = getKey(hash, w, h);
        cacheStorage.put(key, cryptographyService.encrypt(data));
        mvStore.commit();
    }

    @Override
    public synchronized void resetCache() {
        cacheStorage.clear();
        mvStore.commit();
    }

    @Override
    public synchronized void resetCacheElement(byte[] hash, int w, int h) {
        final String key = getKey(hash, w, h);
        cacheStorage.remove(key);
        mvStore.commit();
    }

    @Override
    public byte[] getLocalDBItem(byte[] hash) {
        return new byte[0];
    }

    @Override
    public void storeLocalDBItem(byte[] hash, byte[] data) {
        /*final File file = getLocalStorageElement("binary", hash);
        try {
            Files.write(file.toPath(), data, CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

	@Override public void dispose() {
		mvStore.commit();
		mvStore.compactRewriteFully();
		mvStore.close();
	}
}
