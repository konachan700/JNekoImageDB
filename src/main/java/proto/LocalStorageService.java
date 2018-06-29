package proto;

import java.nio.file.Path;
import java.util.Collection;

import model.entity.ImageEntity;

public interface LocalStorageService extends Disposable {
    byte[] getCacheItem(byte[] hash, int w, int h);
    void storeCacheItem(byte[] hash, byte[] data, int w, int h);
    void resetCache();
    void resetCacheElement(byte[] hash, int w, int h);

    byte[] getLocalDBItem(byte[] hash);
    void importAllLocalDBItems(Collection<Path> files, Collection<String> tags, WaitInformer informer);
    void importProcessStop();
	boolean saveImageToExchangeFolder(ImageEntity currentImage, String exchangeFolder);
}
