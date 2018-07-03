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

import javafx.application.Platform;
import model.GlobalConfig;
import service.CryptographyServiceImpl;
import service.InitService;
import service.LocalDaoServiceImpl;
import service.LocalStorageServiceImpl;
import ui.imageview.AbstractImageDashboard;
import ui.imageview.localdb.LocalDbImageView;
import ui.imageview.localfs.LocalFsImageView;
import utils.ReflectionConverter;

public interface UseServices {
	InitService initService = new InitService();

	default <T> T getService(Class<T> clazz) {
		if (initService.getServices() == null || !initService.getServices().containsKey(clazz)) {
			throw new IllegalStateException("Class \"" + clazz + "\" not found in list of services");
		}
		return (T) initService.getServices().get(clazz);
	}

	default GlobalConfig getConfig() {
		return initService.getGlobalConfig();
	}

	default void init(String password) {
		initService.setPassword(password);
		initService.init();
	}

	default void dispose() {
		initService.dispose();
	}
}
