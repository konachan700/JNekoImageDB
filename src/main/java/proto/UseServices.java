package proto;

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
import service.CryptographyServiceImpl;
import service.LocalDaoServiceImpl;
import service.LocalStorageServiceImpl;
import ui.imageview.AbstractImageDashboard;
import ui.imageview.localdb.LocalDbImageView;
import ui.imageview.localfs.LocalFsImageView;
import utils.ReflectionConverter;

public interface UseServices {
	Map<Class, Disposable> objects = new HashMap<>();
	AtomicBoolean init = new AtomicBoolean(false);
	GlobalConfig config = new GlobalConfig();
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	Path configPath = new File("./config.json").getAbsoluteFile().toPath();

	default <T> T getService(Class<T> clazz) {
		if (!init.get()) throw new IllegalStateException("Please, use init() before use");
		return (T) objects.get(clazz);
	}

	default boolean isInit() {
		return init.get();
	}

	default GlobalConfig getConfig() {
		return config;
	}

	default void init(String password) {
		if (init.get()) return;

		try {
			final String configData = new String(Files.readAllBytes(configPath));
			GlobalConfig tempConfig = gson.fromJson(configData, GlobalConfig.class);
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

		final CryptographyService cryptographyService = CryptographyServiceImpl.getInstance(password);
		final LocalDaoService localDaoService = new LocalDaoServiceImpl(cryptographyService);
		final LocalStorageService localStorageService = new LocalStorageServiceImpl(cryptographyService, localDaoService);

		objects.put(CryptographyService.class, cryptographyService);
		objects.put(LocalDaoService.class, localDaoService);
		objects.put(LocalStorageService.class, localStorageService);

		init.set(true);
	}

	default void dispose() {
		AbstractImageDashboard.disposeStatic();
		LocalDbImageView.disposeStatic();
		LocalFsImageView.disposeStatic();
		objects.values().forEach(Disposable::dispose);

		final String configData = gson.toJson(config);
		try {
			Files.write(configPath, configData.getBytes(), CREATE);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new IllegalStateException("Can't write config");
		}
	}
}
