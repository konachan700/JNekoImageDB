package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Metadata implements Serializable {
	public static final String META_IMG__FILE_EXT = "ext";
	public static final String META_IMG__MIMETYPE = "mime-type";
	public static final String META_IMG__WIDTH = "img-w";
	public static final String META_IMG__HEIGHT = "img-h";

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
