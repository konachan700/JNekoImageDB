package service.fs;

import javafx.application.Platform;
import org.apache.commons.codec.binary.Hex;
import service.RootService;
import service.resizer.ImageResizeTask;
import utils.CryptUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static service.RootService.DATASTORAGE_ROOT;

public class EncryptedFileAccessService extends AbstractFileAccessService {
    private final ExecutorService executor;
    private final LinkedBlockingDeque<FilePusherTask> queue = new LinkedBlockingDeque<>();

    private final Runnable thread = () -> {
        while(true) {
            try {
                final FilePusherTask filePusherTask = queue.pollLast(9999, TimeUnit.DAYS);
                final Path path = filePusherTask.getImagePath();

                if (queue.size() == 0)
                    Platform.runLater(() -> filePusherTask.getPusherActionListener().onZeroQuene());

                if (RootService.getDaoService().hasDuplicates(path)) {
                    Platform.runLater(() -> filePusherTask.getPusherActionListener().onDuplicateDetected(path));
                } else {
                    if (Objects.nonNull(path)) {
                        try {
                            final byte[] bytes = Files.readAllBytes(path);
                            final String id = super.writeDBFile(bytes);
                            RootService.getDaoService().pushImageId(path, id);
                            Platform.runLater(() -> filePusherTask.getPusherActionListener().onPush(path, 0, queue.size()));
                        } catch (IOException ex) {
                            Platform.runLater(() -> filePusherTask.getPusherActionListener().onError(path, ex));
                        }
                    }
                }
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getClass().getSimpleName() + " (" + e.getMessage() + ")");
            }
        }
    };

    private final byte[] masterKey;
    private final byte[] iv;
    private final String storageName;
    private final File storageDir;

    public EncryptedFileAccessService(byte[] authData) {
        if (Objects.isNull(authData)) throw new IllegalArgumentException("authData cannot be null");

        final byte[] sha512 = CryptUtils.sha512(authData);
        this.masterKey = Arrays.copyOfRange(sha512, 0, 32);
        this.iv = Arrays.copyOfRange(sha512, 32, 48);
        this.storageName = Hex.encodeHexString(Arrays.copyOfRange(CryptUtils.sha512(sha512), 48, 64), true);
        this.storageDir = new File(DATASTORAGE_ROOT + "data/" + this.storageName).getAbsoluteFile();
        this.storageDir.mkdirs();

        int cores = Runtime.getRuntime().availableProcessors();
        if (cores <= 0) throw new IllegalStateException("cannot get CPUs count");

        executor = Executors.newFixedThreadPool(cores);
        for (int i=0; i<cores; i++) executor.submit(thread);
    }

    public void dispose() {
        executor.shutdownNow();
    }

    @Override
    byte[] crypt(byte[] plainBlob) {
        return CryptUtils.aes256Encrypt(plainBlob, masterKey, iv);
    }

    @Override
    byte[] decrypt(byte[] cryptedBlob) {
        return CryptUtils.aes256Decrypt(cryptedBlob, masterKey, iv);
    }

    @Override
    byte[] hash(byte[] data) {
        return CryptUtils.sha256(data);
    }

    @Override
    File getStorageDirectory() {
        return this.storageDir;
    }

    @Override
    int getStorageDeep() {
        return 4;
    }

    public void pushImagesToStorage(CopyOnWriteArrayList<FilePusherTask> images) {
        images.forEach(e -> {
            try {
                if (!queue.contains(e)) queue.putLast(e);
            } catch (InterruptedException e1) { }
        });
    }

    public void pushImageToStorage(FilePusherTask image) {
        try {
            if (!queue.contains(image)) queue.putLast(image);
        } catch (InterruptedException e1) { }
    }
}
