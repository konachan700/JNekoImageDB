package ui.taglist;

import javafx.scene.control.Label;

public class TagElement extends Label {
	public TagElement(String tag){
		super(tag);
		super.getStyleClass().addAll("tag_element");
		super.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
	}

	public TagElement(String tag, String colorStyle){
		super(tag);
		super.getStyleClass().addAll("tag_element", colorStyle);
		super.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
	}
}
