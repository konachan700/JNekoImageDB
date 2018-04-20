package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Metadata implements Serializable {
	private final Map<String, Object> meta = new HashMap<>();

	public void setMeta(String key, Object object) {
		if (key != null && object != null) meta.put(key, object);
	}

	public <T> T getMeta(String key, Class<T> clazz) {
		final Object o = meta.get(key);
		if (o == null) return null;
		if (!clazz.isInstance(o)) return null;
		return (T) o;
	}
}
