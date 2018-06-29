package ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import ui.annotation.creator.UICreatorDialog;
import ui.annotation.creator.UICreatorGUIElement;
import ui.proto.UIEntity;
import ui.annotation.creator.UICreatorEntityElement;
import ui.proto.UIPaneElement;

public class UICreator {
	private static final String scanPackage = "ui.elements.generator";
	private static final Map<String, Class> linkedUI = new HashMap<>();
	static {
		final Reflections reflections = new Reflections(scanPackage);
		final Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(UICreatorGUIElement.class);
		allClasses.stream()
				.forEach(e -> {
					try {
						final UIPaneElement uiCreatorElement = (UIPaneElement) e.newInstance();
						if (uiCreatorElement != null) {
							linkedUI.put(uiCreatorElement.getValueClassName(), e);
						}
					} catch (InstantiationException | IllegalAccessException e1) {
						e1.printStackTrace();
					}
				});
	}

	public static VBox createUI(UIEntity uiEntity) {
		if (uiEntity.getClass().isAnnotationPresent(UICreatorDialog.class)) {
			return createDialogUI(uiEntity);
		}
		return null;
	}

	private static VBox createDialogUI(UIEntity uiEntity) {
		final VBox content = new VBox();
		content.getStyleClass().addAll("null_pane");

		final ScrollPane rootSP = new ScrollPane();
		rootSP.getStyleClass().addAll("fill_all");
		rootSP.setContent(content);
		rootSP.setFitToWidth(true);
		rootSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		rootSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		final VBox root = new VBox();
		root.getStyleClass().addAll("null_pane");
		root.getChildren().add(rootSP);

		final Field[] fields = uiEntity.getClass().getDeclaredFields();
		if (fields == null || fields.length == 0) {
			//TODO: add "no element here" label
			return root;
		}
		Arrays.stream(fields)
				.filter(e -> !Modifier.isTransient(e.getModifiers()))
				.filter(e -> !Modifier.isStatic(e.getModifiers()))
				.filter(e -> !e.getType().isPrimitive())
				.filter(e -> !e.getType().isArray())
				.forEach(e -> {
					final String type = e.getType().getTypeName();
					final UIPaneElement uiPaneElement;
					try {
						uiPaneElement = (UIPaneElement) linkedUI.get(type).newInstance();
						if (uiPaneElement == null) {
							return;
						}

						if (e.isAnnotationPresent(UICreatorEntityElement.class)) {
							final UICreatorEntityElement anno = e.getAnnotation(UICreatorEntityElement.class);
							uiPaneElement.setCaption(anno.caption());
						} else {
							uiPaneElement.setCaption(e.getName() + "'s value");
						}
						e.setAccessible(true);
						final Object value = e.get(uiEntity);
						uiPaneElement.setValue(value);
						content.getChildren().add(uiPaneElement.getUIElement());
						e.setAccessible(false);
						uiPaneElement.setUpdateCallback(val -> {
							try {
								e.setAccessible(true);
								e.set(uiEntity, val);
								e.setAccessible(false);
							} catch (IllegalAccessException e1) {}
						});
					} catch (IllegalAccessException | InstantiationException e1) {
						e1.printStackTrace();
					}
				});
		return root;
	}
}
