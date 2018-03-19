package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dao.AuthInfo;
import utils.CryptUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static service.RootService.DATASTORAGE_ROOT;

public class AuthService {
    private static final File storageDir = new File(DATASTORAGE_ROOT + "auth/").getAbsoluteFile();
    private static final Gson gson = new Gson();
    private static final SecureRandom rnd = new SecureRandom();

    public AuthService() {
        storageDir.mkdirs();
    }

    private byte[] readFileWOException(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private AuthInfo getAuthInfoFromJson(String json) {
        try {
            return gson.fromJson(json, AuthInfo.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private boolean verifyAuthData(String password, AuthInfo obj) {
        try {
            final byte[] ecnryptedHash = CryptUtils.sha512(obj.getEncryptedMasterKey());
            if (!Arrays.equals(ecnryptedHash, obj.getEncryptedMasterKeyHash())) return false;
            //System.out.println(Hex.encodeHexString(obj.getEncryptedMasterKey()));

            final byte[] masterKey = CryptUtils.aes256Decrypt(obj.getEncryptedMasterKey(), password);
            if (Objects.isNull(masterKey)) return false;

            final byte[] denryptedHash = CryptUtils.sha512(masterKey);
            return Arrays.equals(denryptedHash, obj.getDecryptedMasterKeyHash());
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] createAuthDataByPassword(String password) {
        final byte[] masterKey = new byte[512];
        rnd.nextBytes(masterKey);
        final byte[] masterKeyHash = CryptUtils.sha512(masterKey);
        final byte[] encryptedMasterKey = CryptUtils.aes256Encrypt(masterKey, password);
        final byte[] encryptedMasterKeyHash = CryptUtils.sha512(encryptedMasterKey);

        final AuthInfo authInfo = new AuthInfo();
        authInfo.setEncryptedMasterKey(encryptedMasterKey);
        authInfo.setDecryptedMasterKeyHash(masterKeyHash);
        authInfo.setEncryptedMasterKeyHash(encryptedMasterKeyHash);

        final String json = gson.toJson(authInfo, AuthInfo.class);
        final Path savePath = new File(storageDir.getAbsolutePath() + File.separator + "auth" + System.currentTimeMillis() + ".json").toPath();
        try {
            Files.write(savePath, json.getBytes(), CREATE);
        } catch (IOException e) {
            System.err.println("Cannot write auth file");
            return null;
        }

        return masterKey;
    }

    public byte[] getAuthDataByPassword(String password) {
        final File[] files = storageDir.listFiles();
        if (Objects.isNull(files)) return null;
        if (files.length <= 0) return null;

        final List<Path> jsonList = Arrays.asList(files).stream()
                .filter(e -> e.getName().endsWith(".json"))
                .map(e -> e.toPath())
                .collect(Collectors.toList());
        if (jsonList.isEmpty()) return null;

        return jsonList.stream()
                .map(path -> readFileWOException(path))
                .filter(data -> (data.length > 0))
                .map(data -> new String(data))
                .map(data -> getAuthInfoFromJson(data))
                .filter(obj -> Objects.nonNull(obj))
                .filter(obj -> verifyAuthData(password, obj))
                .findFirst()
                .map(obj -> obj.getEncryptedMasterKey())
                .map(arr -> CryptUtils.aes256Decrypt(arr, password))
                .orElse(null);
    }
}
