package ui.proto;

import javafx.scene.layout.Pane;

public interface UIPaneElement<T> {
	String getValueClassName();
	T getValue();
	void setValue(T value);
	Pane getUIElement();
	void setCaption(String value);
	void setUpdateCallback(UIUpdateCallback updateCallback);
}
