package service;

import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.GlobalConfig;
import proto.CryptographyService;
import proto.Disposable;
import proto.LocalDaoService;
import proto.LocalStorageService;
import proto.UseStorageDirectory;
import ui.imageview.AbstractImageDashboard;
import ui.imageview.localdb.LocalDbImageView;
import ui.imageview.localfs.LocalFsImageView;
import utils.ReflectionConverter;

public class InitService implements UseStorageDirectory {
	private final AtomicBoolean init = new AtomicBoolean(false);

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Path configPath = null;
	private final GlobalConfig config = new GlobalConfig();

	private final Map<Class, Disposable> services = new HashMap<>();

	private String password = null;

	public void init() {
		if (init.get()) return;

		if (password == null) {
			throw new IllegalStateException("Please, set password first!");
		}

		// ******************** Create storage directory ********************
		final File storageDirectory = getStorageDirectory();
		if (!storageDirectory.exists()) {
			storageDirectory.mkdirs();
			if (!storageDirectory.exists() || !storageDirectory.isDirectory()) {
				throw new IllegalStateException("UseStorageDirectory.getStorageDirectory() cannot create storage directory");
			}
		}

		// ******************** Read configuration ********************
		configPath = new File(storageDirectory.getPath() + File.separator + "config.json").getAbsoluteFile().toPath();
		try {
			final String configData = new String(Files.readAllBytes(configPath));
			final GlobalConfig tempConfig = gson.fromJson(configData, GlobalConfig.class);
			ReflectionConverter.convert(tempConfig, config);
		} catch (IOException e) {
			final String configData = gson.toJson(config);
			try {
				Files.write(configPath, configData.getBytes(), CREATE);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new IllegalStateException("Can't write config");
			}
		}

		// ******************** Start services ********************
		final CryptographyService cryptographyService = CryptographyServiceImpl.getInstance(password, config.getSalt(), config.getEncryptType());
		final LocalDaoService localDaoService = new LocalDaoServiceImpl(cryptographyService);
		final LocalStorageService localStorageService = new LocalStorageServiceImpl(cryptographyService, localDaoService);

		getServices().put(CryptographyService.class, cryptographyService);
		getServices().put(LocalDaoService.class, localDaoService);
		getServices().put(LocalStorageService.class, localStorageService);

		password = null;
		System.gc();

		init.set(true);
	}

	public void dispose() {
		if (!init.get()) return;

		// ******************** Save configuration ********************
		if (configPath != null) {
			final String configData = gson.toJson(config);
			try {
				Files.delete(configPath);
				Files.write(configPath, configData.getBytes(), CREATE);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new IllegalStateException("Can't write config");
			}
		}

		// ******************** Stop threads and timers ********************
		AbstractImageDashboard.disposeStatic();
		LocalDbImageView.disposeStatic();
		LocalFsImageView.disposeStatic();

		// ******************** Stop services ********************
		getServices().values().forEach(Disposable::dispose);

		// ******************** Set init state to false ********************
		init.set(false);
	}

	public void setPassword(String p) {
		if (init.get()) return;
		this.password = p;
	}

	public Map<Class, Disposable> getServices() {
		return services;
	}

	public GlobalConfig getGlobalConfig() {
		return config;
	}
}
