package ui.elements.generator;

import javafx.scene.input.KeyEvent;
import ui.annotation.creator.UICreatorGUIElement;

@UICreatorGUIElement
public class IntegerInputBox extends TextLineInputBox<Integer> {
	@Override
	public void onValueChanged(String text) {
		try {
			Integer value = Integer.parseInt(this.getText());
			this.setError(false);
			if (getUiUpdateCallback() != null) {
				getUiUpdateCallback().onUpdate(value);
			}
		} catch (NumberFormatException err) {
			this.setError(true);
		}
	}

	@Override
	public String getValueClassName() {
		return Integer.class.getTypeName();
	}
}
