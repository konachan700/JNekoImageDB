package services.impl;

import static java.nio.file.StandardOpenOption.CREATE;
import static model.GlobalConfig.STORAGE_ROOT_DIR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Platform;
import model.GlobalConfig;
import services.api.UtilService;
import ui.dialogs.windows.PasswordWindow;
import utils.ReflectionConverter;

@Service
public class UtilServiceImpl implements UtilService, DisposableBean {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private Path configPath = null;
	private File storageDir = null;
	private File dbDir = null;

	private final GlobalConfig globalConfig = new GlobalConfig();

	@Autowired
	PasswordWindow passwordWindow;

	@PostConstruct
	void init() {
		try {
			final String configData = new String(Files.readAllBytes(getConfigPath()));
			final GlobalConfig tempConfig = gson.fromJson(configData, GlobalConfig.class);
			ReflectionConverter.convert(tempConfig, globalConfig);
		} catch (IOException e) {
			final String configData = gson.toJson(globalConfig);
			try {
				Files.write(getConfigPath(), configData.getBytes(), CREATE);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new IllegalStateException("Can't write configs");
			}
		}
	}

	@Override
	public String getMasterPassword() {
		if (Platform.isFxApplicationThread()) {
			passwordWindow.show(true);
			return passwordWindow.getPassword();
		} else {
			final ReentrantLock lock = new ReentrantLock();
			final AtomicReference<String> password = new AtomicReference<>();
			lock.lock();
			Platform.runLater(() -> {
				passwordWindow.show(true);
				if (passwordWindow.getPassword() != null) {
					password.set(passwordWindow.getPassword());
				}
				lock.unlock();
			});
			lock.lock();
			lock.unlock();
			return password.get();
		}
	}

	@Override
	public File databaseDirectory() {
		if (dbDir != null) {
			return dbDir;
		}
		dbDir = new File(getStorageDirectory().getAbsolutePath() + File.separator + "db").getAbsoluteFile();
		return dbDir;
	}

	@Override
	public Path getConfigPath() {
		if (configPath != null) {
			return configPath;
		}

		configPath = new File(getStorageDirectory().getAbsolutePath() + File.separator + "configs.json").getAbsoluteFile().toPath();
		return configPath;
	}

	@Override
	public File getStorageDirectory() {
		if (storageDir != null) {
			return storageDir;
		}
		storageDir = new File(STORAGE_ROOT_DIR).getAbsoluteFile();
		return storageDir;
	}

	@Override public GlobalConfig getConfig() {
		return globalConfig;
	}

	@Override
	public void destroy() throws Exception {
		final String configData = gson.toJson(globalConfig);
		try {
			Files.delete(getConfigPath());
			Files.write(getConfigPath(), configData.getBytes(), CREATE);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new IllegalStateException("Can't write configs");
		}
	}
}
