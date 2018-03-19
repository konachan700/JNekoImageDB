package service.fs;

import fao.ImageFile;
import org.apache.commons.codec.binary.Hex;
import utils.ImageUtils;
import utils.Utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;

public abstract class AbstractFileAccessService {
    //private final CopyOnWriteArrayList<Path> temporaryList = new CopyOnWriteArrayList<>();

    abstract byte[] crypt(byte[] plainBlob);
    abstract byte[] decrypt(byte[] cryptedBlob);
    abstract byte[] hash(byte[] data);
    abstract File getStorageDirectory();
    abstract int getStorageDeep();

    private static final Set<String> allowedExtentions = Utils.createSet(".jpg", ".jpeg", ".jpe", ".png");

    public CopyOnWriteArrayList<ImageFile> readImagesFromDirectory(Path dir) {
        final Set<Path> imagesList = Arrays.asList(dir.toAbsolutePath().toFile().listFiles()).parallelStream()
                .filter(file -> file.isFile())
                .filter(file -> allowedExtentions.stream().filter(name -> file.getName().toLowerCase().endsWith(name)).count() > 0)
                .map(file -> file.toPath())
                .collect(Collectors.toSet());
        final CopyOnWriteArrayList<ImageFile> retval = new CopyOnWriteArrayList<>();
        if (Objects.nonNull(imagesList)) {
            imagesList.forEach(file -> {
                try {
                    final Dimension dimension = ImageUtils.getImageDimension(file.toFile().getAbsoluteFile());
                    final ImageFile imageFile = new ImageFile(file);
                    imageFile.setRealSize(dimension.getWidth(), dimension.getHeight());
                    retval.add(imageFile);
                } catch (IOException e) {
                    System.err.println("File \"" + file.toFile().getName() + "\" not an image.");
                }
            });

            Collections.sort(retval, Comparator.comparing(a -> a.getImagePath().toFile().getName()));
        }
        return retval;
    }

    /*
    public CopyOnWriteArrayList<Path> readImagesFromTreeOfDirs(Path dir) {
        temporaryList.clear();
        temporaryList.addAll(readImagesInternal(dir));
        return temporaryList;
    }

    private CopyOnWriteArrayList<Path> readImagesInternal(Path dir) {
        final List<File> fileList = Arrays.asList(dir.toAbsolutePath().toFile().listFiles());
        fileList.parallelStream()
                .filter(file -> file.isDirectory())
                .map(file -> file.toPath())
                .forEach(d -> temporaryList.addAll(readImagesInternal(d)));

        return readImagesFromDirectory(dir);
    }*/

    public byte[] readDBFile(String id) throws IOException {
        final String storagePath = getStoragePath(id);

        final File file = new File(storagePath);
        if (!file.isFile()) throw new IOException("File not a regular!");
        if (!file.exists()) throw new IOException("File not exist!");
        if (!file.canRead()) throw new IOException("File cannot be read!");

        final Path path = file.toPath();
        final byte[] encryptedFile = Files.readAllBytes(path);
        final byte[] rawFile = decrypt(encryptedFile);

        final byte[] hashOfEncryptedData = hash(rawFile);
        final String hashString = Hex.encodeHexString(hashOfEncryptedData, true);
        if (!hashString.contentEquals(id.toLowerCase())) throw new IOException("File broken");

        return rawFile;
    }

    public String writeDBFile(byte[] data) throws IOException {
        if (Objects.isNull(data)) throw new IOException("Cannot write a null files");

        final byte[] hashOfEncryptedData = hash(data);
        final byte[] encrypted = crypt(data);
        final String hashString = Hex.encodeHexString(hashOfEncryptedData, true);

        final String storagePath = getStoragePath(hashString);
        final Path fileForSave = new File(storagePath).getAbsoluteFile().toPath();

        Files.write(fileForSave, encrypted, CREATE);

        return hashString;
    }

    public String getStoragePath(String id) throws IOException {
        if (Objects.isNull(id)) throw new IOException("Id of the file cannot be null");
        if (id.length() <= (getStorageDeep() * 2)) throw new IOException("Id too short");

        final StringBuilder name = new StringBuilder();
        name.append(getStorageDirectory().getAbsolutePath()).append(File.separator);
        for (int i=0; i<getStorageDeep(); i++) name.append(id.charAt(i)).append(File.separator);

        final File dir = new File(name.toString()).getAbsoluteFile();
        if ((!dir.exists()) && (!dir.mkdirs())) throw new IOException("Cannot create a directories tree!");

        name.append(id.substring(getStorageDeep()));
        return name.toString();
    }
}
