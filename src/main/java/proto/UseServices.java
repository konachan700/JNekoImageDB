package proto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import service.CryptographyServiceImpl;
import service.LocalDaoServiceImpl;
import service.LocalStorageServiceImpl;
import service.SettingsServiceImpl;
import ui.imagelist.AbstractImageDashboard;
import ui.imageview.BaseImageView;

public interface UseServices {
	Map<Class, Disposable> objects = new HashMap<>();
	AtomicBoolean init = new AtomicBoolean(false);

	default <T> T getService(Class<T> clazz) {
		if (!init.get()) throw new IllegalStateException("Please, use init() before use");
		return (T) objects.get(clazz);
	}

	default boolean isInit() {
		return init.get();
	}

	default void init(String password) {
		if (init.get()) return;

		final SettingsService settingsService = new SettingsServiceImpl();
		final CryptographyService cryptographyService = CryptographyServiceImpl.getInstance(password);
		final LocalDaoService localDaoService = new LocalDaoServiceImpl(cryptographyService);
		final LocalStorageService localStorageService = new LocalStorageServiceImpl(cryptographyService);

		objects.put(SettingsService.class, settingsService);
		objects.put(CryptographyService.class, cryptographyService);
		objects.put(LocalDaoService.class, localDaoService);
		objects.put(LocalStorageService.class, localStorageService);

		init.set(true);
	}

	default void dispose() {
		AbstractImageDashboard.disposeStatic();
		BaseImageView.disposeStatic();

		objects.values().forEach(Disposable::dispose);
	}
}
