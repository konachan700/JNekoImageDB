package proto;

public interface LocalStorageService extends Disposable {
    byte[] getCacheItem(byte[] hash, int w, int h);
    void storeCacheItem(byte[] hash, byte[] data, int w, int h);
    void resetCache();
    void resetCacheElement(byte[] hash, int w, int h);

    byte[] getLocalDBItem(byte[] hash);
    void storeLocalDBItem(byte[] hash, byte[] data);
}
