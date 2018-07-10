package services.api;

import java.io.File;
import java.nio.file.Path;

import model.GlobalConfig;

public interface UtilService {
	String getMasterPassword();
	File databaseDirectory();
	Path getConfigPath();
	File getStorageDirectory();
	GlobalConfig getConfig();
}
