package ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.Node;
import ui.annotation.style.CssStyle;
import ui.annotation.style.HasStyledElements;

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
				.orElse(new ArrayList<>())
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
