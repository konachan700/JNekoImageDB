package ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import annotation.HasStyledElements;
import javafx.scene.Node;
import annotation.CssStyle;

public class StyleParser {
	public static void parseStyles(Object object) {
		Optional.ofNullable(object)
				.map(Object::getClass)
				.map(Class::getDeclaredFields)
				.map(Arrays::asList)
				.map(list -> {
					final Class c = object.getClass().getSuperclass();
					if (c != null && c.isAnnotationPresent(HasStyledElements.class)) {
						final List<Field> fields = new ArrayList<>();
						fields.addAll(Arrays.asList(c.getDeclaredFields()));
						fields.addAll(list);
						return fields;
					}
					return list;
				})
				.orElse(Collections.EMPTY_LIST)
				.stream()
				.filter(Objects::nonNull)
				.forEach(f -> {
					final Field field = (Field) f;
					if (!field.isAnnotationPresent(CssStyle.class)) return;

					final String[] styles = Optional.ofNullable((CssStyle) field.getAnnotation(CssStyle.class)).map(CssStyle::value).orElse(null);
					if (styles == null) return;

					//System.out.println(field.getName());

					try {
						field.setAccessible(true);
						final Node node = (Node) field.get(object);
						if (node != null) node.getStyleClass().addAll(styles);
						field.setAccessible(false);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				});
	}
}
