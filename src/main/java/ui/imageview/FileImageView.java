package ui.imageview;

import proto.CryptographyService;
import proto.LocalStorageService;

public abstract class FileImageView extends BaseImageView {
    private final CryptographyService cryptographyService;
    private final LocalStorageService localStorageService;

    public FileImageView() {
        this.cryptographyService = getService(CryptographyService.class);
        this.localStorageService = getService(LocalStorageService.class);
    }

    @Override
    public synchronized byte[] requestCacheItem(byte[] file) {
        final byte[] hash = cryptographyService.hash(file);
        final byte[] data = localStorageService.getCacheItem(hash, (int) getWidth(), (int) getHeight());
        return data;
    }

    @Override
    public void saveCacheItem(byte[] file, byte[] item) {
        final byte[] hash = cryptographyService.hash(file);
        localStorageService.storeCacheItem(hash, item, (int) getWidth(), (int) getHeight());
    }

    @Override
    public void deleteCacheItem(byte[] file) {
        final byte[] hash = cryptographyService.hash(file);
        localStorageService.resetCacheElement(hash, (int) getWidth(), (int) getHeight());
    }
}
