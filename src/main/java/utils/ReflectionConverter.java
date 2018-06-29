package utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReflectionConverter {

	private static String getId(Method method) {
		return  method.getName()
				.replaceAll("get", "")
				.replaceAll("set", "");
	}

	public static void convert(Object from, Object to) {
		if (from == null || to == null) {
			return;
		}

		final List<String> fromFields = Arrays.stream(from.getClass().getDeclaredFields()).map(f -> f.getName().toLowerCase()).collect(Collectors.toList());
		final Method[] fromMethods = from.getClass().getDeclaredMethods();
		final List<String> toFields = Arrays.stream(to.getClass().getDeclaredFields()).map(f -> f.getName().toLowerCase()).collect(Collectors.toList());
		final Method[] toMethods = to.getClass().getDeclaredMethods();
		if (fromMethods == null || toMethods == null || fromMethods.length == 0 || toMethods.length == 0) {
			return;
		}

		final Map<String, Method> mapFromGetters = Arrays.stream(fromMethods)
				.filter(m -> m.getName().startsWith("get"))
				.filter(m -> fromFields.contains(getId(m).toLowerCase()))
				//.peek(m -> m.setAccessible(true))
				.collect(Collectors.toMap(ReflectionConverter::getId, v -> v));

		final Map<String, Method> mapToSetters = Arrays.stream(toMethods)
				.filter(m -> m.getName().startsWith("set"))
				.filter(m -> toFields.contains(getId(m).toLowerCase()))
				//.peek(m -> m.setAccessible(true))
				.collect(Collectors.toMap(ReflectionConverter::getId, v -> v));

		mapFromGetters.keySet().forEach(key -> {
			if (!mapToSetters.containsKey(key)) {
				return;
			}

			final Method getter = mapFromGetters.get(key);
			final Method setter = mapToSetters.get(key);

			try {
				Object oFrom = getter.invoke(from);
				setter.invoke(to, oFrom);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}
}
