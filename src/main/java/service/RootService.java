package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dao.AppSettings;
import service.fs.EncryptedCacheAccessService;
import service.fs.EncryptedFileAccessService;
import ui.dialog.ImportImagesDialog;
import ui.imagelist.AbstractImageList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class RootService {
    public static final String DATASTORAGE_ROOT = "storage" + File.separator;

    private static final Gson gson = new Gson();
    private static final Path appConfigPath = new File(DATASTORAGE_ROOT + "config.json").toPath();

    private static final AuthService authService = new AuthService();

    private static EncryptedCacheAccessService encryptedCacheAccessService;
    private static EncryptedFileAccessService encryptedFileAccessService;
    private static ImportImagesDialog importImagesDialog;
    private static DaoService daoService;

    private static AppSettings appSettings;

    public static void dispose() {
        if (Objects.nonNull(importImagesDialog)) importImagesDialog.dispose();
        if (Objects.nonNull(encryptedFileAccessService)) encryptedFileAccessService.dispose();
        if (Objects.nonNull(daoService)) daoService.dispose();
        AbstractImageList.disposeStatic();
    }

    public static void initAllEncryptedStorages(byte[] authData) {
        if (Objects.isNull(encryptedFileAccessService)) encryptedFileAccessService = new EncryptedFileAccessService(authData);
        if (Objects.isNull(encryptedCacheAccessService)) encryptedCacheAccessService = new EncryptedCacheAccessService(authData);
        if (Objects.isNull(daoService)) daoService = new DaoService(authData);
        if (Objects.isNull(importImagesDialog)) importImagesDialog = new ImportImagesDialog();
    }

    public static EncryptedFileAccessService getFileService() {
        if (Objects.isNull(encryptedFileAccessService)) throw new IllegalStateException("EncryptedFileAccessService need init before use");
        return encryptedFileAccessService;
    }

    public static AuthService getAuthService() {
        return authService;
    }

    public static EncryptedCacheAccessService getCacheService() {
        if (Objects.isNull(encryptedCacheAccessService)) throw new IllegalStateException("EncryptedCacheAccessService need init before use");
        return encryptedCacheAccessService;
    }

    public static DaoService getDaoService() {
        if (Objects.isNull(daoService)) throw new IllegalStateException("DaoService need init before use");
        return daoService;
    }

    public static AppSettings getAppSettings() {
        if (Objects.isNull(appSettings)) throw new IllegalStateException("AppSettings need init before use");
        return appSettings;
    }

    public static void setAppSettings(AppSettings appSettings) {
        RootService.appSettings = appSettings;
    }

    public static void showImportDialog() {
        if (Objects.nonNull(importImagesDialog)) importImagesDialog.showAndWait();
    }

    public static void saveConfig() {
        try {
            Files.write(appConfigPath, gson.toJson(RootService.getAppSettings(), AppSettings.class).getBytes(), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e1) {
            System.err.println("System settings not be wrtitten. " + e1.getMessage());
        }
    }

    public static boolean loadConfig() {
        try {
            setAppSettings(Optional.ofNullable(Files.readAllBytes(appConfigPath))
                    .map(bytes -> new String(bytes))
                    .map(str -> gson.fromJson(str, AppSettings.class))
                    .orElse(new AppSettings()));
            return true;
        } catch (IOException | JsonSyntaxException e) {
            RootService.setAppSettings(new AppSettings());
            try {
                Files.write(appConfigPath, gson.toJson(RootService.getAppSettings(), AppSettings.class).getBytes(), CREATE);
                return true;
            } catch (IOException e1) {
                return false;
            }
        }
    }
}
