package proto;

import model.GlobalConfig;
import service.InitService;

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
