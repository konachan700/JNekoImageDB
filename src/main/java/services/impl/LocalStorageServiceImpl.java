package services.impl;

import static java.nio.file.StandardOpenOption.CREATE;
import static model.GlobalConfig.PREFIX;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javafx.application.Platform;
import model.Metadata;
import model.entity.ImageEntity;
import model.entity.TagEntity;
import services.api.CryptographyService;
import services.api.LocalDaoService;
import services.api.LocalStorageService;
import services.api.UtilService;
import services.api.WaitInformer;
import utils.SimpleImageInfo;
import worker.QueuedWorker;

@Service
public class LocalStorageServiceImpl implements LocalStorageService, DisposableBean {
	public class Task {
		private Path imagePath = null;
		private Collection<String> tagsForImage = null;
		private WaitInformer informer = null;

		public Path getImagePath() {
			return imagePath;
		}

		public void setImagePath(Path imagePath) {
			this.imagePath = imagePath;
		}

		public Collection<String> getTagsForImage() {
			return tagsForImage;
		}

		public void setTagsForImage(Collection<String> tagsForImage) {
			this.tagsForImage = tagsForImage;
		}

		public WaitInformer getInformer() {
			return informer;
		}

		public void setInformer(WaitInformer informer) {
			this.informer = informer;
		}
	}

	public static final String IMG_LOCAL = "img-local";
	private MVStore mvStore = null;
    private File cacheFile = null;
    private MVMap<String, byte[]> cacheStorage = null;

	@Autowired
	CryptographyService cryptographyService;

	@Autowired
	LocalDaoService localDaoService;

	@Autowired
	UtilService utilService;

	private final QueuedWorker<Task> importerPool = new QueuedWorker<Task>() {
		@Override
		public void onZeroCount(Task task) {
			Platform.runLater(() -> task.getInformer().onProgress("FINISH", 0));
		}

		@Override
		public void threadEvent(Task task) {
			if (task == null || task.getImagePath() == null || task.getInformer() == null || task.getTagsForImage() == null) return;
			final int qCount = importerPool.getCounter().get();
			try {
				final byte[] content = Files.readAllBytes(task.getImagePath());
				final byte[] encryptedContent = cryptographyService.encrypt(content);
				final byte[] encryptedHash = cryptographyService.hash(encryptedContent);

				if (localDaoService.imagesElementExist(encryptedHash)) {
					Platform.runLater(() ->
							task.getInformer().onProgress(
									"ALREADY EXIST [" + qCount + "]\tFile: " + task.getImagePath().toString(), qCount));
					return;
				}

				final File file = getLocalStorageElement(IMG_LOCAL, encryptedHash);
				Files.write(file.toPath(), encryptedContent, CREATE);

				final List<TagEntity> tags = localDaoService.tagGetOrCreate(task.getTagsForImage());
				localDaoService.imagesWrite(encryptedHash, tags);

				final Metadata metadata = new Metadata();
				metadata.setMeta(Metadata.META_IMG__FILE_EXT, getExtention(task.getImagePath().toFile()));
				try {
					SimpleImageInfo simpleImageInfo = new SimpleImageInfo(task.getImagePath().toFile());
					metadata.setMeta(Metadata.META_IMG__MIMETYPE, simpleImageInfo.getMimeType());
					metadata.setMeta(Metadata.META_IMG__WIDTH, simpleImageInfo.getWidth());
					metadata.setMeta(Metadata.META_IMG__HEIGHT, simpleImageInfo.getHeight());
				} catch (IOException e) { }
				localDaoService.saveImageMeta(encryptedHash, metadata);

				Platform.runLater(() ->
						task.getInformer().onProgress(
								"OK [" + qCount + "]\tFile: " + task.getImagePath().toString(), qCount));
			} catch (Exception e) {
				Platform.runLater(() ->
						task.getInformer().onProgress(
								"ERROR! Message: " + e.getMessage() + "\n\tFile: " + task.getImagePath().toString(), qCount));
			}
		}
	};

   @PostConstruct
	void init() {
	   utilService.databaseDirectory().mkdirs();
		if (!utilService.databaseDirectory().exists() || !utilService.databaseDirectory().isDirectory()) {
			throw new ExceptionInInitializerError("Can't create database directory");
		}

		cacheFile = new File(utilService.databaseDirectory().getPath() + File.separator + cryptographyService.getNameForCacheDb() + ".cache");
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
    public byte[] getLocalDBItem(byte[] hash) {
        final File file = getLocalStorageElement(IMG_LOCAL, hash);
		try {
			final byte[] data = Files.readAllBytes(file.toPath());
			return cryptographyService.decrypt(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }

	@Override
	public void importAllLocalDBItems(Collection<Path> files, Collection<String> tags, WaitInformer informer) {
		if (files.isEmpty()) return;
		final AtomicInteger counter = new AtomicInteger(0);
		files.forEach(file -> {
			final Task task = new Task();
			task.setImagePath(file);
			task.setTagsForImage(tags);
			task.setInformer(informer);
			importerPool.pushTask(task);
			counter.incrementAndGet();
		});
		informer.onProgress("All images push to queue. Image count = " + counter.get(), counter.get());
	}

	@Override
	public void importProcessStop() {
		importerPool.getQueue().clear();
	}

	@Override
	public boolean saveImageToExchangeFolder(ImageEntity currentImage, String exchangeFolder) {
		if (currentImage == null) {
			return false;
		}
		final File dir = new File(exchangeFolder).getAbsoluteFile();
		dir.mkdirs();
		final String ext = Optional.ofNullable(localDaoService.getImageMeta(currentImage.getImageHash()))
				.map(meta -> meta.getMeta(Metadata.META_IMG__FILE_EXT, String.class))
				.orElse("jpg");
		final File imgFile = new File(dir.getAbsolutePath() + File.separator + currentImage.getImageID() + "." + ext);
		final byte[] content = getLocalDBItem(currentImage.getImageHash());
		try {
			Files.write(imgFile.toPath(), content, CREATE);
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	@Override
	public void destroy() {
		importerPool.dispose();
		mvStore.commit();
		mvStore.compactRewriteFully();
		mvStore.close();
	}

	String getExtention(File f) {
		final String name = f.getName();
		final int extDotPos = name.lastIndexOf(".");
		return name.substring(extDotPos + 1);
	}

	File getLocalStorageElement(String storageName, byte[] hash) {
		final StringBuilder pathToStorageDir = new StringBuilder();
		pathToStorageDir
				.append(utilService.getStorageDirectory().getAbsolutePath())
				.append(File.separator)
				.append(storageName)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[0]) & 0x0F)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[0] >> 4) & 0x0F)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[1]) & 0x0F)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[1] >> 4) & 0x0F);

		final File dir = new File(pathToStorageDir.toString());
		if (!dir.mkdirs()) {
			if (!dir.exists()) throw new IllegalStateException("can't create storage directory");
		}

		pathToStorageDir
				.append(File.separator)
				.append(Hex.encodeHexString(hash), 8, 16 + 8)
				.append(".bin");

		return new File(pathToStorageDir.toString());
	}
}
