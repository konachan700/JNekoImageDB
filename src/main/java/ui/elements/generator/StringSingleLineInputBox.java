package ui.elements.generator;

import javafx.scene.input.KeyEvent;
import ui.annotation.creator.UICreatorGUIElement;

@UICreatorGUIElement
public class StringSingleLineInputBox extends TextLineInputBox<String>  {
	@Override
	public void onValueChanged(String text) {

	}

	@Override
	public String getValueClassName() {
		return String.class.getTypeName();
	}
}
