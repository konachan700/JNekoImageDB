package proto;

import java.util.UUID;

public interface SettingsService extends Disposable {
	<T> T readSettingsEntry(UUID uuid, String name, T defaultValue);
	void writeSettingsEntry(UUID uuid, String name, Object value);
}
